package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
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

  private DateTimeHandler handler;

  @Mock private PreparedStatement preparedStatement;
  @Mock private ResultSet resultSet;
  @Mock private CallableStatement callableStatement;

  @BeforeEach
  void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  void should_set_parameter_with_datetime() throws SQLException {
    DateTime now = new DateTime(2023, 1, 15, 10, 30, 0, DateTimeZone.UTC);

    handler.setParameter(preparedStatement, 1, now, JdbcType.TIMESTAMP);

    verify(preparedStatement).setTimestamp(eq(1), any(Timestamp.class), any(Calendar.class));
  }

  @Test
  void should_set_parameter_with_null_datetime() throws SQLException {
    handler.setParameter(preparedStatement, 1, null, JdbcType.TIMESTAMP);

    verify(preparedStatement).setTimestamp(eq(1), eq(null), any(Calendar.class));
  }

  @Test
  void should_get_result_by_column_name() throws SQLException {
    Timestamp timestamp = new Timestamp(1673784600000L);
    when(resultSet.getTimestamp(eq("created_at"), any(Calendar.class))).thenReturn(timestamp);

    DateTime result = handler.getResult(resultSet, "created_at");

    assertNotNull(result);
    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  void should_get_null_result_by_column_name() throws SQLException {
    when(resultSet.getTimestamp(eq("created_at"), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(resultSet, "created_at");

    assertNull(result);
  }

  @Test
  void should_get_result_by_column_index() throws SQLException {
    Timestamp timestamp = new Timestamp(1673784600000L);
    when(resultSet.getTimestamp(eq(1), any(Calendar.class))).thenReturn(timestamp);

    DateTime result = handler.getResult(resultSet, 1);

    assertNotNull(result);
    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  void should_get_null_result_by_column_index() throws SQLException {
    when(resultSet.getTimestamp(eq(1), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(resultSet, 1);

    assertNull(result);
  }

  @Test
  void should_get_result_from_callable_statement() throws SQLException {
    Timestamp timestamp = new Timestamp(1673784600000L);
    when(callableStatement.getTimestamp(eq(1), any(Calendar.class))).thenReturn(timestamp);

    DateTime result = handler.getResult(callableStatement, 1);

    assertNotNull(result);
    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  void should_get_null_result_from_callable_statement() throws SQLException {
    when(callableStatement.getTimestamp(eq(1), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(callableStatement, 1);

    assertNull(result);
  }
}
