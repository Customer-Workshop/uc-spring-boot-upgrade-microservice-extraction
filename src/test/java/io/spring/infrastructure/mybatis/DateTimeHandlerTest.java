package io.spring.infrastructure.mybatis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DateTimeHandlerTest {

  private final DateTimeHandler handler = new DateTimeHandler();

  @Mock private PreparedStatement preparedStatement;
  @Mock private CallableStatement callableStatement;
  @Mock private ResultSet resultSet;

  @Test
  public void should_set_timestamp_parameter() throws SQLException {
    DateTime now = new DateTime();

    handler.setParameter(preparedStatement, 1, now, null);

    verify(preparedStatement)
        .setTimestamp(eq(1), eq(new Timestamp(now.getMillis())), any(Calendar.class));
  }

  @Test
  public void should_set_null_parameter() throws SQLException {
    handler.setParameter(preparedStatement, 1, null, null);

    verify(preparedStatement).setTimestamp(eq(1), isNull(), any(Calendar.class));
  }

  @Test
  public void should_read_result_by_column_name() throws SQLException {
    DateTime now = new DateTime();
    when(resultSet.getTimestamp(eq("created_at"), any(Calendar.class)))
        .thenReturn(new Timestamp(now.getMillis()));

    Assertions.assertEquals(
        now.getMillis(), handler.getResult(resultSet, "created_at").getMillis());
  }

  @Test
  public void should_read_null_result_by_column_name() throws SQLException {
    when(resultSet.getTimestamp(eq("created_at"), any(Calendar.class))).thenReturn(null);

    Assertions.assertNull(handler.getResult(resultSet, "created_at"));
  }

  @Test
  public void should_read_result_by_column_index() throws SQLException {
    DateTime now = new DateTime();
    when(resultSet.getTimestamp(anyInt(), any(Calendar.class)))
        .thenReturn(new Timestamp(now.getMillis()));

    Assertions.assertEquals(now.getMillis(), handler.getResult(resultSet, 1).getMillis());
  }

  @Test
  public void should_read_null_result_by_column_index() throws SQLException {
    when(resultSet.getTimestamp(anyInt(), any(Calendar.class))).thenReturn(null);

    Assertions.assertNull(handler.getResult(resultSet, 1));
  }

  @Test
  public void should_read_result_from_callable_statement() throws SQLException {
    DateTime now = new DateTime();
    when(callableStatement.getTimestamp(anyInt(), any(Calendar.class)))
        .thenReturn(new Timestamp(now.getMillis()));

    Assertions.assertEquals(now.getMillis(), handler.getResult(callableStatement, 1).getMillis());
  }

  @Test
  public void should_read_null_result_from_callable_statement() throws SQLException {
    when(callableStatement.getTimestamp(anyInt(), any(Calendar.class))).thenReturn(null);

    Assertions.assertNull(handler.getResult(callableStatement, 1));
  }
}
