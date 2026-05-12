package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  void should_create_cursor_with_datetime() {
    DateTime dt = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dt);

    assertEquals(dt, cursor.getData());
  }

  @Test
  void should_return_millis_as_string() {
    DateTime dt = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dt);

    assertEquals(String.valueOf(dt.getMillis()), cursor.toString());
  }

  @Test
  void should_parse_null_cursor_to_null() {
    DateTime result = DateTimeCursor.parse(null);
    assertNull(result);
  }

  @Test
  void should_parse_valid_millis_string() {
    DateTime original = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
    String millis = String.valueOf(original.getMillis());

    DateTime result = DateTimeCursor.parse(millis);

    assertNotNull(result);
    assertEquals(original.getMillis(), result.getMillis());
    assertEquals(DateTimeZone.UTC, result.getZone());
  }

  @Test
  void should_throw_on_invalid_cursor_string() {
    assertThrows(NumberFormatException.class, () -> DateTimeCursor.parse("not-a-number"));
  }
}
