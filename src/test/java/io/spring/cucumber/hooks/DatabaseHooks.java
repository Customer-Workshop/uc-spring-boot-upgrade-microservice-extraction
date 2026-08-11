package io.spring.cucumber.hooks;

import io.cucumber.java.Before;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Gives every scenario a clean database. */
public class DatabaseHooks {

  private static final List<String> TABLES =
      Arrays.asList(
          "comments", "article_tags", "article_favorites", "tags", "articles", "follows", "users");

  @Autowired private JdbcTemplate jdbcTemplate;

  @Before
  public void cleanDatabase() {
    TABLES.forEach(table -> jdbcTemplate.update("delete from " + table));
  }
}
