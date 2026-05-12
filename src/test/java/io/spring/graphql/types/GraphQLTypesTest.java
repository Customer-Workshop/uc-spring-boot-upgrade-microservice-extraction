package io.spring.graphql.types;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class GraphQLTypesTest {

  @Test
  public void should_build_article_with_builder() {
    Article article =
        Article.newBuilder()
            .slug("test-slug")
            .title("Test Title")
            .description("desc")
            .body("body")
            .favorited(true)
            .favoritesCount(5)
            .createdAt("2024-01-01")
            .updatedAt("2024-01-02")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    assertEquals("test-slug", article.getSlug());
    assertEquals("Test Title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertTrue(article.getFavorited());
    assertEquals(5, article.getFavoritesCount());
    assertEquals("2024-01-01", article.getCreatedAt());
    assertEquals("2024-01-02", article.getUpdatedAt());
    assertEquals(2, article.getTagList().size());
    assertNotNull(article.toString());
  }

  @Test
  public void should_build_article_with_constructor() {
    Profile author =
        Profile.newBuilder().username("author").bio("bio").image("img").following(false).build();
    CommentsConnection comments =
        CommentsConnection.newBuilder().edges(Collections.emptyList()).build();
    Article article =
        new Article(
            author,
            "body",
            comments,
            "2024-01-01",
            "desc",
            false,
            0,
            "slug",
            Arrays.asList("java"),
            "title",
            "2024-01-02");

    assertEquals("slug", article.getSlug());
    assertEquals(author, article.getAuthor());
    assertEquals(comments, article.getComments());
    assertNotNull(article.toString());
  }

  @Test
  public void should_set_article_fields() {
    Article article = new Article();
    article.setSlug("slug");
    article.setTitle("title");
    article.setDescription("desc");
    article.setBody("body");
    article.setFavorited(true);
    article.setFavoritesCount(10);
    article.setCreatedAt("2024-01-01");
    article.setUpdatedAt("2024-01-02");
    article.setTagList(Arrays.asList("test"));
    article.setAuthor(new Profile());
    article.setComments(new CommentsConnection());

    assertEquals("slug", article.getSlug());
    assertEquals("title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertTrue(article.getFavorited());
    assertEquals(10, article.getFavoritesCount());
    assertEquals("2024-01-01", article.getCreatedAt());
    assertEquals("2024-01-02", article.getUpdatedAt());
    assertEquals(1, article.getTagList().size());
    assertNotNull(article.getAuthor());
    assertNotNull(article.getComments());
  }

  @Test
  public void should_test_article_equals_and_hash() {
    Article a1 = Article.newBuilder().slug("s").title("t").build();
    Article a2 = Article.newBuilder().slug("s").title("t").build();
    Article a3 = Article.newBuilder().slug("s2").title("t2").build();
    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());
    assertNotEquals(a1, a3);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "not an article");
    assertEquals(a1, a1);
  }

  @Test
  public void should_build_profile() {
    Profile profile =
        Profile.newBuilder().username("user").bio("bio").image("image").following(true).build();

    assertEquals("user", profile.getUsername());
    assertEquals("bio", profile.getBio());
    assertEquals("image", profile.getImage());
    assertTrue(profile.getFollowing());
    assertNotNull(profile.toString());
  }

  @Test
  public void should_build_profile_with_constructor_and_setters() {
    Profile profile = new Profile("user", "bio", true, "image", null, null, null);
    assertEquals("user", profile.getUsername());
    profile.setUsername("new");
    assertEquals("new", profile.getUsername());
    profile.setBio("new bio");
    assertEquals("new bio", profile.getBio());
    profile.setImage("new img");
    assertEquals("new img", profile.getImage());
    profile.setFollowing(false);
    assertFalse(profile.getFollowing());
  }

  @Test
  public void should_test_profile_equals_and_hash() {
    Profile p1 = Profile.newBuilder().username("u").build();
    Profile p2 = Profile.newBuilder().username("u").build();
    Profile p3 = Profile.newBuilder().username("v").build();
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotEquals(p1, p3);
  }

  @Test
  public void should_build_profile_with_connections() {
    ArticlesConnection conn =
        ArticlesConnection.newBuilder().edges(Collections.emptyList()).build();
    Profile profile =
        Profile.newBuilder().username("user").articles(conn).favorites(conn).feed(conn).build();
    assertNotNull(profile.getArticles());
    assertNotNull(profile.getFavorites());
    assertNotNull(profile.getFeed());
    profile.setArticles(null);
    assertNull(profile.getArticles());
    profile.setFavorites(null);
    assertNull(profile.getFavorites());
    profile.setFeed(null);
    assertNull(profile.getFeed());
  }

  @Test
  public void should_build_comment() {
    Comment comment =
        Comment.newBuilder()
            .id("id")
            .body("body")
            .createdAt("2024-01-01")
            .updatedAt("2024-01-02")
            .build();

    assertEquals("id", comment.getId());
    assertEquals("body", comment.getBody());
    assertEquals("2024-01-01", comment.getCreatedAt());
    assertEquals("2024-01-02", comment.getUpdatedAt());
    assertNotNull(comment.toString());
  }

  @Test
  public void should_build_comment_with_constructor_and_setters() {
    Profile author = Profile.newBuilder().username("u").build();
    Article article = Article.newBuilder().slug("s").build();
    Comment c = new Comment("id", author, article, "body", "2024-01-01", "2024-01-02");
    assertEquals("id", c.getId());
    assertEquals(author, c.getAuthor());
    assertEquals(article, c.getArticle());
    c.setId("new");
    assertEquals("new", c.getId());
    c.setBody("new body");
    assertEquals("new body", c.getBody());
    c.setCreatedAt("new date");
    assertEquals("new date", c.getCreatedAt());
    c.setUpdatedAt("new updated");
    assertEquals("new updated", c.getUpdatedAt());
    c.setAuthor(null);
    assertNull(c.getAuthor());
    c.setArticle(null);
    assertNull(c.getArticle());
  }

  @Test
  public void should_test_comment_equals_and_hash() {
    Comment c1 = Comment.newBuilder().id("1").body("b").build();
    Comment c2 = Comment.newBuilder().id("1").body("b").build();
    Comment c3 = Comment.newBuilder().id("2").body("b").build();
    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotEquals(c1, c3);
  }

  @Test
  public void should_build_user() {
    User user = User.newBuilder().email("e@e.com").username("user").token("token").build();

    assertEquals("e@e.com", user.getEmail());
    assertEquals("user", user.getUsername());
    assertEquals("token", user.getToken());
    assertNotNull(user.toString());
  }

  @Test
  public void should_build_user_with_constructor_and_setters() {
    Profile profile = Profile.newBuilder().username("u").build();
    User u = new User("email", profile, "token", "user");
    assertEquals("email", u.getEmail());
    assertEquals(profile, u.getProfile());
    u.setEmail("new@e.com");
    assertEquals("new@e.com", u.getEmail());
    u.setUsername("new user");
    assertEquals("new user", u.getUsername());
    u.setToken("new token");
    assertEquals("new token", u.getToken());
    u.setProfile(null);
    assertNull(u.getProfile());
  }

  @Test
  public void should_test_user_equals_and_hash() {
    User u1 = User.newBuilder().email("e").username("u").build();
    User u2 = User.newBuilder().email("e").username("u").build();
    User u3 = User.newBuilder().email("f").username("v").build();
    assertEquals(u1, u2);
    assertEquals(u1.hashCode(), u2.hashCode());
    assertNotEquals(u1, u3);
  }

  @Test
  public void should_build_article_edge() {
    Article article = Article.newBuilder().slug("s").build();
    ArticleEdge edge = ArticleEdge.newBuilder().node(article).cursor("cur").build();
    assertEquals(article, edge.getNode());
    assertEquals("cur", edge.getCursor());
    assertNotNull(edge.toString());
  }

  @Test
  public void should_build_article_edge_with_constructor_and_setters() {
    ArticleEdge edge = new ArticleEdge("cursor", null);
    assertEquals("cursor", edge.getCursor());
    edge.setCursor("new");
    assertEquals("new", edge.getCursor());
    edge.setNode(Article.newBuilder().slug("s").build());
    assertNotNull(edge.getNode());
  }

  @Test
  public void should_test_article_edge_equals_and_hash() {
    ArticleEdge e1 = ArticleEdge.newBuilder().cursor("c").build();
    ArticleEdge e2 = ArticleEdge.newBuilder().cursor("c").build();
    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  public void should_build_comment_edge() {
    Comment comment = Comment.newBuilder().id("id").build();
    CommentEdge edge = CommentEdge.newBuilder().node(comment).cursor("cur").build();
    assertEquals(comment, edge.getNode());
    assertEquals("cur", edge.getCursor());
    assertNotNull(edge.toString());
  }

  @Test
  public void should_build_comment_edge_with_constructor_and_setters() {
    CommentEdge edge = new CommentEdge("cursor", null);
    assertEquals("cursor", edge.getCursor());
    edge.setCursor("new");
    assertEquals("new", edge.getCursor());
    edge.setNode(Comment.newBuilder().id("c").build());
    assertNotNull(edge.getNode());
  }

  @Test
  public void should_test_comment_edge_equals_and_hash() {
    CommentEdge e1 = CommentEdge.newBuilder().cursor("c").build();
    CommentEdge e2 = CommentEdge.newBuilder().cursor("c").build();
    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  public void should_build_page_info() {
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
    assertNotNull(pi.toString());
  }

  @Test
  public void should_build_page_info_with_constructor_and_setters() {
    PageInfo pi = new PageInfo("end", true, false, "start");
    assertEquals("end", pi.getEndCursor());
    assertTrue(pi.getHasNextPage());
    assertFalse(pi.getHasPreviousPage());
    assertEquals("start", pi.getStartCursor());
    pi.setEndCursor("newEnd");
    assertEquals("newEnd", pi.getEndCursor());
    pi.setStartCursor("newStart");
    assertEquals("newStart", pi.getStartCursor());
    pi.setHasNextPage(false);
    assertFalse(pi.getHasNextPage());
    pi.setHasPreviousPage(true);
    assertTrue(pi.getHasPreviousPage());
  }

  @Test
  public void should_test_page_info_equals_and_hash() {
    PageInfo p1 = PageInfo.newBuilder().hasNextPage(true).build();
    PageInfo p2 = PageInfo.newBuilder().hasNextPage(true).build();
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  public void should_build_articles_connection() {
    List<ArticleEdge> edges = Arrays.asList(ArticleEdge.newBuilder().cursor("c").build());
    ArticlesConnection conn = ArticlesConnection.newBuilder().edges(edges).build();

    assertEquals(1, conn.getEdges().size());
    assertNotNull(conn.toString());
  }

  @Test
  public void should_build_articles_connection_with_constructor_and_setters() {
    ArticlesConnection conn = new ArticlesConnection(Collections.emptyList(), null);
    assertNotNull(conn.getEdges());
    conn.setEdges(null);
    assertNull(conn.getEdges());
  }

  @Test
  public void should_test_articles_connection_equals_and_hash() {
    ArticlesConnection c1 = ArticlesConnection.newBuilder().edges(Collections.emptyList()).build();
    ArticlesConnection c2 = ArticlesConnection.newBuilder().edges(Collections.emptyList()).build();
    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
  }

  @Test
  public void should_build_comments_connection() {
    CommentsConnection conn =
        CommentsConnection.newBuilder().edges(Collections.emptyList()).build();
    assertNotNull(conn.getEdges());
    assertNotNull(conn.toString());
  }

  @Test
  public void should_build_comments_connection_with_constructor_and_setters() {
    CommentsConnection conn = new CommentsConnection(Collections.emptyList(), null);
    assertNotNull(conn.getEdges());
    conn.setEdges(null);
    assertNull(conn.getEdges());
  }

  @Test
  public void should_test_comments_connection_equals_and_hash() {
    CommentsConnection c1 = CommentsConnection.newBuilder().edges(Collections.emptyList()).build();
    CommentsConnection c2 = CommentsConnection.newBuilder().edges(Collections.emptyList()).build();
    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
  }

  @Test
  public void should_build_deletion_status() {
    DeletionStatus ds = DeletionStatus.newBuilder().success(true).build();
    assertTrue(ds.getSuccess());
    assertNotNull(ds.toString());
  }

  @Test
  public void should_build_deletion_status_with_constructor_and_setters() {
    DeletionStatus ds = new DeletionStatus(false);
    assertFalse(ds.getSuccess());
    ds.setSuccess(true);
    assertTrue(ds.getSuccess());
  }

  @Test
  public void should_test_deletion_status_equals_and_hash() {
    DeletionStatus d1 = DeletionStatus.newBuilder().success(true).build();
    DeletionStatus d2 = DeletionStatus.newBuilder().success(true).build();
    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  public void should_build_error() {
    ErrorItem item = ErrorItem.newBuilder().key("field").value(Arrays.asList("msg")).build();
    Error error = Error.newBuilder().message("BAD_REQUEST").errors(Arrays.asList(item)).build();

    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(1, error.getErrors().size());
    assertNotNull(error.toString());
  }

  @Test
  public void should_build_error_with_constructor_and_setters() {
    Error error = new Error("msg", Collections.emptyList());
    assertEquals("msg", error.getMessage());
    error.setMessage("new");
    assertEquals("new", error.getMessage());
    error.setErrors(null);
    assertNull(error.getErrors());
  }

  @Test
  public void should_test_error_equals_and_hash() {
    Error e1 = Error.newBuilder().message("m").build();
    Error e2 = Error.newBuilder().message("m").build();
    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  public void should_build_error_item() {
    ErrorItem item =
        ErrorItem.newBuilder().key("email").value(Arrays.asList("must not be empty")).build();

    assertEquals("email", item.getKey());
    assertEquals(1, item.getValue().size());
    assertNotNull(item.toString());
  }

  @Test
  public void should_build_error_item_with_constructor_and_setters() {
    ErrorItem item = new ErrorItem("key", Arrays.asList("val"));
    assertEquals("key", item.getKey());
    item.setKey("new");
    assertEquals("new", item.getKey());
    item.setValue(null);
    assertNull(item.getValue());
  }

  @Test
  public void should_test_error_item_equals_and_hash() {
    ErrorItem i1 = ErrorItem.newBuilder().key("k").build();
    ErrorItem i2 = ErrorItem.newBuilder().key("k").build();
    assertEquals(i1, i2);
    assertEquals(i1.hashCode(), i2.hashCode());
  }

  @Test
  public void should_build_create_article_input() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();

    assertEquals("title", input.getTitle());
    assertEquals("desc", input.getDescription());
    assertEquals("body", input.getBody());
    assertEquals(1, input.getTagList().size());
    assertNotNull(input.toString());
  }

  @Test
  public void should_build_create_article_input_with_constructor_and_setters() {
    CreateArticleInput input = new CreateArticleInput("body", "desc", Arrays.asList("t"), "title");
    assertEquals("body", input.getBody());
    input.setBody("new body");
    assertEquals("new body", input.getBody());
    input.setDescription("new desc");
    assertEquals("new desc", input.getDescription());
    input.setTitle("new title");
    assertEquals("new title", input.getTitle());
    input.setTagList(null);
    assertNull(input.getTagList());
  }

  @Test
  public void should_test_create_article_input_equals_and_hash() {
    CreateArticleInput i1 =
        CreateArticleInput.newBuilder().title("t").body("b").description("d").build();
    CreateArticleInput i2 =
        CreateArticleInput.newBuilder().title("t").body("b").description("d").build();
    assertEquals(i1, i2);
    assertEquals(i1.hashCode(), i2.hashCode());
  }

  @Test
  public void should_build_update_article_input() {
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("new title")
            .description("new desc")
            .body("new body")
            .build();

    assertEquals("new title", input.getTitle());
    assertEquals("new desc", input.getDescription());
    assertEquals("new body", input.getBody());
    assertNotNull(input.toString());
  }

  @Test
  public void should_build_update_article_input_with_constructor_and_setters() {
    UpdateArticleInput input = new UpdateArticleInput("body", "desc", "title");
    assertEquals("body", input.getBody());
    input.setBody("new body");
    assertEquals("new body", input.getBody());
    input.setDescription("new desc");
    assertEquals("new desc", input.getDescription());
    input.setTitle("new title");
    assertEquals("new title", input.getTitle());
  }

  @Test
  public void should_test_update_article_input_equals_and_hash() {
    UpdateArticleInput i1 = UpdateArticleInput.newBuilder().title("t").build();
    UpdateArticleInput i2 = UpdateArticleInput.newBuilder().title("t").build();
    assertEquals(i1, i2);
    assertEquals(i1.hashCode(), i2.hashCode());
  }

  @Test
  public void should_build_create_user_input() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("e@e.com").username("user").password("pass").build();

    assertEquals("e@e.com", input.getEmail());
    assertEquals("user", input.getUsername());
    assertEquals("pass", input.getPassword());
    assertNotNull(input.toString());
  }

  @Test
  public void should_build_create_user_input_with_constructor_and_setters() {
    CreateUserInput input = new CreateUserInput("email", "pass", "user");
    assertEquals("email", input.getEmail());
    input.setEmail("new@e.com");
    assertEquals("new@e.com", input.getEmail());
    input.setUsername("new user");
    assertEquals("new user", input.getUsername());
    input.setPassword("new pass");
    assertEquals("new pass", input.getPassword());
  }

  @Test
  public void should_test_create_user_input_equals_and_hash() {
    CreateUserInput i1 = CreateUserInput.newBuilder().email("e").build();
    CreateUserInput i2 = CreateUserInput.newBuilder().email("e").build();
    assertEquals(i1, i2);
    assertEquals(i1.hashCode(), i2.hashCode());
  }

  @Test
  public void should_build_update_user_input() {
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("e@e.com")
            .username("user")
            .password("pass")
            .image("img")
            .bio("bio")
            .build();

    assertEquals("e@e.com", input.getEmail());
    assertEquals("user", input.getUsername());
    assertEquals("pass", input.getPassword());
    assertEquals("img", input.getImage());
    assertEquals("bio", input.getBio());
    assertNotNull(input.toString());
  }

  @Test
  public void should_build_update_user_input_with_constructor_and_setters() {
    UpdateUserInput input = new UpdateUserInput("email", "user", "pass", "img", "bio");
    assertEquals("bio", input.getBio());
    input.setBio("new bio");
    assertEquals("new bio", input.getBio());
    input.setEmail("new@e.com");
    assertEquals("new@e.com", input.getEmail());
    input.setImage("new img");
    assertEquals("new img", input.getImage());
    input.setPassword("new pass");
    assertEquals("new pass", input.getPassword());
    input.setUsername("new user");
    assertEquals("new user", input.getUsername());
  }

  @Test
  public void should_test_update_user_input_equals_and_hash() {
    UpdateUserInput i1 = UpdateUserInput.newBuilder().email("e").build();
    UpdateUserInput i2 = UpdateUserInput.newBuilder().email("e").build();
    assertEquals(i1, i2);
    assertEquals(i1.hashCode(), i2.hashCode());
  }

  @Test
  public void should_build_article_payload() {
    Article article = Article.newBuilder().slug("s").build();
    ArticlePayload payload = ArticlePayload.newBuilder().article(article).build();
    assertEquals(article, payload.getArticle());
    assertNotNull(payload.toString());
  }

  @Test
  public void should_build_article_payload_with_constructor_and_setters() {
    ArticlePayload payload = new ArticlePayload(null);
    assertNull(payload.getArticle());
    payload.setArticle(Article.newBuilder().slug("s").build());
    assertNotNull(payload.getArticle());
  }

  @Test
  public void should_test_article_payload_equals_and_hash() {
    ArticlePayload p1 = ArticlePayload.newBuilder().build();
    ArticlePayload p2 = ArticlePayload.newBuilder().build();
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  public void should_build_comment_payload() {
    Comment comment = Comment.newBuilder().id("c").build();
    CommentPayload payload = CommentPayload.newBuilder().comment(comment).build();
    assertEquals(comment, payload.getComment());
    assertNotNull(payload.toString());
  }

  @Test
  public void should_build_comment_payload_with_constructor_and_setters() {
    CommentPayload payload = new CommentPayload(null);
    assertNull(payload.getComment());
    payload.setComment(Comment.newBuilder().id("c").build());
    assertNotNull(payload.getComment());
  }

  @Test
  public void should_test_comment_payload_equals_and_hash() {
    CommentPayload p1 = CommentPayload.newBuilder().build();
    CommentPayload p2 = CommentPayload.newBuilder().build();
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  public void should_build_user_payload() {
    User user = User.newBuilder().email("e").build();
    UserPayload payload = UserPayload.newBuilder().user(user).build();
    assertEquals(user, payload.getUser());
    assertNotNull(payload.toString());
  }

  @Test
  public void should_build_user_payload_with_constructor_and_setters() {
    UserPayload payload = new UserPayload(null);
    assertNull(payload.getUser());
    payload.setUser(User.newBuilder().email("e").build());
    assertNotNull(payload.getUser());
  }

  @Test
  public void should_test_user_payload_equals_and_hash() {
    UserPayload p1 = UserPayload.newBuilder().build();
    UserPayload p2 = UserPayload.newBuilder().build();
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  public void should_build_profile_payload() {
    Profile profile = Profile.newBuilder().username("u").build();
    ProfilePayload payload = ProfilePayload.newBuilder().profile(profile).build();
    assertEquals(profile, payload.getProfile());
    assertNotNull(payload.toString());
  }

  @Test
  public void should_build_profile_payload_with_constructor_and_setters() {
    ProfilePayload payload = new ProfilePayload(null);
    assertNull(payload.getProfile());
    payload.setProfile(Profile.newBuilder().username("u").build());
    assertNotNull(payload.getProfile());
  }

  @Test
  public void should_test_profile_payload_equals_and_hash() {
    ProfilePayload p1 = ProfilePayload.newBuilder().build();
    ProfilePayload p2 = ProfilePayload.newBuilder().build();
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  public void should_default_construct_all_types() {
    assertNotNull(new Article());
    assertNotNull(new Profile());
    assertNotNull(new Comment());
    assertNotNull(new User());
    assertNotNull(new ArticleEdge());
    assertNotNull(new CommentEdge());
    assertNotNull(new PageInfo());
    assertNotNull(new ArticlesConnection());
    assertNotNull(new CommentsConnection());
    assertNotNull(new DeletionStatus());
    assertNotNull(new Error());
    assertNotNull(new ErrorItem());
    assertNotNull(new CreateArticleInput());
    assertNotNull(new UpdateArticleInput());
    assertNotNull(new CreateUserInput());
    assertNotNull(new UpdateUserInput());
    assertNotNull(new ArticlePayload());
    assertNotNull(new CommentPayload());
    assertNotNull(new UserPayload());
    assertNotNull(new ProfilePayload());
  }
}
