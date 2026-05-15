package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void should_store_datetime() {
    DateTime now = new DateTime();
    DateTimeCursor cursor = new DateTimeCursor(now);
    assertEquals(now, cursor.getData());
  }

  @Test
  public void should_convert_to_string_as_millis() {
    DateTime now = new DateTime();
    DateTimeCursor cursor = new DateTimeCursor(now);
    assertEquals(String.valueOf(now.getMillis()), cursor.toString());
  }

  @Test
  public void should_parse_cursor_string() {
    DateTime now = new DateTime();
    String cursorStr = String.valueOf(now.getMillis());
    DateTime parsed = DateTimeCursor.parse(cursorStr);
    assertNotNull(parsed);
    assertEquals(now.getMillis(), parsed.getMillis());
    assertEquals(DateTimeZone.UTC, parsed.getZone());
  }

  @Test
  public void should_return_null_for_null_cursor() {
    assertNull(DateTimeCursor.parse(null));
  }
}
