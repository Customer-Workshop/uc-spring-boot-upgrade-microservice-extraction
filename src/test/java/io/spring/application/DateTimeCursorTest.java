package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void should_create_cursor_and_get_data() {
    DateTime now = new DateTime();
    DateTimeCursor cursor = new DateTimeCursor(now);
    assertEquals(now, cursor.getData());
  }

  @Test
  public void should_convert_to_string_as_millis() {
    DateTime dt = new DateTime(1686825000000L, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dt);
    assertEquals("1686825000000", cursor.toString());
  }

  @Test
  public void should_parse_cursor_string() {
    DateTime result = DateTimeCursor.parse("1686825000000");
    assertNotNull(result);
    assertEquals(1686825000000L, result.getMillis());
  }

  @Test
  public void should_parse_null_to_null() {
    DateTime result = DateTimeCursor.parse(null);
    assertNull(result);
  }
}
