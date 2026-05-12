package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PageTest {

  @Test
  void should_create_page_with_valid_params() {
    Page page = new Page(0, 20);
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void should_create_page_with_defaults() {
    Page page = new Page(0, 0);
    assertNotNull(page);
  }

  @Test
  void should_test_page_equality() {
    Page p1 = new Page(0, 20);
    Page p2 = new Page(0, 20);
    Page p3 = new Page(1, 10);
    assertEquals(p1, p2);
    assertNotEquals(p1, p3);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
  }
}
