package io.spring.comments.config;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.joda.time.DateTime;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {

  @Bean
  public ConfigurationCustomizer mybatisConfigCustomizer() {
    return configuration -> {
      configuration.getTypeHandlerRegistry().register(new DateTimeHandler());
    };
  }

  @MappedTypes(DateTime.class)
  public static class DateTimeHandler extends BaseTypeHandler<DateTime> {

    @Override
    public void setNonNullParameter(
        PreparedStatement ps, int i, DateTime parameter, JdbcType jdbcType) throws SQLException {
      ps.setTimestamp(i, new Timestamp(parameter.getMillis()));
    }

    @Override
    public DateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
      Timestamp timestamp = rs.getTimestamp(columnName);
      return timestamp != null ? new DateTime(timestamp.getTime()) : null;
    }

    @Override
    public DateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
      Timestamp timestamp = rs.getTimestamp(columnIndex);
      return timestamp != null ? new DateTime(timestamp.getTime()) : null;
    }

    @Override
    public DateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
      Timestamp timestamp = cs.getTimestamp(columnIndex);
      return timestamp != null ? new DateTime(timestamp.getTime()) : null;
    }
  }
}
