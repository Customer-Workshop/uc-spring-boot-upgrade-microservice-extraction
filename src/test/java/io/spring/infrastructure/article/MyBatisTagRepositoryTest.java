package io.spring.infrastructure.article;

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
  public void should_create_and_fetch_tag_by_id() {
    Tag tag = new Tag("java");
    tagRepository.save(tag);

    Optional<Tag> found = tagRepository.findById(tag.getId());
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals("java", found.get().getName());
    Assertions.assertEquals(tag.getId(), found.get().getId());
  }

  @Test
  public void should_create_and_fetch_tag_by_name() {
    Tag tag = new Tag("spring-boot");
    tagRepository.save(tag);

    Optional<Tag> found = tagRepository.findByName("spring-boot");
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals(tag.getId(), found.get().getId());
  }

  @Test
  public void should_find_all_tags() {
    Tag tag1 = new Tag("alpha");
    Tag tag2 = new Tag("beta");
    tagRepository.save(tag1);
    tagRepository.save(tag2);

    List<Tag> tags = tagRepository.findAll();
    Assertions.assertTrue(tags.size() >= 2);
    Assertions.assertTrue(tags.stream().anyMatch(t -> t.getName().equals("alpha")));
    Assertions.assertTrue(tags.stream().anyMatch(t -> t.getName().equals("beta")));
  }

  @Test
  public void should_update_tag_name() {
    Tag tag = new Tag("old-name");
    tagRepository.save(tag);

    tag.update("new-name");
    tagRepository.save(tag);

    Optional<Tag> found = tagRepository.findById(tag.getId());
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals("new-name", found.get().getName());
  }

  @Test
  public void should_delete_tag() {
    Tag tag = new Tag("to-delete");
    tagRepository.save(tag);

    tagRepository.remove(tag.getId());

    Optional<Tag> found = tagRepository.findById(tag.getId());
    Assertions.assertFalse(found.isPresent());
  }

  @Test
  public void should_return_empty_for_nonexistent_tag() {
    Optional<Tag> found = tagRepository.findById("nonexistent-id");
    Assertions.assertFalse(found.isPresent());

    Optional<Tag> foundByName = tagRepository.findByName("nonexistent-name");
    Assertions.assertFalse(foundByName.isPresent());
  }

  @Test
  public void should_remove_article_tag_associations_when_removing_by_tag_id() {
    Tag tag = new Tag("removable-tag");
    tagRepository.save(tag);

    Article article =
        new Article("test-article", "desc", "body", Arrays.asList("removable-tag"), "user-1");
    articleRepository.save(article);

    tagRepository.removeArticleTagsByTagId(tag.getId());
    tagRepository.remove(tag.getId());

    Assertions.assertFalse(tagRepository.findById(tag.getId()).isPresent());
  }
}
