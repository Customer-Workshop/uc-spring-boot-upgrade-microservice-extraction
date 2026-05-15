package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  void should_create_cursor_with_datetime() {
    DateTime dt = new DateTime(1234567890000L);
    DateTimeCursor cursor = new DateTimeCursor(dt);

    assertEquals(dt, cursor.getData());
  }

  @Test
  void should_convert_to_string_as_millis() {
    DateTime dt = new DateTime(1234567890000L);
    DateTimeCursor cursor = new DateTimeCursor(dt);

    assertEquals("1234567890000", cursor.toString());
  }

  @Test
  void should_parse_valid_cursor_string() {
    DateTime result = DateTimeCursor.parse("1234567890000");

    assertNotNull(result);
    assertEquals(1234567890000L, result.getMillis());
    assertEquals(DateTimeZone.UTC, result.getZone());
  }

  @Test
  void should_return_null_when_parsing_null() {
    DateTime result = DateTimeCursor.parse(null);

    assertNull(result);
  }

  @Test
  void should_parse_and_match_original_millis() {
    DateTime original = new DateTime(9999999999L);
    DateTimeCursor cursor = new DateTimeCursor(original);
    DateTime parsed = DateTimeCursor.parse(cursor.toString());

    assertEquals(original.getMillis(), parsed.getMillis());
  }
}
