package io.spring.graphql.types;

import static org.junit.jupiter.api.Assertions.*;

import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultPageInfo;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class GraphQLTypesTest {

  @Test
  void should_build_article_with_builder() {
    Article article =
        Article.newBuilder()
            .slug("test-slug")
            .title("Test Title")
            .description("description")
            .body("body")
            .favorited(true)
            .favoritesCount(5)
            .tagList(Arrays.asList("java", "spring"))
            .createdAt("2023-01-01T00:00:00Z")
            .updatedAt("2023-01-02T00:00:00Z")
            .build();

    assertEquals("test-slug", article.getSlug());
    assertEquals("Test Title", article.getTitle());
    assertEquals("description", article.getDescription());
    assertEquals("body", article.getBody());
    assertTrue(article.getFavorited());
    assertEquals(5, article.getFavoritesCount());
    assertEquals(2, article.getTagList().size());
    assertEquals("2023-01-01T00:00:00Z", article.getCreatedAt());
    assertEquals("2023-01-02T00:00:00Z", article.getUpdatedAt());
  }

  @Test
  void should_build_article_with_setters() {
    Article article = new Article();
    article.setSlug("test-slug");
    article.setTitle("Test Title");
    article.setDescription("description");
    article.setBody("body");
    article.setFavorited(false);
    article.setFavoritesCount(0);
    article.setTagList(Arrays.asList("java"));
    article.setCreatedAt("2023-01-01");
    article.setUpdatedAt("2023-01-02");
    article.setAuthor(Profile.newBuilder().username("author").build());
    article.setComments(CommentsConnection.newBuilder().build());

    assertEquals("test-slug", article.getSlug());
    assertNotNull(article.getAuthor());
    assertNotNull(article.getComments());
    assertNotNull(article.toString());
  }

  @Test
  void should_test_article_equals_and_hashCode() {
    Article a1 = Article.newBuilder().slug("slug").title("Title").build();
    Article a2 = Article.newBuilder().slug("slug").title("Title").build();
    Article a3 = Article.newBuilder().slug("other").title("Other").build();

    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());
    assertNotEquals(a1, a3);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "string");
    assertEquals(a1, a1);
  }

  @Test
  void should_build_article_with_all_args_constructor() {
    Profile author = Profile.newBuilder().username("author").build();
    CommentsConnection comments = CommentsConnection.newBuilder().build();
    Article article =
        new Article(
            author,
            "body",
            comments,
            "created",
            "desc",
            true,
            3,
            "slug",
            Arrays.asList("tag"),
            "title",
            "updated");

    assertEquals("body", article.getBody());
    assertEquals("slug", article.getSlug());
    assertEquals("title", article.getTitle());
    assertEquals(author, article.getAuthor());
  }

  @Test
  void should_build_article_edge() {
    Article article = Article.newBuilder().slug("slug").build();
    ArticleEdge edge = ArticleEdge.newBuilder().cursor("cursor1").node(article).build();

    assertEquals("cursor1", edge.getCursor());
    assertEquals("slug", edge.getNode().getSlug());
    assertNotNull(edge.toString());

    ArticleEdge edge2 = new ArticleEdge("cursor1", article);
    assertEquals(edge, edge2);
    assertEquals(edge.hashCode(), edge2.hashCode());

    ArticleEdge empty = new ArticleEdge();
    empty.setCursor("c");
    empty.setNode(article);
    assertEquals("c", empty.getCursor());
  }

  @Test
  void should_build_articles_connection() {
    graphql.relay.PageInfo pageInfo =
        new DefaultPageInfo(
            new DefaultConnectionCursor("start"), new DefaultConnectionCursor("end"), false, true);
    ArticleEdge edge = ArticleEdge.newBuilder().cursor("c").build();
    ArticlesConnection conn =
        ArticlesConnection.newBuilder().pageInfo(pageInfo).edges(Arrays.asList(edge)).build();

    assertNotNull(conn.getPageInfo());
    assertEquals(1, conn.getEdges().size());
    assertNotNull(conn.toString());

    ArticlesConnection conn2 =
        ArticlesConnection.newBuilder().pageInfo(pageInfo).edges(Arrays.asList(edge)).build();
    assertEquals(conn, conn2);
    assertEquals(conn.hashCode(), conn2.hashCode());

    ArticlesConnection empty = new ArticlesConnection();
    empty.setEdges(Collections.emptyList());
    empty.setPageInfo(pageInfo);
    assertNotNull(empty.getPageInfo());
  }

  @Test
  void should_build_comment() {
    Comment comment =
        Comment.newBuilder()
            .id("comment-1")
            .body("Great article!")
            .createdAt("2023-01-01")
            .updatedAt("2023-01-02")
            .build();

    assertEquals("comment-1", comment.getId());
    assertEquals("Great article!", comment.getBody());
    assertEquals("2023-01-01", comment.getCreatedAt());
    assertEquals("2023-01-02", comment.getUpdatedAt());
    assertNotNull(comment.toString());

    Comment c2 = new Comment("comment-1", null, null, "Great article!", "2023-01-01", "2023-01-02");
    assertEquals(comment.getId(), c2.getId());

    Comment c3 = new Comment();
    c3.setId("id");
    c3.setBody("body");
    c3.setCreatedAt("c");
    c3.setUpdatedAt("u");
    c3.setAuthor(Profile.newBuilder().build());
    c3.setArticle(Article.newBuilder().build());
    assertNotNull(c3.getAuthor());
    assertNotNull(c3.getArticle());
    assertEquals(c3, c3);
    assertNotEquals(comment, c3);
  }

  @Test
  void should_build_comment_edge() {
    Comment comment = Comment.newBuilder().id("c1").build();
    CommentEdge edge = CommentEdge.newBuilder().cursor("cursor").node(comment).build();

    assertEquals("cursor", edge.getCursor());
    assertEquals("c1", edge.getNode().getId());

    CommentEdge edge2 = new CommentEdge("cursor", comment);
    assertEquals(edge, edge2);

    CommentEdge empty = new CommentEdge();
    empty.setCursor("c");
    empty.setNode(comment);
    assertNotNull(empty.toString());
  }

  @Test
  void should_build_comments_connection() {
    graphql.relay.PageInfo pageInfo = new DefaultPageInfo(null, null, false, false);
    CommentsConnection conn =
        CommentsConnection.newBuilder().pageInfo(pageInfo).edges(Collections.emptyList()).build();

    assertNotNull(conn.getPageInfo());
    assertTrue(conn.getEdges().isEmpty());
    assertNotNull(conn.toString());

    CommentsConnection conn2 =
        CommentsConnection.newBuilder().pageInfo(pageInfo).edges(Collections.emptyList()).build();
    assertEquals(conn, conn2);
    assertEquals(conn.hashCode(), conn2.hashCode());

    CommentsConnection empty = new CommentsConnection();
    empty.setEdges(Collections.emptyList());
    empty.setPageInfo(pageInfo);
    assertNotNull(empty.getEdges());
  }

  @Test
  void should_build_user() {
    User user =
        User.newBuilder().email("test@test.com").username("testuser").token("jwt-token").build();

    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("jwt-token", user.getToken());
    assertNotNull(user.toString());

    User u2 = new User("test@test.com", null, "jwt-token", "testuser");
    assertEquals(user.getEmail(), u2.getEmail());

    User u3 = new User();
    u3.setEmail("e");
    u3.setUsername("u");
    u3.setToken("t");
    u3.setProfile(Profile.newBuilder().build());
    assertNotNull(u3.getProfile());
    assertEquals(u3, u3);
    assertNotEquals(user, u3);
  }

  @Test
  void should_build_profile() {
    Profile profile =
        Profile.newBuilder()
            .username("testuser")
            .bio("bio")
            .image("image.png")
            .following(true)
            .build();

    assertEquals("testuser", profile.getUsername());
    assertEquals("bio", profile.getBio());
    assertEquals("image.png", profile.getImage());
    assertTrue(profile.getFollowing());
    assertNotNull(profile.toString());

    Profile p2 =
        Profile.newBuilder()
            .username("testuser")
            .bio("bio")
            .image("image.png")
            .following(true)
            .build();
    assertEquals(profile, p2);
    assertEquals(profile.hashCode(), p2.hashCode());

    Profile p3 = new Profile("testuser", "bio", true, "image.png", null, null, null);
    assertEquals("testuser", p3.getUsername());

    Profile p4 = new Profile();
    p4.setUsername("u");
    p4.setBio("b");
    p4.setImage("i");
    p4.setFollowing(false);
    p4.setArticles(ArticlesConnection.newBuilder().build());
    p4.setFavorites(ArticlesConnection.newBuilder().build());
    p4.setFeed(ArticlesConnection.newBuilder().build());
    assertNotNull(p4.getArticles());
    assertNotNull(p4.getFavorites());
    assertNotNull(p4.getFeed());
  }

  @Test
  void should_build_create_article_input() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("description")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    assertEquals("Test Title", input.getTitle());
    assertEquals("description", input.getDescription());
    assertEquals("body", input.getBody());
    assertEquals(2, input.getTagList().size());
    assertNotNull(input.toString());

    CreateArticleInput i2 =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("description")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();
    assertEquals(input, i2);
    assertEquals(input.hashCode(), i2.hashCode());

    CreateArticleInput i3 =
        new CreateArticleInput("body", "description", Arrays.asList("java"), "Test Title");
    assertEquals("body", i3.getBody());

    CreateArticleInput i4 = new CreateArticleInput();
    i4.setTitle("t");
    i4.setDescription("d");
    i4.setBody("b");
    i4.setTagList(Collections.emptyList());
    assertEquals("t", i4.getTitle());
  }

  @Test
  void should_build_update_article_input() {
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("New Body")
            .description("New Desc")
            .build();

    assertEquals("New Title", input.getTitle());
    assertEquals("New Body", input.getBody());
    assertEquals("New Desc", input.getDescription());
    assertNotNull(input.toString());

    UpdateArticleInput i2 =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("New Body")
            .description("New Desc")
            .build();
    assertEquals(input, i2);

    UpdateArticleInput i3 = new UpdateArticleInput("New Body", "New Desc", "New Title");
    assertEquals("New Title", i3.getTitle());

    UpdateArticleInput i4 = new UpdateArticleInput();
    i4.setTitle("t");
    i4.setBody("b");
    i4.setDescription("d");
    assertEquals("t", i4.getTitle());
  }

  @Test
  void should_build_create_user_input() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("password123")
            .build();

    assertEquals("test@test.com", input.getEmail());
    assertEquals("testuser", input.getUsername());
    assertEquals("password123", input.getPassword());
    assertNotNull(input.toString());

    CreateUserInput i2 =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("password123")
            .build();
    assertEquals(input, i2);

    CreateUserInput i3 = new CreateUserInput("test@test.com", "password123", "testuser");
    assertEquals("test@test.com", i3.getEmail());

    CreateUserInput i4 = new CreateUserInput();
    i4.setEmail("e");
    i4.setUsername("u");
    i4.setPassword("p");
    assertEquals("e", i4.getEmail());
  }

  @Test
  void should_build_update_user_input() {
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new.png")
            .build();

    assertEquals("new@test.com", input.getEmail());
    assertEquals("newuser", input.getUsername());
    assertEquals("newpass", input.getPassword());
    assertEquals("new bio", input.getBio());
    assertEquals("new.png", input.getImage());
    assertNotNull(input.toString());

    UpdateUserInput i2 =
        UpdateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new.png")
            .build();
    assertEquals(input, i2);
    assertEquals(input.hashCode(), i2.hashCode());

    UpdateUserInput i3 =
        new UpdateUserInput("new@test.com", "newuser", "newpass", "new.png", "new bio");
    assertEquals("new@test.com", i3.getEmail());

    UpdateUserInput i4 = new UpdateUserInput();
    i4.setEmail("e");
    i4.setUsername("u");
    i4.setPassword("p");
    i4.setBio("b");
    i4.setImage("i");
    assertEquals("e", i4.getEmail());
  }

  @Test
  void should_build_deletion_status() {
    DeletionStatus status = DeletionStatus.newBuilder().success(true).build();

    assertTrue(status.getSuccess());
    assertNotNull(status.toString());

    DeletionStatus s2 = DeletionStatus.newBuilder().success(true).build();
    assertEquals(status, s2);
    assertEquals(status.hashCode(), s2.hashCode());

    DeletionStatus s3 = new DeletionStatus(true);
    assertTrue(s3.getSuccess());

    DeletionStatus s4 = new DeletionStatus();
    s4.setSuccess(false);
    assertFalse(s4.getSuccess());
  }

  @Test
  void should_build_error() {
    ErrorItem item = ErrorItem.newBuilder().key("email").value(Arrays.asList("invalid")).build();
    Error error = Error.newBuilder().message("BAD_REQUEST").errors(Arrays.asList(item)).build();

    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(1, error.getErrors().size());
    assertEquals("email", error.getErrors().get(0).getKey());
    assertNotNull(error.toString());

    Error e2 = Error.newBuilder().message("BAD_REQUEST").errors(Arrays.asList(item)).build();
    assertEquals(error, e2);
    assertEquals(error.hashCode(), e2.hashCode());

    Error e3 = new Error("BAD_REQUEST", Arrays.asList(item));
    assertEquals("BAD_REQUEST", e3.getMessage());

    Error e4 = new Error();
    e4.setMessage("msg");
    e4.setErrors(Collections.emptyList());
    assertEquals("msg", e4.getMessage());
  }

  @Test
  void should_build_error_item() {
    ErrorItem item =
        ErrorItem.newBuilder().key("email").value(Arrays.asList("invalid", "duplicate")).build();

    assertEquals("email", item.getKey());
    assertEquals(2, item.getValue().size());
    assertNotNull(item.toString());

    ErrorItem i2 =
        ErrorItem.newBuilder().key("email").value(Arrays.asList("invalid", "duplicate")).build();
    assertEquals(item, i2);
    assertEquals(item.hashCode(), i2.hashCode());

    ErrorItem i3 = new ErrorItem("email", Arrays.asList("invalid"));
    assertEquals("email", i3.getKey());

    ErrorItem i4 = new ErrorItem();
    i4.setKey("k");
    i4.setValue(Collections.emptyList());
    assertEquals("k", i4.getKey());
  }

  @Test
  void should_build_page_info() {
    PageInfo pageInfo =
        PageInfo.newBuilder()
            .hasNextPage(true)
            .hasPreviousPage(false)
            .startCursor("start")
            .endCursor("end")
            .build();

    assertTrue(pageInfo.getHasNextPage());
    assertFalse(pageInfo.getHasPreviousPage());
    assertEquals("start", pageInfo.getStartCursor());
    assertEquals("end", pageInfo.getEndCursor());
    assertNotNull(pageInfo.toString());

    PageInfo p2 =
        PageInfo.newBuilder()
            .hasNextPage(true)
            .hasPreviousPage(false)
            .startCursor("start")
            .endCursor("end")
            .build();
    assertEquals(pageInfo, p2);
    assertEquals(pageInfo.hashCode(), p2.hashCode());

    PageInfo p3 = new PageInfo("end", true, false, "start");
    assertEquals("end", p3.getEndCursor());

    PageInfo p4 = new PageInfo();
    p4.setHasNextPage(false);
    p4.setHasPreviousPage(true);
    p4.setStartCursor("s");
    p4.setEndCursor("e");
    assertFalse(p4.getHasNextPage());
  }

  @Test
  void should_build_article_payload() {
    Article article = Article.newBuilder().slug("slug").build();
    ArticlePayload payload = ArticlePayload.newBuilder().article(article).build();

    assertEquals("slug", payload.getArticle().getSlug());
    assertNotNull(payload.toString());

    ArticlePayload p2 = ArticlePayload.newBuilder().article(article).build();
    assertEquals(payload, p2);
    assertEquals(payload.hashCode(), p2.hashCode());

    ArticlePayload p3 = new ArticlePayload(article);
    assertEquals("slug", p3.getArticle().getSlug());

    ArticlePayload p4 = new ArticlePayload();
    p4.setArticle(article);
    assertNotNull(p4.getArticle());
  }

  @Test
  void should_build_comment_payload() {
    Comment comment = Comment.newBuilder().id("c1").build();
    CommentPayload payload = CommentPayload.newBuilder().comment(comment).build();

    assertEquals("c1", payload.getComment().getId());
    assertNotNull(payload.toString());

    CommentPayload p2 = CommentPayload.newBuilder().comment(comment).build();
    assertEquals(payload, p2);
    assertEquals(payload.hashCode(), p2.hashCode());

    CommentPayload p3 = new CommentPayload(comment);
    assertEquals("c1", p3.getComment().getId());

    CommentPayload p4 = new CommentPayload();
    p4.setComment(comment);
    assertNotNull(p4.getComment());
  }

  @Test
  void should_build_user_payload() {
    User user = User.newBuilder().email("test@test.com").build();
    UserPayload payload = UserPayload.newBuilder().user(user).build();

    assertEquals("test@test.com", payload.getUser().getEmail());
    assertNotNull(payload.toString());

    UserPayload p2 = UserPayload.newBuilder().user(user).build();
    assertEquals(payload, p2);
    assertEquals(payload.hashCode(), p2.hashCode());

    UserPayload p3 = new UserPayload(user);
    assertEquals("test@test.com", p3.getUser().getEmail());

    UserPayload p4 = new UserPayload();
    p4.setUser(user);
    assertNotNull(p4.getUser());
  }

  @Test
  void should_build_profile_payload() {
    Profile profile = Profile.newBuilder().username("user").build();
    ProfilePayload payload = ProfilePayload.newBuilder().profile(profile).build();

    assertEquals("user", payload.getProfile().getUsername());
    assertNotNull(payload.toString());

    ProfilePayload p2 = ProfilePayload.newBuilder().profile(profile).build();
    assertEquals(payload, p2);
    assertEquals(payload.hashCode(), p2.hashCode());

    ProfilePayload p3 = new ProfilePayload(profile);
    assertEquals("user", p3.getProfile().getUsername());

    ProfilePayload p4 = new ProfilePayload();
    p4.setProfile(profile);
    assertNotNull(p4.getProfile());
  }

  @Test
  void should_build_user_result() {
    UserResult result = UserPayload.newBuilder().build();
    assertNotNull(result);
  }
}
