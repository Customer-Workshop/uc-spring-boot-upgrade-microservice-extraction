package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PageTest {

  @Test
  public void should_create_default_page() {
    Page page = new Page();
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_create_page_with_valid_params() {
    Page page = new Page(5, 50);
    assertEquals(5, page.getOffset());
    assertEquals(50, page.getLimit());
  }

  @Test
  public void should_cap_limit_at_max() {
    Page page = new Page(0, 200);
    assertEquals(100, page.getLimit());
  }

  @Test
  public void should_ignore_negative_offset() {
    Page page = new Page(-1, 10);
    assertEquals(0, page.getOffset());
  }

  @Test
  public void should_ignore_negative_limit() {
    Page page = new Page(0, -5);
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_ignore_zero_limit() {
    Page page = new Page(0, 0);
    assertEquals(20, page.getLimit());
  }
}
