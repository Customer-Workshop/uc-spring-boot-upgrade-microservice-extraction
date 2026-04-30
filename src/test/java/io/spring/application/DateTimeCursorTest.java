package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  void should_create_cursor_with_datetime() {
    DateTime now = new DateTime();
    DateTimeCursor cursor = new DateTimeCursor(now);

    assertEquals(now, cursor.getData());
  }

  @Test
  void should_convert_to_string_as_millis() {
    DateTime now = new DateTime();
    DateTimeCursor cursor = new DateTimeCursor(now);

    assertEquals(String.valueOf(now.getMillis()), cursor.toString());
  }

  @Test
  void should_parse_cursor_string_to_datetime() {
    DateTime now = new DateTime();
    String cursorString = String.valueOf(now.getMillis());

    DateTime parsed = DateTimeCursor.parse(cursorString);

    assertNotNull(parsed);
    assertEquals(now.getMillis(), parsed.getMillis());
    assertEquals(DateTimeZone.UTC, parsed.getZone());
  }

  @Test
  void should_return_null_when_parsing_null() {
    DateTime parsed = DateTimeCursor.parse(null);

    assertNull(parsed);
  }
}
