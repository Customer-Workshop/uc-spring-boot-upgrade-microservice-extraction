package io.spring.tags.application;

import io.spring.tags.domain.Tag;
import io.spring.tags.infrastructure.mybatis.mapper.TagMapper;
import java.util.List;
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
@Import({TagsQueryService.class, TagCommandService.class})
public class TagsQueryServiceTest {
  @Autowired private TagsQueryService tagsQueryService;
  @Autowired private TagCommandService tagCommandService;

  @Test
  public void should_get_all_tags() {
    tagCommandService.findOrCreateTag("java");
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.contains("java"));
  }

  @Test
  public void should_return_empty_when_no_tags() {
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.isEmpty());
  }
}
