package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

  @Mock private PreparedStatement ps;
  @Mock private ResultSet rs;
  @Mock private CallableStatement cs;

  private DateTimeHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  public void should_set_parameter_with_datetime() throws SQLException {
    DateTime now = new DateTime();
    handler.setParameter(ps, 1, now, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(anyInt(), any(Timestamp.class), any());
  }

  @Test
  public void should_set_parameter_with_null() throws SQLException {
    handler.setParameter(ps, 1, null, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(anyInt(), eq(null), any());
  }

  @Test
  public void should_get_result_by_column_name() throws SQLException {
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(rs.getTimestamp(anyString(), any())).thenReturn(ts);

    DateTime result = handler.getResult(rs, "created_at");
    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_get_null_result_by_column_name() throws SQLException {
    when(rs.getTimestamp(anyString(), any())).thenReturn(null);
    DateTime result = handler.getResult(rs, "created_at");
    assertNull(result);
  }

  @Test
  public void should_get_result_by_column_index() throws SQLException {
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(rs.getTimestamp(anyInt(), any())).thenReturn(ts);

    DateTime result = handler.getResult(rs, 1);
    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_get_null_result_by_column_index() throws SQLException {
    when(rs.getTimestamp(anyInt(), any())).thenReturn(null);
    DateTime result = handler.getResult(rs, 1);
    assertNull(result);
  }

  @Test
  public void should_get_result_from_callable_statement() throws SQLException {
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(cs.getTimestamp(anyInt(), any())).thenReturn(ts);

    DateTime result = handler.getResult(cs, 1);
    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_get_null_result_from_callable_statement() throws SQLException {
    when(cs.getTimestamp(anyInt(), any())).thenReturn(null);
    DateTime result = handler.getResult(cs, 1);
    assertNull(result);
  }
}
