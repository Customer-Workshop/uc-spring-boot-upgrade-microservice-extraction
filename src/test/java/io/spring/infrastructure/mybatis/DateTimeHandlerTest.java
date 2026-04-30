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
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DateTimeHandlerTest {

  private DateTimeHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  public void should_set_parameter_with_non_null_datetime() throws Exception {
    PreparedStatement ps = Mockito.mock(PreparedStatement.class);
    DateTime now = new DateTime(2024, 1, 15, 10, 30, 0, DateTimeZone.UTC);

    handler.setParameter(ps, 1, now, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(eq(1), any(Timestamp.class), any());
  }

  @Test
  public void should_set_parameter_with_null_datetime() throws Exception {
    PreparedStatement ps = Mockito.mock(PreparedStatement.class);

    handler.setParameter(ps, 1, null, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(eq(1), eq(null), any());
  }

  @Test
  public void should_get_result_by_column_name() throws Exception {
    ResultSet rs = Mockito.mock(ResultSet.class);
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(rs.getTimestamp(eq("created_at"), any())).thenReturn(ts);

    DateTime result = handler.getResult(rs, "created_at");
    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_return_null_for_null_column_name_result() throws Exception {
    ResultSet rs = Mockito.mock(ResultSet.class);
    when(rs.getTimestamp(eq("created_at"), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, "created_at");
    assertNull(result);
  }

  @Test
  public void should_get_result_by_column_index() throws Exception {
    ResultSet rs = Mockito.mock(ResultSet.class);
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(rs.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(rs, 1);
    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_return_null_for_null_column_index_result() throws Exception {
    ResultSet rs = Mockito.mock(ResultSet.class);
    when(rs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, 1);
    assertNull(result);
  }

  @Test
  public void should_get_result_from_callable_statement() throws Exception {
    CallableStatement cs = Mockito.mock(CallableStatement.class);
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(cs.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(cs, 1);
    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_return_null_from_callable_statement_with_null() throws Exception {
    CallableStatement cs = Mockito.mock(CallableStatement.class);
    when(cs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(cs, 1);
    assertNull(result);
  }
}
