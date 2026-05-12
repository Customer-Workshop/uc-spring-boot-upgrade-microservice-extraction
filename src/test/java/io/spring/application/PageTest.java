package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PageTest {

  @Test
  void should_create_page_with_defaults() {
    Page page = new Page();
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void should_create_page_with_custom_values() {
    Page page = new Page(10, 30);
    assertEquals(10, page.getOffset());
    assertEquals(30, page.getLimit());
  }

  @Test
  void should_not_allow_negative_offset() {
    Page page = new Page(-5, 20);
    assertEquals(0, page.getOffset());
  }

  @Test
  void should_cap_limit_at_max() {
    Page page = new Page(0, 200);
    assertEquals(100, page.getLimit());
  }

  @Test
  void should_not_allow_negative_limit() {
    Page page = new Page(0, -5);
    assertEquals(20, page.getLimit());
  }

  @Test
  void should_not_allow_zero_limit() {
    Page page = new Page(0, 0);
    assertEquals(20, page.getLimit());
  }
}
