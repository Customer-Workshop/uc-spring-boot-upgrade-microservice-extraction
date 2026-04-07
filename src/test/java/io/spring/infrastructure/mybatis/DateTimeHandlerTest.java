package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
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
    DateTime dt = new DateTime(2024, 1, 1, 0, 0, 0);
    handler.setParameter(ps, 1, dt, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(eq(1), any(Timestamp.class), any(Calendar.class));
  }

  @Test
  public void should_set_parameter_with_null() throws SQLException {
    handler.setParameter(ps, 1, null, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(eq(1), isNull(), any(Calendar.class));
  }

  @Test
  public void should_get_result_by_column_name() throws SQLException {
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(rs.getTimestamp(eq("created_at"), any(Calendar.class))).thenReturn(ts);

    DateTime result = handler.getResult(rs, "created_at");

    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_return_null_for_null_column_name() throws SQLException {
    when(rs.getTimestamp(eq("created_at"), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(rs, "created_at");

    assertNull(result);
  }

  @Test
  public void should_get_result_by_column_index() throws SQLException {
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(rs.getTimestamp(eq(1), any(Calendar.class))).thenReturn(ts);

    DateTime result = handler.getResult(rs, 1);

    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_return_null_for_null_column_index() throws SQLException {
    when(rs.getTimestamp(eq(1), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(rs, 1);

    assertNull(result);
  }

  @Test
  public void should_get_result_from_callable_statement() throws SQLException {
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(cs.getTimestamp(eq(1), any(Calendar.class))).thenReturn(ts);

    DateTime result = handler.getResult(cs, 1);

    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  public void should_return_null_from_callable_statement() throws SQLException {
    when(cs.getTimestamp(eq(1), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(cs, 1);

    assertNull(result);
  }
}
