package io.spring.application.tag;

import io.spring.application.TagsQueryService;
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
  public void should_get_all_tag_entities() {
    articleRepository.save(
        new Article("test", "test", "test", Arrays.asList("spring", "boot"), "123"));
    List<Tag> tags = tagsQueryService.allTagEntities();
    Assertions.assertTrue(tags.size() >= 2);
  }

  @Test
  public void should_create_tag() {
    Tag tag = tagsQueryService.createTag("new-tag");
    Assertions.assertNotNull(tag.getId());
    Assertions.assertEquals("new-tag", tag.getName());
  }

  @Test
  public void should_return_existing_tag_when_creating_duplicate() {
    Tag tag1 = tagsQueryService.createTag("duplicate");
    Tag tag2 = tagsQueryService.createTag("duplicate");
    Assertions.assertEquals(tag1.getId(), tag2.getId());
  }

  @Test
  public void should_find_tag_by_id() {
    Tag created = tagsQueryService.createTag("findme");
    Optional<Tag> found = tagsQueryService.findById(created.getId());
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals("findme", found.get().getName());
  }

  @Test
  public void should_find_tag_by_name() {
    tagsQueryService.createTag("byname");
    Optional<Tag> found = tagsQueryService.findByName("byname");
    Assertions.assertTrue(found.isPresent());
  }

  @Test
  public void should_update_tag() {
    Tag tag = tagsQueryService.createTag("before-update");
    Tag updated = tagsQueryService.updateTag(tag.getId(), "after-update");
    Assertions.assertEquals("after-update", updated.getName());
  }

  @Test
  public void should_delete_tag() {
    Tag tag = tagsQueryService.createTag("to-delete");
    tagsQueryService.deleteTag(tag.getId());
    Optional<Tag> found = tagsQueryService.findById(tag.getId());
    Assertions.assertFalse(found.isPresent());
  }

  @Test
  public void should_find_tags_by_article_id() {
    Article article =
        new Article("test-article", "desc", "body", Arrays.asList("tag1", "tag2"), "user-1");
    articleRepository.save(article);

    List<Tag> tags = tagsQueryService.findByArticleId(article.getId());
    Assertions.assertEquals(2, tags.size());
  }
}
