package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  void should_create_cursor_from_datetime() {
    DateTime dt = new DateTime(2023, 1, 15, 10, 30, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dt);

    assertNotNull(cursor);
    assertEquals(dt, cursor.getData());
  }

  @Test
  void should_convert_to_string() {
    DateTime dt = new DateTime(2023, 1, 15, 10, 30, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dt);

    String str = cursor.toString();

    assertEquals(String.valueOf(dt.getMillis()), str);
  }

  @Test
  void should_parse_cursor_string() {
    DateTime original = new DateTime(2023, 1, 15, 10, 30, 0, DateTimeZone.UTC);
    String millis = String.valueOf(original.getMillis());

    DateTime parsed = DateTimeCursor.parse(millis);

    assertNotNull(parsed);
    assertEquals(original.getMillis(), parsed.getMillis());
  }

  @Test
  void should_return_null_when_parsing_null() {
    DateTime result = DateTimeCursor.parse(null);

    assertNull(result);
  }
}
