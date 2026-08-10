package io.spring.cucumber;

import java.util.Arrays;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/** Empties every table so that each scenario starts from a known state. */
public class DatabaseCleaner {

  private static final List<String> TABLES =
      Arrays.asList(
          "article_favorites", "article_tags", "comments", "follows", "tags", "articles", "users");

  private final JdbcTemplate jdbcTemplate;

  public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void clean() {
    TABLES.forEach(table -> jdbcTemplate.update("delete from " + table));
  }
}
