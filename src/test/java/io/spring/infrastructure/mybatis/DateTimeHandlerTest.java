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
import org.joda.time.DateTime;
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
  void should_set_parameter_with_datetime() throws SQLException {
    DateTime dt = new DateTime(2023, 1, 15, 10, 30, 0);

    handler.setParameter(ps, 1, dt, null);

    verify(ps).setTimestamp(eq(1), any(Timestamp.class), any());
  }

  @Test
  void should_set_parameter_with_null() throws SQLException {
    handler.setParameter(ps, 1, null, null);

    verify(ps).setTimestamp(eq(1), eq(null), any());
  }

  @Test
  void should_get_result_by_column_name() throws SQLException {
    Timestamp ts = new Timestamp(1673784600000L);
    when(rs.getTimestamp(eq("created_at"), any())).thenReturn(ts);

    DateTime result = handler.getResult(rs, "created_at");

    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  void should_get_null_result_by_column_name() throws SQLException {
    when(rs.getTimestamp(eq("created_at"), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, "created_at");

    assertNull(result);
  }

  @Test
  void should_get_result_by_column_index() throws SQLException {
    Timestamp ts = new Timestamp(1673784600000L);
    when(rs.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(rs, 1);

    assertNotNull(result);
  }

  @Test
  void should_get_null_result_by_column_index() throws SQLException {
    when(rs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, 1);

    assertNull(result);
  }

  @Test
  void should_get_result_from_callable_statement() throws SQLException {
    Timestamp ts = new Timestamp(1673784600000L);
    when(cs.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(cs, 1);

    assertNotNull(result);
  }

  @Test
  void should_get_null_from_callable_statement() throws SQLException {
    when(cs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(cs, 1);

    assertNull(result);
  }
}
