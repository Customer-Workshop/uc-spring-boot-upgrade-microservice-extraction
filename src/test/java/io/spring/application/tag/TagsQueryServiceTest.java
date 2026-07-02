package io.spring.application.tag;

import io.spring.application.TagsQueryService;
import io.spring.application.data.TagData;
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

@Import({TagsQueryService.class, MyBatisArticleRepository.class, MyBatisTagRepository.class})
public class TagsQueryServiceTest extends DbTestBase {
  @Autowired private TagsQueryService tagsQueryService;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private TagRepository tagRepository;

  @Test
  public void should_get_all_tags() {
    articleRepository.save(new Article("test", "test", "test", Arrays.asList("java"), "123"));
    Assertions.assertTrue(tagsQueryService.allTags().contains("java"));
  }

  @Test
  public void should_create_tag() {
    Tag tag = tagsQueryService.createTag("new-tag");
    Assertions.assertNotNull(tag.getId());
    Assertions.assertEquals("new-tag", tag.getName());
    Assertions.assertNotNull(tag.getCreatedAt());

    Optional<Tag> found = tagRepository.findByName("new-tag");
    Assertions.assertTrue(found.isPresent());
  }

  @Test
  public void should_not_create_duplicate_tag() {
    tagsQueryService.createTag("unique-tag");
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> tagsQueryService.createTag("unique-tag"));
  }

  @Test
  public void should_update_tag() {
    Tag tag = tagsQueryService.createTag("old-name");
    Tag updated = tagsQueryService.updateTag(tag.getId(), "new-name");
    Assertions.assertEquals("new-name", updated.getName());
  }

  @Test
  public void should_remove_tag() {
    Tag tag = tagsQueryService.createTag("removable");
    tagsQueryService.removeTag(tag.getId());
    Optional<Tag> found = tagRepository.findById(tag.getId());
    Assertions.assertFalse(found.isPresent());
  }

  @Test
  public void should_find_all_tag_details() {
    tagsQueryService.createTag("detail-tag-1");
    tagsQueryService.createTag("detail-tag-2");
    List<TagData> tags = tagsQueryService.findAll();
    Assertions.assertTrue(tags.size() >= 2);
  }

  @Test
  public void should_find_tag_by_id() {
    Tag tag = tagsQueryService.createTag("findable");
    Optional<TagData> found = tagsQueryService.findById(tag.getId());
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals("findable", found.get().getName());
  }

  @Test
  public void should_find_tags_by_article() {
    articleRepository.save(
        new Article("tagged-article", "desc", "body", Arrays.asList("art-tag"), "user1"));
    Article article =
        articleRepository.findBySlug("tagged-article").orElseThrow(RuntimeException::new);
    List<TagData> tags = tagsQueryService.findByArticleId(article.getId());
    Assertions.assertTrue(tags.stream().anyMatch(t -> t.getName().equals("art-tag")));
  }
}
