package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class PageCursorTest {

  @Test
  public void should_store_and_retrieve_data() {
    DateTime dt = new DateTime(DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dt);
    assertEquals(dt, cursor.getData());
  }

  @Test
  public void should_convert_to_string() {
    DateTime dt = new DateTime(1000L, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dt);
    assertEquals("1000", cursor.toString());
  }
}
