package io.spring.infrastructure.tag;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.article.TagRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisTagRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisTagRepository.class, MyBatisArticleRepository.class})
public class MyBatisTagRepositoryTest extends DbTestBase {
  @Autowired private TagRepository tagRepository;

  @Autowired private ArticleRepository articleRepository;

  @Test
  public void should_create_and_fetch_tag() {
    Tag tag = new Tag("java");
    tagRepository.save(tag);

    Optional<Tag> found = tagRepository.findById(tag.getId());
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals("java", found.get().getName());
  }

  @Test
  public void should_find_tag_by_name() {
    Tag tag = new Tag("spring");
    tagRepository.save(tag);

    Optional<Tag> found = tagRepository.findByName("spring");
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals(tag.getId(), found.get().getId());
  }

  @Test
  public void should_find_all_tags() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("kotlin");
    tagRepository.save(tag1);
    tagRepository.save(tag2);

    List<Tag> tags = tagRepository.findAll();
    Assertions.assertTrue(tags.size() >= 2);
  }

  @Test
  public void should_update_tag() {
    Tag tag = new Tag("old-name");
    tagRepository.save(tag);

    tag.setName("new-name");
    tagRepository.save(tag);

    Optional<Tag> found = tagRepository.findById(tag.getId());
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals("new-name", found.get().getName());
  }

  @Test
  public void should_delete_tag() {
    Tag tag = new Tag("deleteme");
    tagRepository.save(tag);

    tagRepository.remove(tag.getId());

    Optional<Tag> found = tagRepository.findById(tag.getId());
    Assertions.assertFalse(found.isPresent());
  }

  @Test
  public void should_find_tags_by_article_id() {
    Article article =
        new Article("test", "desc", "body", Arrays.asList("java", "spring"), "user-1");
    articleRepository.save(article);

    List<Tag> tags = tagRepository.findByArticleId(article.getId());
    Assertions.assertEquals(2, tags.size());
  }

  @Test
  public void should_delete_tag_and_remove_article_associations() {
    Article article = new Article("test", "desc", "body", Arrays.asList("removetag"), "user-1");
    articleRepository.save(article);

    Optional<Tag> tag = tagRepository.findByName("removetag");
    Assertions.assertTrue(tag.isPresent());

    tagRepository.remove(tag.get().getId());

    List<Tag> tags = tagRepository.findByArticleId(article.getId());
    Assertions.assertTrue(tags.isEmpty());
  }
}
