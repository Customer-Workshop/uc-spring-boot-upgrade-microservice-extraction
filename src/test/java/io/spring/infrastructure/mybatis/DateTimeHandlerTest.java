package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class DateTimeHandlerTest {
  private final DateTimeHandler handler = new DateTimeHandler();
  private final DateTime value = new DateTime(123456789L);

  @Test
  void writesAndReadsTimestampsIncludingNulls() throws Exception {
    PreparedStatement ps = mock(PreparedStatement.class);
    handler.setParameter(ps, 1, value, JdbcType.TIMESTAMP);
    handler.setParameter(ps, 2, null, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(eq(1), any(), any());

    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(eq("created"), any(Calendar.class)))
        .thenReturn(new Timestamp(value.getMillis()));
    when(rs.getTimestamp(anyInt(), any(Calendar.class))).thenReturn(null);
    assertEquals(value, handler.getResult(rs, "created"));
    assertNull(handler.getResult(rs, 1));

    CallableStatement cs = mock(CallableStatement.class);
    when(cs.getTimestamp(anyInt(), any(Calendar.class)))
        .thenReturn(new Timestamp(value.getMillis()));
    assertEquals(value, handler.getResult(cs, 1));
  }
}
