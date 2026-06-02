package io.spring.article.application;

import org.joda.time.DateTime;

public class DateTimeCursor {
  private DateTime data;

  public DateTimeCursor(DateTime data) {
    this.data = data;
  }

  public DateTime getData() {
    return data;
  }

  @Override
  public String toString() {
    return String.valueOf(data.getMillis());
  }
}
