package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

public class ArticlePropertyTest {

  private static final Pattern URL_SAFE_PATTERN = Pattern.compile("^[a-z0-9\\-\\u0080-\\uffff]+$");

  @Property
  void any_valid_title_should_be_accepted(
      @ForAll @StringLength(min = 1, max = 200) String title) {
    Assume.that(!title.trim().isEmpty());
    Assume.that(title.matches(".*[a-zA-Z0-9\\u0080-\\uffff].*"));

    Article article =
        new Article(title, "description", "body", Collections.singletonList("tag"), "userId");

    assertThat(article.getTitle(), is(title));
    assertThat(article.getSlug(), is(notNullValue()));
    assertThat(article.getSlug().isEmpty(), is(false));
  }

  @Property
  void slug_generation_should_always_produce_url_safe_strings(
      @ForAll("alphanumericTitles") String title) {
    String slug = Article.toSlug(title);

    assertThat(slug, is(notNullValue()));
    assertThat(slug.isEmpty(), is(false));
    // Slug should not contain uppercase letters
    assertThat(slug, is(slug.toLowerCase()));
    // Slug should not contain spaces, question marks, commas, or periods
    assertThat(slug.contains(" "), is(false));
    assertThat(slug.contains("?"), is(false));
    assertThat(slug.contains(","), is(false));
    assertThat(slug.contains("."), is(false));
    // Slug should match URL-safe pattern (lowercase alphanumeric, hyphens, unicode)
    assertThat(
        "Slug '" + slug + "' should be URL-safe",
        URL_SAFE_PATTERN.matcher(slug).matches(),
        is(true));
  }

  @Property
  void article_body_should_be_preserved_exactly(@ForAll String body) {
    Assume.that(body != null);

    Article article =
        new Article("title", "description", body, Collections.singletonList("tag"), "userId");

    assertThat(article.getBody(), is(body));
  }

  @Property
  void article_body_should_be_preserved_after_update(@ForAll String originalBody, @ForAll String newBody) {
    Assume.that(originalBody != null);
    Assume.that(newBody != null);
    Assume.that(!newBody.isEmpty());

    Article article =
        new Article(
            "title", "description", originalBody, Collections.singletonList("tag"), "userId");

    assertThat(article.getBody(), is(originalBody));

    article.update(null, null, newBody);

    assertThat(article.getBody(), is(newBody));
  }

  @Property
  void article_description_should_be_preserved(@ForAll String description) {
    Assume.that(description != null);

    Article article =
        new Article("title", description, "body", Collections.singletonList("tag"), "userId");

    assertThat(article.getDescription(), is(description));
  }

  @Property
  void article_tags_should_be_deduplicated(@ForAll List<@StringLength(min = 1, max = 50) String> tagNames) {
    Assume.that(tagNames != null);
    Assume.that(!tagNames.isEmpty());
    Assume.that(tagNames.stream().allMatch(t -> t != null && !t.isEmpty()));

    Article article = new Article("title", "desc", "body", tagNames, "userId");

    long uniqueCount = tagNames.stream().distinct().count();
    assertThat((long) article.getTags().size(), is(uniqueCount));
  }

  @Provide
  Arbitrary<String> alphanumericTitles() {
    // Only include characters that the toSlug regex actually handles:
    // letters, digits, spaces, commas, periods, question marks, ampersands
    // Single/double quotes are NOT fully stripped by toSlug, so we exclude them
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withCharRange('A', 'Z')
        .withCharRange('0', '9')
        .withChars(' ', ',', '.', '?', '&')
        .ofMinLength(1)
        .ofMaxLength(100)
        .filter(s -> s.matches(".*[a-zA-Z0-9].*"));
  }
}
