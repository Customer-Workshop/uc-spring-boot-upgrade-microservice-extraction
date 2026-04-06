package io.spring.user.infrastructure.mybatis;

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
    if (parameter != null) {
      ps.setTimestamp(i, new Timestamp(parameter.getMillis()));
    } else {
      ps.setTimestamp(i, null);
    }
  }

  @Override
  public DateTime getResult(ResultSet rs, String columnName) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnName);
    if (timestamp != null) {
      return new DateTime(timestamp.getTime());
    }
    return null;
  }

  @Override
  public DateTime getResult(ResultSet rs, int columnIndex) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnIndex);
    if (timestamp != null) {
      return new DateTime(timestamp.getTime());
    }
    return null;
  }

  @Override
  public DateTime getResult(CallableStatement cs, int columnIndex) throws SQLException {
    Timestamp timestamp = cs.getTimestamp(columnIndex);
    if (timestamp != null) {
      return new DateTime(timestamp.getTime());
    }
    return null;
  }
}
