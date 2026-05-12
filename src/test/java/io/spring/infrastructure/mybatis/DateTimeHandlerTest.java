package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DateTimeHandlerTest {

  @Mock private PreparedStatement ps;
  @Mock private ResultSet rs;
  @Mock private CallableStatement cs;

  private DateTimeHandler handler;

  @BeforeEach
  void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  void should_set_parameter_with_non_null_datetime() throws SQLException {
    DateTime dateTime = new DateTime(2024, 1, 15, 10, 30, 0, DateTimeZone.UTC);

    handler.setParameter(ps, 1, dateTime, JdbcType.TIMESTAMP);

    verify(ps).setTimestamp(eq(1), any(Timestamp.class), any());
  }

  @Test
  void should_set_parameter_with_null_datetime() throws SQLException {
    handler.setParameter(ps, 1, null, JdbcType.TIMESTAMP);

    verify(ps).setTimestamp(eq(1), isNull(), any());
  }

  @Test
  void should_get_result_by_column_name_with_non_null_timestamp() throws SQLException {
    Timestamp timestamp = new Timestamp(1705312200000L);
    when(rs.getTimestamp(eq("created_at"), any())).thenReturn(timestamp);

    DateTime result = handler.getResult(rs, "created_at");

    assertNotNull(result);
    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  void should_get_result_by_column_name_with_null_timestamp() throws SQLException {
    when(rs.getTimestamp(eq("created_at"), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, "created_at");

    assertNull(result);
  }

  @Test
  void should_get_result_by_column_index_with_non_null_timestamp() throws SQLException {
    Timestamp timestamp = new Timestamp(1705312200000L);
    when(rs.getTimestamp(eq(1), any())).thenReturn(timestamp);

    DateTime result = handler.getResult(rs, 1);

    assertNotNull(result);
    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  void should_get_result_by_column_index_with_null_timestamp() throws SQLException {
    when(rs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, 1);

    assertNull(result);
  }

  @Test
  void should_get_result_from_callable_statement_with_non_null_timestamp() throws SQLException {
    Timestamp timestamp = new Timestamp(1705312200000L);
    when(cs.getTimestamp(eq(1), any())).thenReturn(timestamp);

    DateTime result = handler.getResult(cs, 1);

    assertNotNull(result);
    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  void should_get_result_from_callable_statement_with_null_timestamp() throws SQLException {
    when(cs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(cs, 1);

    assertNull(result);
  }
}
