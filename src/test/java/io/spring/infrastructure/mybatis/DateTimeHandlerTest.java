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

  @Mock private PreparedStatement preparedStatement;
  @Mock private ResultSet resultSet;
  @Mock private CallableStatement callableStatement;

  private DateTimeHandler dateTimeHandler;

  @BeforeEach
  public void setUp() {
    dateTimeHandler = new DateTimeHandler();
  }

  @Test
  public void should_set_parameter_with_datetime() throws SQLException {
    DateTime dateTime = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
    dateTimeHandler.setParameter(preparedStatement, 1, dateTime, JdbcType.TIMESTAMP);
    verify(preparedStatement).setTimestamp(eq(1), any(Timestamp.class), any());
  }

  @Test
  public void should_set_parameter_with_null() throws SQLException {
    dateTimeHandler.setParameter(preparedStatement, 1, null, JdbcType.TIMESTAMP);
    verify(preparedStatement).setTimestamp(eq(1), isNull(), any());
  }

  @Test
  public void should_get_result_by_column_name() throws SQLException {
    Timestamp ts = new Timestamp(1686825000000L);
    when(resultSet.getTimestamp(eq("created_at"), any())).thenReturn(ts);

    DateTime result = dateTimeHandler.getResult(resultSet, "created_at");

    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_get_null_result_by_column_name() throws SQLException {
    when(resultSet.getTimestamp(eq("created_at"), any())).thenReturn(null);

    DateTime result = dateTimeHandler.getResult(resultSet, "created_at");

    assertNull(result);
  }

  @Test
  public void should_get_result_by_column_index() throws SQLException {
    Timestamp ts = new Timestamp(1686825000000L);
    when(resultSet.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = dateTimeHandler.getResult(resultSet, 1);

    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_get_null_result_by_column_index() throws SQLException {
    when(resultSet.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = dateTimeHandler.getResult(resultSet, 1);

    assertNull(result);
  }

  @Test
  public void should_get_result_from_callable_statement() throws SQLException {
    Timestamp ts = new Timestamp(1686825000000L);
    when(callableStatement.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = dateTimeHandler.getResult(callableStatement, 1);

    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_get_null_result_from_callable_statement() throws SQLException {
    when(callableStatement.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = dateTimeHandler.getResult(callableStatement, 1);

    assertNull(result);
  }
}
