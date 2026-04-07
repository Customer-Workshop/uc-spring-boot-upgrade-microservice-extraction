package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void should_create_with_datetime() {
    DateTime now = new DateTime();
    DateTimeCursor cursor = new DateTimeCursor(now);
    assertEquals(now, cursor.getData());
  }

  @Test
  public void should_convert_to_string_as_millis() {
    DateTime dt = new DateTime(1234567890000L, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dt);
    assertEquals("1234567890000", cursor.toString());
  }

  @Test
  public void should_parse_null_returns_null() {
    DateTime result = DateTimeCursor.parse(null);
    assertNull(result);
  }

  @Test
  public void should_parse_valid_cursor_string() {
    DateTime result = DateTimeCursor.parse("1234567890000");
    assertNotNull(result);
    assertEquals(1234567890000L, result.getMillis());
    assertEquals(DateTimeZone.UTC, result.getZone());
  }

  @Test
  public void should_roundtrip_cursor() {
    DateTime original = new DateTime(DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(original);
    String str = cursor.toString();
    DateTime parsed = DateTimeCursor.parse(str);
    assertEquals(original.getMillis(), parsed.getMillis());
  }
}
