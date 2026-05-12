package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  void should_create_from_datetime() {
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
  void should_parse_from_string() {
    DateTime original = new DateTime().withZone(DateTimeZone.UTC);
    String millis = String.valueOf(original.getMillis());

    DateTime parsed = DateTimeCursor.parse(millis);

    assertNotNull(parsed);
    assertEquals(original.getMillis(), parsed.getMillis());
  }

  @Test
  void should_return_null_when_parsing_null() {
    DateTime parsed = DateTimeCursor.parse(null);
    assertNull(parsed);
  }

  @Test
  void should_throw_when_parsing_invalid_string() {
    assertThrows(NumberFormatException.class, () -> DateTimeCursor.parse("not-a-number"));
  }
}
