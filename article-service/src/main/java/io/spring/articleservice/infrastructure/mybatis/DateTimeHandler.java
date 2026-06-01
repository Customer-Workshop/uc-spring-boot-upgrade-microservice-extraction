package io.spring.articleservice.infrastructure.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.joda.time.DateTime;

@MappedTypes(DateTime.class)
public class DateTimeHandler implements TypeHandler<DateTime> {

  private static Calendar utcCalendar() {
    return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
  }

  @Override
  public void setParameter(PreparedStatement ps, int i, DateTime parameter, JdbcType jdbcType)
      throws SQLException {
    if (parameter != null) {
      ps.setTimestamp(i, new Timestamp(parameter.getMillis()), utcCalendar());
    } else {
      ps.setNull(i, java.sql.Types.TIMESTAMP);
    }
  }

  @Override
  public DateTime getResult(ResultSet rs, String columnName) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnName, utcCalendar());
    return timestamp != null ? new DateTime(timestamp.getTime()) : null;
  }

  @Override
  public DateTime getResult(ResultSet rs, int columnIndex) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnIndex, utcCalendar());
    return timestamp != null ? new DateTime(timestamp.getTime()) : null;
  }

  @Override
  public DateTime getResult(CallableStatement cs, int columnIndex) throws SQLException {
    Timestamp timestamp = cs.getTimestamp(columnIndex, utcCalendar());
    return timestamp != null ? new DateTime(timestamp.getTime()) : null;
  }
}
