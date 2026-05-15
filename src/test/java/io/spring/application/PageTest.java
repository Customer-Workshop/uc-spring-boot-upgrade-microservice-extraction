package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PageTest {

  @Test
  public void should_use_default_values() {
    Page page = new Page();
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_set_custom_offset_and_limit() {
    Page page = new Page(10, 50);
    assertEquals(10, page.getOffset());
    assertEquals(50, page.getLimit());
  }

  @Test
  public void should_cap_limit_at_max() {
    Page page = new Page(0, 200);
    assertEquals(100, page.getLimit());
  }

  @Test
  public void should_not_set_negative_offset() {
    Page page = new Page(-5, 20);
    assertEquals(0, page.getOffset());
  }

  @Test
  public void should_not_set_negative_limit() {
    Page page = new Page(0, -5);
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_not_set_zero_limit() {
    Page page = new Page(0, 0);
    assertEquals(20, page.getLimit());
  }
}
