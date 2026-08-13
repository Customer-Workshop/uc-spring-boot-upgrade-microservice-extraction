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
    articleRepository.save(
        new Article("first", "test", "test", Arrays.asList("java", "spring"), "123"));
    articleRepository.save(
        new Article("second", "test", "test", Arrays.asList("java", "spring"), "123"));
    articleRepository.save(new Article("third", "test", "test", Arrays.asList("java"), "123"));
    articleRepository.save(new Article("zeta", "test", "test", Arrays.asList("zeta"), "123"));
    articleRepository.save(new Article("alpha", "test", "test", Arrays.asList("alpha"), "123"));
    jdbcTemplate.update("insert into tags (id, name) values (?, ?)", "zulu-zero-id", "zulu-zero");
    jdbcTemplate.update("insert into tags (id, name) values (?, ?)", "alpha-zero-id", "alpha-zero");

    List<TagStatsData> stats = tagsQueryService.tagStats();

    Assertions.assertEquals(
        Arrays.asList(
            new TagStatsData("java", 3),
            new TagStatsData("spring", 2),
            new TagStatsData("alpha", 1),
            new TagStatsData("zeta", 1),
            new TagStatsData("alpha-zero", 0),
            new TagStatsData("zulu-zero", 0)),
        stats);
  }
}
