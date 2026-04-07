package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PageTest {

  @Test
  public void should_create_with_default_values() {
    Page page = new Page();
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_create_with_valid_offset_and_limit() {
    Page page = new Page(5, 10);
    assertEquals(5, page.getOffset());
    assertEquals(10, page.getLimit());
  }

  @Test
  public void should_cap_limit_at_max() {
    Page page = new Page(0, 200);
    assertEquals(100, page.getLimit());
  }

  @Test
  public void should_keep_default_offset_for_negative_value() {
    Page page = new Page(-1, 10);
    assertEquals(0, page.getOffset());
  }

  @Test
  public void should_keep_default_offset_for_zero() {
    Page page = new Page(0, 10);
    assertEquals(0, page.getOffset());
  }

  @Test
  public void should_keep_default_limit_for_negative_value() {
    Page page = new Page(0, -1);
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_keep_default_limit_for_zero() {
    Page page = new Page(0, 0);
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_set_valid_positive_offset() {
    Page page = new Page(10, 20);
    assertEquals(10, page.getOffset());
  }

  @Test
  public void should_set_limit_at_boundary() {
    Page page = new Page(0, 100);
    assertEquals(100, page.getLimit());
  }

  @Test
  public void should_cap_limit_just_above_max() {
    Page page = new Page(0, 101);
    assertEquals(100, page.getLimit());
  }

  @Test
  public void should_set_limit_to_one() {
    Page page = new Page(0, 1);
    assertEquals(1, page.getLimit());
  }
}
