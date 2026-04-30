package io.spring.comments.infrastructure.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.joda.time.DateTime;

@MappedTypes(DateTime.class)
public class DateTimeHandler implements TypeHandler<DateTime> {

  @Override
  public void setParameter(PreparedStatement ps, int i, DateTime parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setTimestamp(i, parameter != null ? new Timestamp(parameter.getMillis()) : null);
  }

  @Override
  public DateTime getResult(ResultSet rs, String columnName) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnName);
    return timestamp != null ? new DateTime(timestamp.getTime()) : null;
  }

  @Override
  public DateTime getResult(ResultSet rs, int columnIndex) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnIndex);
    return timestamp != null ? new DateTime(timestamp.getTime()) : null;
  }

  @Override
  public DateTime getResult(CallableStatement cs, int columnIndex) throws SQLException {
    Timestamp timestamp = cs.getTimestamp(columnIndex);
    return timestamp != null ? new DateTime(timestamp.getTime()) : null;
  }
}
