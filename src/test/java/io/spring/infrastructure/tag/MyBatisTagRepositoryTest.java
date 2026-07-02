package io.spring.infrastructure.tag;

import io.spring.core.article.Tag;
import io.spring.core.article.TagRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisTagRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisTagRepository.class})
public class MyBatisTagRepositoryTest extends DbTestBase {

  @Autowired private TagRepository tagRepository;

  @Test
  public void should_create_and_fetch_tag() {
    Tag tag = new Tag("java");
    tagRepository.save(tag);

    Optional<Tag> found = tagRepository.findById(tag.getId());
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals("java", found.get().getName());
    Assertions.assertNotNull(found.get().getCreatedAt());
  }

  @Test
  public void should_find_tag_by_name() {
    Tag tag = new Tag("spring-boot");
    tagRepository.save(tag);

    Optional<Tag> found = tagRepository.findByName("spring-boot");
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals(tag.getId(), found.get().getId());
  }

  @Test
  public void should_find_all_tags() {
    Tag tag1 = new Tag("kotlin");
    Tag tag2 = new Tag("scala");
    tagRepository.save(tag1);
    tagRepository.save(tag2);

    List<Tag> all = tagRepository.findAll();
    Assertions.assertTrue(all.size() >= 2);
    Assertions.assertTrue(all.stream().anyMatch(t -> t.getName().equals("kotlin")));
    Assertions.assertTrue(all.stream().anyMatch(t -> t.getName().equals("scala")));
  }

  @Test
  public void should_update_tag() {
    Tag tag = new Tag("oldname");
    tagRepository.save(tag);

    tag.update("newname");
    tagRepository.save(tag);

    Optional<Tag> found = tagRepository.findById(tag.getId());
    Assertions.assertTrue(found.isPresent());
    Assertions.assertEquals("newname", found.get().getName());
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
  public void should_return_empty_when_tag_not_found() {
    Optional<Tag> found = tagRepository.findById("nonexistent-id");
    Assertions.assertFalse(found.isPresent());

    Optional<Tag> foundByName = tagRepository.findByName("nonexistent-name");
    Assertions.assertFalse(foundByName.isPresent());
  }
}
