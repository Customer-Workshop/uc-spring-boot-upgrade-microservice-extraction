package io.spring.graphql.types;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class GeneratedTypesTest {

  @Test
  void article_builder_and_accessors() {
    Article article =
        Article.newBuilder()
            .slug("test-slug")
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .favorited(true)
            .favoritesCount(42)
            .createdAt("2024-01-01T00:00:00Z")
            .updatedAt("2024-01-02T00:00:00Z")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    assertEquals("test-slug", article.getSlug());
    assertEquals("Test Title", article.getTitle());
    assertEquals("Test Desc", article.getDescription());
    assertEquals("Test Body", article.getBody());
    assertTrue(article.getFavorited());
    assertEquals(42, article.getFavoritesCount());
    assertEquals("2024-01-01T00:00:00Z", article.getCreatedAt());
    assertEquals("2024-01-02T00:00:00Z", article.getUpdatedAt());
    assertEquals(2, article.getTagList().size());
    assertNotNull(article.toString());
  }

  @Test
  void article_setters() {
    Article article = new Article();
    article.setSlug("slug");
    article.setTitle("title");
    article.setDescription("desc");
    article.setBody("body");
    article.setFavorited(false);
    article.setFavoritesCount(0);
    article.setCreatedAt("created");
    article.setUpdatedAt("updated");
    article.setTagList(Collections.singletonList("tag"));
    article.setAuthor(Profile.newBuilder().username("author").build());
    article.setComments(CommentsConnection.newBuilder().build());

    assertEquals("slug", article.getSlug());
    assertEquals("title", article.getTitle());
    assertEquals("body", article.getBody());
    assertEquals("author", article.getAuthor().getUsername());
  }

  @Test
  void article_equals_and_hashcode() {
    Article a1 = Article.newBuilder().slug("s").title("t").body("b").build();
    Article a2 = Article.newBuilder().slug("s").title("t").body("b").build();

    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());
    assertEquals(a1, a1);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "string");
  }

  @Test
  void comment_builder_and_accessors() {
    Comment comment =
        Comment.newBuilder()
            .id("comment-id")
            .body("Comment body")
            .createdAt("2024-01-01T00:00:00Z")
            .updatedAt("2024-01-02T00:00:00Z")
            .build();

    assertEquals("comment-id", comment.getId());
    assertEquals("Comment body", comment.getBody());
    assertNotNull(comment.toString());
  }

  @Test
  void comment_setters_and_equality() {
    Comment c = new Comment();
    c.setId("id");
    c.setBody("body");
    c.setCreatedAt("created");
    c.setUpdatedAt("updated");
    c.setAuthor(Profile.newBuilder().username("author").build());
    c.setArticle(Article.newBuilder().slug("slug").build());

    assertEquals("id", c.getId());
    assertEquals("body", c.getBody());
    assertNotNull(c.getAuthor());
    assertNotNull(c.getArticle());

    Comment c2 = new Comment();
    c2.setId("id");
    c2.setBody("body");
    c2.setCreatedAt("created");
    c2.setUpdatedAt("updated");
    c2.setAuthor(Profile.newBuilder().username("author").build());
    c2.setArticle(Article.newBuilder().slug("slug").build());
    assertEquals(c, c2);
    assertEquals(c.hashCode(), c2.hashCode());
  }

  @Test
  void profile_builder_and_accessors() {
    Profile profile =
        Profile.newBuilder()
            .username("testuser")
            .bio("bio")
            .image("image.jpg")
            .following(true)
            .build();

    assertEquals("testuser", profile.getUsername());
    assertEquals("bio", profile.getBio());
    assertEquals("image.jpg", profile.getImage());
    assertTrue(profile.getFollowing());
  }

  @Test
  void profile_setters_and_equality() {
    Profile p = new Profile();
    p.setUsername("user");
    p.setBio("bio");
    p.setImage("img");
    p.setFollowing(false);
    p.setFeed(ArticlesConnection.newBuilder().build());
    p.setFavorites(ArticlesConnection.newBuilder().build());
    p.setArticles(ArticlesConnection.newBuilder().build());

    assertEquals("user", p.getUsername());
    assertNotNull(p.getFeed());
    assertNotNull(p.getFavorites());
    assertNotNull(p.getArticles());
    assertNotNull(p.toString());

    Profile p2 = new Profile();
    p2.setUsername("user");
    p2.setBio("bio");
    p2.setImage("img");
    p2.setFollowing(false);
    p2.setFeed(ArticlesConnection.newBuilder().build());
    p2.setFavorites(ArticlesConnection.newBuilder().build());
    p2.setArticles(ArticlesConnection.newBuilder().build());
    assertEquals(p, p2);
    assertEquals(p.hashCode(), p2.hashCode());
  }

  @Test
  void user_builder_and_accessors() {
    User user =
        User.newBuilder().email("test@test.com").username("testuser").token("jwt-token").build();

    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("jwt-token", user.getToken());
  }

  @Test
  void user_setters_and_equality() {
    User u = new User();
    u.setEmail("e");
    u.setUsername("u");
    u.setToken("t");
    u.setProfile(Profile.newBuilder().build());

    assertNotNull(u.toString());

    User u2 = new User();
    u2.setEmail("e");
    u2.setUsername("u");
    u2.setToken("t");
    u2.setProfile(Profile.newBuilder().build());
    assertEquals(u, u2);
    assertEquals(u.hashCode(), u2.hashCode());
  }

  @Test
  void create_article_input() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Title")
            .description("Desc")
            .body("Body")
            .tagList(Arrays.asList("tag1", "tag2"))
            .build();

    assertEquals("Title", input.getTitle());
    assertEquals("Desc", input.getDescription());
    assertEquals("Body", input.getBody());
    assertEquals(2, input.getTagList().size());
    assertNotNull(input.toString());

    CreateArticleInput input2 =
        CreateArticleInput.newBuilder()
            .title("Title")
            .description("Desc")
            .body("Body")
            .tagList(Arrays.asList("tag1", "tag2"))
            .build();
    assertEquals(input, input2);
    assertEquals(input.hashCode(), input2.hashCode());
  }

  @Test
  void create_article_input_setters() {
    CreateArticleInput input = new CreateArticleInput();
    input.setTitle("t");
    input.setDescription("d");
    input.setBody("b");
    input.setTagList(Collections.emptyList());

    assertEquals("t", input.getTitle());
    assertEquals("d", input.getDescription());
    assertEquals("b", input.getBody());
    assertTrue(input.getTagList().isEmpty());
  }

  @Test
  void update_article_input() {
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .description("New Desc")
            .body("New Body")
            .build();

    assertEquals("New Title", input.getTitle());
    assertEquals("New Desc", input.getDescription());
    assertEquals("New Body", input.getBody());

    UpdateArticleInput input2 =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .description("New Desc")
            .body("New Body")
            .build();
    assertEquals(input, input2);
    assertEquals(input.hashCode(), input2.hashCode());
  }

  @Test
  void update_article_input_setters() {
    UpdateArticleInput input = new UpdateArticleInput();
    input.setTitle("t");
    input.setDescription("d");
    input.setBody("b");
    assertEquals("t", input.getTitle());
  }

  @Test
  void create_user_input() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("email@test.com")
            .username("user")
            .password("pass")
            .build();

    assertEquals("email@test.com", input.getEmail());
    assertEquals("user", input.getUsername());
    assertEquals("pass", input.getPassword());

    CreateUserInput input2 =
        CreateUserInput.newBuilder()
            .email("email@test.com")
            .username("user")
            .password("pass")
            .build();
    assertEquals(input, input2);
  }

  @Test
  void create_user_input_setters() {
    CreateUserInput input = new CreateUserInput();
    input.setEmail("e");
    input.setUsername("u");
    input.setPassword("p");
    assertEquals("e", input.getEmail());
    assertNotNull(input.toString());
  }

  @Test
  void update_user_input() {
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new.jpg")
            .build();

    assertEquals("new@test.com", input.getEmail());
    assertEquals("newuser", input.getUsername());
    assertEquals("newpass", input.getPassword());
    assertEquals("new bio", input.getBio());
    assertEquals("new.jpg", input.getImage());

    UpdateUserInput input2 =
        UpdateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new.jpg")
            .build();
    assertEquals(input, input2);
    assertEquals(input.hashCode(), input2.hashCode());
  }

  @Test
  void update_user_input_setters() {
    UpdateUserInput input = new UpdateUserInput();
    input.setEmail("e");
    input.setUsername("u");
    input.setPassword("p");
    input.setBio("b");
    input.setImage("i");
    assertEquals("e", input.getEmail());
    assertNotNull(input.toString());
  }

  @Test
  void deletion_status() {
    DeletionStatus ds = DeletionStatus.newBuilder().success(true).build();
    assertTrue(ds.getSuccess());

    DeletionStatus ds2 = new DeletionStatus();
    ds2.setSuccess(false);
    assertFalse(ds2.getSuccess());

    DeletionStatus ds3 = DeletionStatus.newBuilder().success(true).build();
    assertEquals(ds, ds3);
    assertNotNull(ds.toString());
    assertEquals(ds.hashCode(), ds3.hashCode());
  }

  @Test
  void error_type() {
    ErrorItem item = ErrorItem.newBuilder().key("email").value(Arrays.asList("invalid")).build();
    Error error =
        Error.newBuilder().message("BAD_REQUEST").errors(Collections.singletonList(item)).build();

    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
    assertNotNull(error.toString());

    Error error2 =
        Error.newBuilder().message("BAD_REQUEST").errors(Collections.singletonList(item)).build();
    assertEquals(error, error2);
    assertEquals(error.hashCode(), error2.hashCode());
  }

  @Test
  void error_setters() {
    Error error = new Error();
    error.setMessage("msg");
    error.setErrors(Collections.emptyList());
    assertEquals("msg", error.getMessage());
    assertTrue(error.getErrors().isEmpty());
  }

  @Test
  void error_item() {
    ErrorItem item =
        ErrorItem.newBuilder().key("field").value(Arrays.asList("error1", "error2")).build();

    assertEquals("field", item.getKey());
    assertEquals(2, item.getValue().size());

    ErrorItem item2 = new ErrorItem();
    item2.setKey("field");
    item2.setValue(Arrays.asList("error1", "error2"));
    assertEquals(item, item2);
    assertEquals(item.hashCode(), item2.hashCode());
    assertNotNull(item.toString());
  }

  @Test
  void article_payload() {
    Article article = Article.newBuilder().slug("slug").build();
    ArticlePayload payload = ArticlePayload.newBuilder().article(article).build();

    assertNotNull(payload.getArticle());
    assertEquals("slug", payload.getArticle().getSlug());

    ArticlePayload payload2 = new ArticlePayload();
    payload2.setArticle(article);
    assertEquals(payload, payload2);
    assertNotNull(payload.toString());
  }

  @Test
  void comment_payload() {
    Comment comment = Comment.newBuilder().id("cid").build();
    CommentPayload payload = CommentPayload.newBuilder().comment(comment).build();

    assertNotNull(payload.getComment());
    assertEquals("cid", payload.getComment().getId());

    CommentPayload payload2 = new CommentPayload();
    payload2.setComment(comment);
    assertEquals(payload, payload2);
    assertNotNull(payload.toString());
  }

  @Test
  void user_payload() {
    User user = User.newBuilder().email("e").build();
    UserPayload payload = UserPayload.newBuilder().user(user).build();

    assertNotNull(payload.getUser());
    assertEquals("e", payload.getUser().getEmail());

    UserPayload payload2 = new UserPayload();
    payload2.setUser(user);
    assertEquals(payload, payload2);
    assertNotNull(payload.toString());
  }

  @Test
  void profile_payload() {
    Profile profile = Profile.newBuilder().username("u").build();
    ProfilePayload payload = ProfilePayload.newBuilder().profile(profile).build();

    assertNotNull(payload.getProfile());
    assertEquals("u", payload.getProfile().getUsername());

    ProfilePayload payload2 = new ProfilePayload();
    payload2.setProfile(profile);
    assertEquals(payload, payload2);
    assertNotNull(payload.toString());
  }

  @Test
  void page_info() {
    PageInfo pi =
        PageInfo.newBuilder()
            .hasNextPage(true)
            .hasPreviousPage(false)
            .startCursor("start")
            .endCursor("end")
            .build();

    assertTrue(pi.getHasNextPage());
    assertFalse(pi.getHasPreviousPage());
    assertEquals("start", pi.getStartCursor());
    assertEquals("end", pi.getEndCursor());

    PageInfo pi2 = new PageInfo();
    pi2.setHasNextPage(true);
    pi2.setHasPreviousPage(false);
    pi2.setStartCursor("start");
    pi2.setEndCursor("end");
    assertEquals(pi, pi2);
    assertEquals(pi.hashCode(), pi2.hashCode());
    assertNotNull(pi.toString());
  }

  @Test
  void articles_connection() {
    ArticleEdge edge =
        ArticleEdge.newBuilder()
            .cursor("cursor")
            .node(Article.newBuilder().slug("s").build())
            .build();
    ArticlesConnection conn =
        ArticlesConnection.newBuilder().edges(Collections.singletonList(edge)).build();

    assertFalse(conn.getEdges().isEmpty());
    assertEquals("cursor", conn.getEdges().get(0).getCursor());

    ArticlesConnection conn2 = new ArticlesConnection();
    conn2.setEdges(Collections.singletonList(edge));
    assertEquals(conn, conn2);
    assertNotNull(conn.toString());
  }

  @Test
  void articles_connection_setters() {
    ArticlesConnection conn = new ArticlesConnection();
    conn.setPageInfo(new graphql.relay.DefaultPageInfo(null, null, false, false));
    assertNotNull(conn.getPageInfo());
  }

  @Test
  void article_edge() {
    ArticleEdge edge =
        ArticleEdge.newBuilder().cursor("c").node(Article.newBuilder().slug("s").build()).build();

    assertEquals("c", edge.getCursor());
    assertNotNull(edge.getNode());

    ArticleEdge edge2 = new ArticleEdge();
    edge2.setCursor("c");
    edge2.setNode(Article.newBuilder().slug("s").build());
    assertEquals(edge, edge2);
    assertEquals(edge.hashCode(), edge2.hashCode());
    assertNotNull(edge.toString());
  }

  @Test
  void comments_connection() {
    CommentEdge edge =
        CommentEdge.newBuilder()
            .cursor("cursor")
            .node(Comment.newBuilder().id("id").build())
            .build();
    CommentsConnection conn =
        CommentsConnection.newBuilder().edges(Collections.singletonList(edge)).build();

    assertFalse(conn.getEdges().isEmpty());

    CommentsConnection conn2 = new CommentsConnection();
    conn2.setEdges(Collections.singletonList(edge));
    conn2.setPageInfo(new graphql.relay.DefaultPageInfo(null, null, false, false));
    assertNotNull(conn2.getPageInfo());
    assertNotNull(conn.toString());
  }

  @Test
  void comment_edge() {
    CommentEdge edge =
        CommentEdge.newBuilder().cursor("c").node(Comment.newBuilder().id("id").build()).build();

    assertEquals("c", edge.getCursor());
    assertNotNull(edge.getNode());

    CommentEdge edge2 = new CommentEdge();
    edge2.setCursor("c");
    edge2.setNode(Comment.newBuilder().id("id").build());
    assertEquals(edge, edge2);
    assertEquals(edge.hashCode(), edge2.hashCode());
    assertNotNull(edge.toString());
  }
}
