package io.spring.application.tag;

import io.spring.application.TagStatsData;
import io.spring.application.TagsQueryService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import({TagsQueryService.class, MyBatisArticleRepository.class})
public class TagsQueryServiceTest extends DbTestBase {
  @Autowired private TagsQueryService tagsQueryService;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  public void should_get_all_tags() {
    articleRepository.save(new Article("test", "test", "test", Arrays.asList("java"), "123"));
    Assertions.assertTrue(tagsQueryService.allTags().contains("java"));
  }

  @Test
  public void should_get_tag_stats_ordered_by_article_count_then_name() {
    articleRepository.save(new Article("first", "test", "test", Arrays.asList("java"), "123"));
    articleRepository.save(new Article("second", "test", "test", Arrays.asList("java"), "123"));
    articleRepository.save(new Article("third", "test", "test", Arrays.asList("alpha"), "123"));
    articleRepository.save(new Article("fourth", "test", "test", Arrays.asList("spring"), "123"));

    List<TagStatsData> stats = tagsQueryService.tagStats();

    Assertions.assertEquals("java", stats.get(0).getName());
    Assertions.assertEquals(2, stats.get(0).getArticleCount());
    Assertions.assertEquals("alpha", stats.get(1).getName());
    Assertions.assertEquals("spring", stats.get(2).getName());
    Assertions.assertEquals(1, stats.get(1).getArticleCount());
    Assertions.assertEquals(1, stats.get(2).getArticleCount());
  }

  @Test
  public void should_include_tags_with_zero_articles() {
    jdbcTemplate.update("insert into tags (id, name) values (?, ?)", "unused-id", "unused");

    List<TagStatsData> stats = tagsQueryService.tagStats();

    TagStatsData unused =
        stats.stream().filter(tag -> tag.getName().equals("unused")).findFirst().get();
    Assertions.assertEquals(0, unused.getArticleCount());
  }
}
