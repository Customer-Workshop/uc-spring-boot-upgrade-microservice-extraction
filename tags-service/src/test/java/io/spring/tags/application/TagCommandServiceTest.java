package io.spring.tags.application;

import io.spring.tags.domain.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@MybatisTest
@Import(TagCommandService.class)
public class TagCommandServiceTest {
  @Autowired private TagCommandService tagCommandService;

  @Test
  public void should_create_tag() {
    Tag tag = tagCommandService.findOrCreateTag("spring");
    Assertions.assertNotNull(tag.getId());
    Assertions.assertEquals("spring", tag.getName());
  }

  @Test
  public void should_return_existing_tag_when_duplicate() {
    Tag first = tagCommandService.findOrCreateTag("java");
    Tag second = tagCommandService.findOrCreateTag("java");
    Assertions.assertEquals(first.getId(), second.getId());
  }

  @Test
  public void should_create_article_tag_relation() {
    Tag tag = tagCommandService.findOrCreateTag("testing");
    // Should not throw
    tagCommandService.createArticleTagRelation("article-1", tag.getId());
  }
}
