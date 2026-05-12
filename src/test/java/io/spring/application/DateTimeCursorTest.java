package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void should_create_cursor_with_datetime() {
    DateTime dateTime = new DateTime(2023, 1, 15, 10, 30, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);

    assertEquals(dateTime, cursor.getData());
  }

  @Test
  public void should_convert_to_string_as_millis() {
    DateTime dateTime = new DateTime(2023, 1, 15, 10, 30, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);

    assertEquals(String.valueOf(dateTime.getMillis()), cursor.toString());
  }

  @Test
  public void should_parse_valid_cursor_string() {
    DateTime original = new DateTime(2023, 1, 15, 10, 30, 0, DateTimeZone.UTC);
    String cursorStr = String.valueOf(original.getMillis());

    DateTime result = DateTimeCursor.parse(cursorStr);

    assertNotNull(result);
    assertEquals(original.getMillis(), result.getMillis());
  }

  @Test
  public void should_return_null_for_null_cursor() {
    DateTime result = DateTimeCursor.parse(null);

    assertNull(result);
  }
}
