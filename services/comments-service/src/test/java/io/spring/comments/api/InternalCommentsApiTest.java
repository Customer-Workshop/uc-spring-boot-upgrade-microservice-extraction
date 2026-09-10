package io.spring.comments.api;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.comments.JacksonCustomizations;
import io.spring.comments.api.security.WebSecurityConfig;
import io.spring.comments.application.CommentQueryService;
import io.spring.comments.application.CursorPageParameter;
import io.spring.comments.application.CursorPager;
import io.spring.comments.application.CursorPager.Direction;
import io.spring.comments.application.data.CommentData;
import io.spring.comments.application.data.ProfileData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalCommentsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class InternalCommentsApiTest extends TestWithCurrentUser {

  @MockBean private CommentQueryService commentQueryService;

  @Autowired private MockMvc mvc;

  private CommentData commentData;
  private DateTime createdAt;

  @BeforeEach
  public void setUp() throws Exception {
    RestAssuredMockMvc.mockMvc(mvc);
    super.setUp();
    createdAt = new DateTime();
    commentData =
        new CommentData(
            UUID.randomUUID().toString(),
            "content",
            "article-1",
            createdAt,
            createdAt,
            new ProfileData(user.getId(), username, "", defaultAvatar, false));
  }

  @Test
  public void should_return_cursor_page_of_comments() {
    when(commentQueryService.findByArticleIdWithCursor(eq("article-1"), isNull(), any()))
        .thenReturn(
            new CursorPager<>(new ArrayList<>(Arrays.asList(commentData)), Direction.NEXT, true));

    RestAssuredMockMvc.when()
        .get("/internal/comments?articleId=article-1&limit=1")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("comments[0].id", equalTo(commentData.getId()))
        .body("comments[0].author.username", equalTo(username))
        .body("hasNext", equalTo(true))
        .body("hasPrevious", equalTo(false))
        .body("startCursor", equalTo(String.valueOf(createdAt.getMillis())))
        .body("endCursor", equalTo(String.valueOf(createdAt.getMillis())));
  }

  @Test
  public void should_return_empty_page_and_null_cursors() {
    when(commentQueryService.findByArticleIdWithCursor(eq("article-9"), isNull(), any()))
        .thenReturn(new CursorPager<>(new ArrayList<>(), Direction.NEXT, false));

    RestAssuredMockMvc.when()
        .get("/internal/comments?articleId=article-9")
        .then()
        .statusCode(200)
        .body("comments.size()", equalTo(0))
        .body("hasNext", equalTo(false))
        .body("startCursor", equalTo(null))
        .body("endCursor", equalTo(null));
  }

  @Test
  public void should_pass_cursor_limit_and_direction_to_the_query_service() {
    when(commentQueryService.findByArticleIdWithCursor(eq("article-1"), isNull(), any()))
        .thenReturn(new CursorPager<>(new ArrayList<>(), Direction.PREV, false));

    RestAssuredMockMvc.when()
        .get("/internal/comments?articleId=article-1&cursor=123456789&limit=5&direction=PREV")
        .then()
        .statusCode(200);

    ArgumentCaptor<CursorPageParameter<DateTime>> captor =
        ArgumentCaptor.forClass(CursorPageParameter.class);
    Mockito.verify(commentQueryService)
        .findByArticleIdWithCursor(eq("article-1"), isNull(), captor.capture());
    CursorPageParameter<DateTime> page = captor.getValue();
    org.junit.jupiter.api.Assertions.assertEquals(5, page.getLimit());
    org.junit.jupiter.api.Assertions.assertEquals(Direction.PREV, page.getDirection());
    org.junit.jupiter.api.Assertions.assertEquals(123456789L, page.getCursor().getMillis());
  }
}
