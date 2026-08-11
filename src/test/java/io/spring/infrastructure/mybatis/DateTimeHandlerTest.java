package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateTimeHandlerTest {

  private DateTimeHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  public void should_set_timestamp_parameter() throws Exception {
    PreparedStatement ps = mock(PreparedStatement.class);

    handler.setParameter(ps, 1, new DateTime(1000L), null);

    verify(ps).setTimestamp(eq(1), eq(new Timestamp(1000L)), any(Calendar.class));
  }

  @Test
  public void should_set_null_parameter_for_null_date() throws Exception {
    PreparedStatement ps = mock(PreparedStatement.class);

    handler.setParameter(ps, 1, null, null);

    verify(ps).setTimestamp(eq(1), isNull(), any(Calendar.class));
  }

  @Test
  public void should_read_date_by_column_name() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(eq("created_at"), any(Calendar.class))).thenReturn(new Timestamp(1000L));

    assertEquals(1000L, handler.getResult(rs, "created_at").getMillis());
  }

  @Test
  public void should_return_null_when_column_value_is_null() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(eq("created_at"), any(Calendar.class))).thenReturn(null);

    assertNull(handler.getResult(rs, "created_at"));
  }

  @Test
  public void should_read_date_by_column_index() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(eq(2), any(Calendar.class))).thenReturn(new Timestamp(2000L));

    assertEquals(2000L, handler.getResult(rs, 2).getMillis());
  }

  @Test
  public void should_return_null_for_null_column_index_value() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(eq(2), any(Calendar.class))).thenReturn(null);

    assertNull(handler.getResult(rs, 2));
  }

  @Test
  public void should_read_date_from_callable_statement() throws Exception {
    CallableStatement cs = mock(CallableStatement.class);
    when(cs.getTimestamp(eq(3), any(Calendar.class))).thenReturn(new Timestamp(3000L));

    assertEquals(3000L, handler.getResult(cs, 3).getMillis());
  }

  @Test
  public void should_return_null_from_callable_statement_for_null_value() throws Exception {
    CallableStatement cs = mock(CallableStatement.class);
    when(cs.getTimestamp(eq(3), any(Calendar.class))).thenReturn(null);

    assertNull(handler.getResult(cs, 3));
  }
}
