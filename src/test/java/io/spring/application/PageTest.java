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
  void should_create_page_with_valid_params() {
    Page page = new Page(10, 50);

    assertEquals(10, page.getOffset());
    assertEquals(50, page.getLimit());
  }

  @Test
  void should_cap_limit_to_max() {
    Page page = new Page(0, 200);

    assertEquals(100, page.getLimit());
  }

  @Test
  void should_use_default_limit_for_zero() {
    Page page = new Page(0, 0);

    assertEquals(20, page.getLimit());
  }

  @Test
  void should_use_default_limit_for_negative() {
    Page page = new Page(0, -5);

    assertEquals(20, page.getLimit());
  }

  @Test
  void should_use_default_offset_for_negative() {
    Page page = new Page(-1, 10);

    assertEquals(0, page.getOffset());
    assertEquals(10, page.getLimit());
  }

  @Test
  void should_accept_offset_of_zero() {
    Page page = new Page(0, 10);

    assertEquals(0, page.getOffset());
  }

  @Test
  void should_accept_limit_at_max_boundary() {
    Page page = new Page(0, 100);

    assertEquals(100, page.getLimit());
  }
}
