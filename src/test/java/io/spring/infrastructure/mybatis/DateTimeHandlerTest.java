package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DateTimeHandlerTest {

  @Mock private PreparedStatement preparedStatement;
  @Mock private ResultSet resultSet;
  @Mock private CallableStatement callableStatement;

  private DateTimeHandler handler;

  @BeforeEach
  void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  void should_set_timestamp_parameter() throws Exception {
    DateTime dateTime = new DateTime(1234567890000L);

    handler.setParameter(preparedStatement, 1, dateTime, JdbcType.TIMESTAMP);

    verify(preparedStatement).setTimestamp(eq(1), any(Timestamp.class), any());
  }

  @Test
  void should_set_null_when_parameter_is_null() throws Exception {
    handler.setParameter(preparedStatement, 1, null, JdbcType.TIMESTAMP);

    verify(preparedStatement).setTimestamp(eq(1), isNull(), any());
  }

  @Test
  void should_get_result_by_column_name() throws Exception {
    Timestamp ts = new Timestamp(1234567890000L);
    when(resultSet.getTimestamp(eq("created_at"), any())).thenReturn(ts);

    DateTime result = handler.getResult(resultSet, "created_at");

    assertNotNull(result);
    assertEquals(1234567890000L, result.getMillis());
  }

  @Test
  void should_return_null_by_column_name_when_timestamp_is_null() throws Exception {
    when(resultSet.getTimestamp(eq("created_at"), any())).thenReturn(null);

    DateTime result = handler.getResult(resultSet, "created_at");

    assertNull(result);
  }

  @Test
  void should_get_result_by_column_index() throws Exception {
    Timestamp ts = new Timestamp(1234567890000L);
    when(resultSet.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(resultSet, 1);

    assertNotNull(result);
    assertEquals(1234567890000L, result.getMillis());
  }

  @Test
  void should_return_null_by_column_index_when_timestamp_is_null() throws Exception {
    when(resultSet.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(resultSet, 1);

    assertNull(result);
  }

  @Test
  void should_get_result_from_callable_statement() throws Exception {
    Timestamp ts = new Timestamp(1234567890000L);
    when(callableStatement.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(callableStatement, 1);

    assertNotNull(result);
    assertEquals(1234567890000L, result.getMillis());
  }

  @Test
  void should_return_null_from_callable_statement_when_null() throws Exception {
    when(callableStatement.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(callableStatement, 1);

    assertNull(result);
  }
}
