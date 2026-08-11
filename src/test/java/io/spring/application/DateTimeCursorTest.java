package io.spring.application;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void should_round_trip_cursor() {
    DateTime now = new DateTime();

    DateTime parsed = DateTimeCursor.parse(new DateTimeCursor(now).toString());

    Assertions.assertEquals(now.getMillis(), parsed.getMillis());
    Assertions.assertEquals(DateTimeZone.UTC, parsed.getZone());
  }

  @Test
  public void should_parse_null_cursor_as_null() {
    Assertions.assertNull(DateTimeCursor.parse(null));
  }

  @Test
  public void should_reject_cursor_with_special_characters() {
    Assertions.assertThrows(NumberFormatException.class, () -> DateTimeCursor.parse("🚀"));
    Assertions.assertThrows(
        NumberFormatException.class, () -> DateTimeCursor.parse("1' OR '1'='1"));
    Assertions.assertThrows(NumberFormatException.class, () -> DateTimeCursor.parse(""));
    Assertions.assertThrows(NumberFormatException.class, () -> DateTimeCursor.parse("12.5"));
  }
}
