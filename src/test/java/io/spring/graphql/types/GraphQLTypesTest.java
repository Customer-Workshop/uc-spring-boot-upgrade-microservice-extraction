package io.spring.graphql.types;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class GraphQLTypesTest {

  // ========== Article ==========

  @Test
  public void article_default_constructor_and_setters() {
    Article article = new Article();
    Profile author = Profile.newBuilder().username("user1").build();
    CommentsConnection comments =
        CommentsConnection.newBuilder().edges(java.util.Collections.emptyList()).build();

    article.setAuthor(author);
    article.setBody("body");
    article.setComments(comments);
    article.setCreatedAt("2024-01-01");
    article.setDescription("desc");
    article.setFavorited(true);
    article.setFavoritesCount(5);
    article.setSlug("test-slug");
    article.setTagList(Arrays.asList("java", "spring"));
    article.setTitle("title");
    article.setUpdatedAt("2024-01-02");

    assertEquals(author, article.getAuthor());
    assertEquals("body", article.getBody());
    assertEquals(comments, article.getComments());
    assertEquals("2024-01-01", article.getCreatedAt());
    assertEquals("desc", article.getDescription());
    assertTrue(article.getFavorited());
    assertEquals(5, article.getFavoritesCount());
    assertEquals("test-slug", article.getSlug());
    assertEquals(Arrays.asList("java", "spring"), article.getTagList());
    assertEquals("title", article.getTitle());
    assertEquals("2024-01-02", article.getUpdatedAt());
  }

  @Test
  public void article_full_constructor() {
    Profile author = Profile.newBuilder().username("user1").build();
    CommentsConnection comments =
        CommentsConnection.newBuilder().edges(java.util.Collections.emptyList()).build();
    List<String> tags = Arrays.asList("java");

    Article article =
        new Article(
            author, "body", comments, "2024-01-01", "desc", true, 5, "slug", tags, "title",
            "2024-01-02");

    assertEquals(author, article.getAuthor());
    assertEquals("body", article.getBody());
    assertEquals("title", article.getTitle());
    assertEquals("slug", article.getSlug());
    assertTrue(article.getFavorited());
    assertEquals(5, article.getFavoritesCount());
  }

  @Test
  public void article_toString() {
    Article article = Article.newBuilder().title("test").slug("test-slug").build();
    String str = article.toString();
    assertNotNull(str);
    assertTrue(str.contains("Article{"));
    assertTrue(str.contains("test"));
  }

  @Test
  public void article_equals_and_hashCode() {
    Article a1 = Article.newBuilder().title("t").slug("s").body("b").build();
    Article a2 = Article.newBuilder().title("t").slug("s").body("b").build();
    Article a3 = Article.newBuilder().title("other").slug("s").body("b").build();

    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());
    assertNotEquals(a1, a3);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "string");
    assertEquals(a1, a1);
  }

  // ========== Profile ==========

  @Test
  public void profile_default_constructor_and_setters() {
    Profile profile = new Profile();
    ArticlesConnection articles =
        ArticlesConnection.newBuilder().edges(java.util.Collections.emptyList()).build();

    profile.setUsername("user1");
    profile.setBio("bio");
    profile.setFollowing(true);
    profile.setImage("img.png");
    profile.setArticles(articles);
    profile.setFavorites(articles);
    profile.setFeed(articles);

    assertEquals("user1", profile.getUsername());
    assertEquals("bio", profile.getBio());
    assertTrue(profile.getFollowing());
    assertEquals("img.png", profile.getImage());
    assertEquals(articles, profile.getArticles());
    assertEquals(articles, profile.getFavorites());
    assertEquals(articles, profile.getFeed());
  }

  @Test
  public void profile_full_constructor() {
    ArticlesConnection articles =
        ArticlesConnection.newBuilder().edges(java.util.Collections.emptyList()).build();
    Profile profile = new Profile("user1", "bio", true, "img.png", articles, articles, articles);

    assertEquals("user1", profile.getUsername());
    assertEquals("bio", profile.getBio());
    assertTrue(profile.getFollowing());
  }

  @Test
  public void profile_toString() {
    Profile profile = Profile.newBuilder().username("user1").build();
    String str = profile.toString();
    assertNotNull(str);
    assertTrue(str.contains("Profile{"));
    assertTrue(str.contains("user1"));
  }

  @Test
  public void profile_equals_and_hashCode() {
    Profile p1 = Profile.newBuilder().username("u").bio("b").build();
    Profile p2 = Profile.newBuilder().username("u").bio("b").build();
    Profile p3 = Profile.newBuilder().username("other").bio("b").build();

    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotEquals(p1, p3);
    assertNotEquals(p1, null);
    assertEquals(p1, p1);
  }

  // ========== Comment ==========

  @Test
  public void comment_default_constructor_and_setters() {
    Comment comment = new Comment();
    Profile author = Profile.newBuilder().username("user1").build();
    Article article = Article.newBuilder().slug("s").build();

    comment.setId("id1");
    comment.setBody("body");
    comment.setAuthor(author);
    comment.setArticle(article);
    comment.setCreatedAt("2024-01-01");
    comment.setUpdatedAt("2024-01-02");

    assertEquals("id1", comment.getId());
    assertEquals("body", comment.getBody());
    assertEquals(author, comment.getAuthor());
    assertEquals(article, comment.getArticle());
    assertEquals("2024-01-01", comment.getCreatedAt());
    assertEquals("2024-01-02", comment.getUpdatedAt());
  }

  @Test
  public void comment_full_constructor() {
    Profile author = Profile.newBuilder().username("user1").build();
    Article article = Article.newBuilder().slug("s").build();
    Comment comment = new Comment("id1", author, article, "body", "2024-01-01", "2024-01-02");

    assertEquals("id1", comment.getId());
    assertEquals("body", comment.getBody());
  }

  @Test
  public void comment_toString() {
    Comment comment = Comment.newBuilder().id("id1").body("body").build();
    String str = comment.toString();
    assertNotNull(str);
    assertTrue(str.contains("Comment{"));
  }

  @Test
  public void comment_equals_and_hashCode() {
    Comment c1 = Comment.newBuilder().id("id1").body("b").build();
    Comment c2 = Comment.newBuilder().id("id1").body("b").build();
    Comment c3 = Comment.newBuilder().id("id2").body("b").build();

    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotEquals(c1, c3);
    assertNotEquals(c1, null);
    assertEquals(c1, c1);
  }

  // ========== User ==========

  @Test
  public void user_default_constructor_and_setters() {
    User user = new User();
    Profile profile = Profile.newBuilder().username("user1").build();

    user.setEmail("test@test.com");
    user.setToken("token123");
    user.setUsername("user1");
    user.setProfile(profile);

    assertEquals("test@test.com", user.getEmail());
    assertEquals("token123", user.getToken());
    assertEquals("user1", user.getUsername());
    assertEquals(profile, user.getProfile());
  }

  @Test
  public void user_full_constructor() {
    Profile profile = Profile.newBuilder().username("user1").build();
    User user = new User("test@test.com", profile, "token123", "user1");

    assertEquals("test@test.com", user.getEmail());
    assertEquals("token123", user.getToken());
  }

  @Test
  public void user_toString() {
    User user = User.newBuilder().email("test@test.com").username("user1").build();
    String str = user.toString();
    assertNotNull(str);
    assertTrue(str.contains("User{"));
  }

  @Test
  public void user_equals_and_hashCode() {
    User u1 = User.newBuilder().email("e").username("u").build();
    User u2 = User.newBuilder().email("e").username("u").build();
    User u3 = User.newBuilder().email("other").username("u").build();

    assertEquals(u1, u2);
    assertEquals(u1.hashCode(), u2.hashCode());
    assertNotEquals(u1, u3);
    assertNotEquals(u1, null);
    assertEquals(u1, u1);
  }

  // ========== PageInfo ==========

  @Test
  public void pageInfo_default_constructor_and_setters() {
    PageInfo pageInfo = new PageInfo();
    pageInfo.setHasNextPage(true);
    pageInfo.setHasPreviousPage(false);
    pageInfo.setStartCursor("start");
    pageInfo.setEndCursor("end");

    assertTrue(pageInfo.getHasNextPage());
    assertFalse(pageInfo.getHasPreviousPage());
    assertEquals("start", pageInfo.getStartCursor());
    assertEquals("end", pageInfo.getEndCursor());
  }

  @Test
  public void pageInfo_full_constructor() {
    PageInfo pageInfo = new PageInfo("end", true, false, "start");
    assertTrue(pageInfo.getHasNextPage());
    assertFalse(pageInfo.getHasPreviousPage());
    assertEquals("start", pageInfo.getStartCursor());
    assertEquals("end", pageInfo.getEndCursor());
  }

  @Test
  public void pageInfo_builder() {
    PageInfo pageInfo =
        PageInfo.newBuilder()
            .hasNextPage(true)
            .hasPreviousPage(false)
            .startCursor("s")
            .endCursor("e")
            .build();

    assertTrue(pageInfo.getHasNextPage());
    assertFalse(pageInfo.getHasPreviousPage());
    assertEquals("s", pageInfo.getStartCursor());
    assertEquals("e", pageInfo.getEndCursor());
  }

  @Test
  public void pageInfo_toString() {
    PageInfo pi = PageInfo.newBuilder().hasNextPage(true).startCursor("s").build();
    String str = pi.toString();
    assertNotNull(str);
    assertTrue(str.contains("PageInfo{"));
  }

  @Test
  public void pageInfo_equals_and_hashCode() {
    PageInfo p1 = PageInfo.newBuilder().hasNextPage(true).startCursor("s").build();
    PageInfo p2 = PageInfo.newBuilder().hasNextPage(true).startCursor("s").build();
    PageInfo p3 = PageInfo.newBuilder().hasNextPage(false).startCursor("s").build();

    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotEquals(p1, p3);
    assertNotEquals(p1, null);
    assertEquals(p1, p1);
  }

  // ========== ArticleEdge ==========

  @Test
  public void articleEdge_default_constructor_and_setters() {
    ArticleEdge edge = new ArticleEdge();
    Article article = Article.newBuilder().slug("s").build();

    edge.setNode(article);
    edge.setCursor("cursor1");

    assertEquals(article, edge.getNode());
    assertEquals("cursor1", edge.getCursor());
  }

  @Test
  public void articleEdge_full_constructor() {
    Article article = Article.newBuilder().slug("s").build();
    ArticleEdge edge = new ArticleEdge("cursor1", article);

    assertEquals(article, edge.getNode());
    assertEquals("cursor1", edge.getCursor());
  }

  @Test
  public void articleEdge_toString() {
    ArticleEdge edge = ArticleEdge.newBuilder().cursor("c1").build();
    String str = edge.toString();
    assertNotNull(str);
    assertTrue(str.contains("ArticleEdge{"));
  }

  @Test
  public void articleEdge_equals_and_hashCode() {
    ArticleEdge e1 = ArticleEdge.newBuilder().cursor("c").build();
    ArticleEdge e2 = ArticleEdge.newBuilder().cursor("c").build();
    ArticleEdge e3 = ArticleEdge.newBuilder().cursor("other").build();

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
    assertNotEquals(e1, e3);
    assertNotEquals(e1, null);
    assertEquals(e1, e1);
  }

  // ========== CommentEdge ==========

  @Test
  public void commentEdge_default_constructor_and_setters() {
    CommentEdge edge = new CommentEdge();
    Comment comment = Comment.newBuilder().id("id1").build();

    edge.setNode(comment);
    edge.setCursor("cursor1");

    assertEquals(comment, edge.getNode());
    assertEquals("cursor1", edge.getCursor());
  }

  @Test
  public void commentEdge_full_constructor() {
    Comment comment = Comment.newBuilder().id("id1").build();
    CommentEdge edge = new CommentEdge("cursor1", comment);

    assertEquals(comment, edge.getNode());
  }

  @Test
  public void commentEdge_toString() {
    CommentEdge edge = CommentEdge.newBuilder().cursor("c1").build();
    String str = edge.toString();
    assertNotNull(str);
    assertTrue(str.contains("CommentEdge{"));
  }

  @Test
  public void commentEdge_equals_and_hashCode() {
    CommentEdge e1 = CommentEdge.newBuilder().cursor("c").build();
    CommentEdge e2 = CommentEdge.newBuilder().cursor("c").build();
    CommentEdge e3 = CommentEdge.newBuilder().cursor("other").build();

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
    assertNotEquals(e1, e3);
  }

  // ========== ArticlesConnection ==========

  @Test
  public void articlesConnection_default_constructor_and_setters() {
    ArticlesConnection conn = new ArticlesConnection();
    List<ArticleEdge> edges = java.util.Collections.emptyList();

    conn.setEdges(edges);

    assertEquals(edges, conn.getEdges());
  }

  @Test
  public void articlesConnection_full_constructor() {
    List<ArticleEdge> edges = java.util.Collections.emptyList();
    ArticlesConnection conn = new ArticlesConnection(edges, null);

    assertEquals(edges, conn.getEdges());
  }

  @Test
  public void articlesConnection_toString() {
    ArticlesConnection conn =
        ArticlesConnection.newBuilder().edges(java.util.Collections.emptyList()).build();
    String str = conn.toString();
    assertNotNull(str);
    assertTrue(str.contains("ArticlesConnection{"));
  }

  @Test
  public void articlesConnection_equals_and_hashCode() {
    ArticlesConnection c1 =
        ArticlesConnection.newBuilder().edges(java.util.Collections.emptyList()).build();
    ArticlesConnection c2 =
        ArticlesConnection.newBuilder().edges(java.util.Collections.emptyList()).build();

    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
    assertEquals(c1, c1);
    assertNotEquals(c1, null);
  }

  // ========== CommentsConnection ==========

  @Test
  public void commentsConnection_default_constructor_and_setters() {
    CommentsConnection conn = new CommentsConnection();
    List<CommentEdge> edges = java.util.Collections.emptyList();

    conn.setEdges(edges);

    assertEquals(edges, conn.getEdges());
  }

  @Test
  public void commentsConnection_full_constructor() {
    List<CommentEdge> edges = java.util.Collections.emptyList();
    CommentsConnection conn = new CommentsConnection(edges, null);

    assertEquals(edges, conn.getEdges());
  }

  @Test
  public void commentsConnection_toString() {
    CommentsConnection conn =
        CommentsConnection.newBuilder().edges(java.util.Collections.emptyList()).build();
    String str = conn.toString();
    assertNotNull(str);
    assertTrue(str.contains("CommentsConnection{"));
  }

  @Test
  public void commentsConnection_equals_and_hashCode() {
    CommentsConnection c1 =
        CommentsConnection.newBuilder().edges(java.util.Collections.emptyList()).build();
    CommentsConnection c2 =
        CommentsConnection.newBuilder().edges(java.util.Collections.emptyList()).build();

    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
  }

  // ========== ArticlePayload ==========

  @Test
  public void articlePayload_default_constructor_and_setters() {
    ArticlePayload payload = new ArticlePayload();
    Article article = Article.newBuilder().slug("s").build();

    payload.setArticle(article);

    assertEquals(article, payload.getArticle());
  }

  @Test
  public void articlePayload_full_constructor() {
    Article article = Article.newBuilder().slug("s").build();
    ArticlePayload payload = new ArticlePayload(article);

    assertEquals(article, payload.getArticle());
  }

  @Test
  public void articlePayload_toString() {
    ArticlePayload payload = ArticlePayload.newBuilder().build();
    String str = payload.toString();
    assertNotNull(str);
    assertTrue(str.contains("ArticlePayload{"));
  }

  @Test
  public void articlePayload_equals_and_hashCode() {
    Article a = Article.newBuilder().slug("s").build();
    ArticlePayload p1 = ArticlePayload.newBuilder().article(a).build();
    ArticlePayload p2 = ArticlePayload.newBuilder().article(a).build();

    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotEquals(p1, null);
  }

  // ========== CommentPayload ==========

  @Test
  public void commentPayload_default_constructor_and_setters() {
    CommentPayload payload = new CommentPayload();
    Comment comment = Comment.newBuilder().id("id1").build();

    payload.setComment(comment);

    assertEquals(comment, payload.getComment());
  }

  @Test
  public void commentPayload_full_constructor() {
    Comment comment = Comment.newBuilder().id("id1").build();
    CommentPayload payload = new CommentPayload(comment);

    assertEquals(comment, payload.getComment());
  }

  @Test
  public void commentPayload_toString() {
    CommentPayload payload = CommentPayload.newBuilder().build();
    String str = payload.toString();
    assertNotNull(str);
    assertTrue(str.contains("CommentPayload{"));
  }

  @Test
  public void commentPayload_equals_and_hashCode() {
    Comment c = Comment.newBuilder().id("id1").build();
    CommentPayload p1 = CommentPayload.newBuilder().comment(c).build();
    CommentPayload p2 = CommentPayload.newBuilder().comment(c).build();

    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  // ========== ProfilePayload ==========

  @Test
  public void profilePayload_default_constructor_and_setters() {
    ProfilePayload payload = new ProfilePayload();
    Profile profile = Profile.newBuilder().username("u").build();

    payload.setProfile(profile);

    assertEquals(profile, payload.getProfile());
  }

  @Test
  public void profilePayload_full_constructor() {
    Profile profile = Profile.newBuilder().username("u").build();
    ProfilePayload payload = new ProfilePayload(profile);

    assertEquals(profile, payload.getProfile());
  }

  @Test
  public void profilePayload_toString() {
    ProfilePayload payload = ProfilePayload.newBuilder().build();
    String str = payload.toString();
    assertNotNull(str);
    assertTrue(str.contains("ProfilePayload{"));
  }

  @Test
  public void profilePayload_equals_and_hashCode() {
    Profile p = Profile.newBuilder().username("u").build();
    ProfilePayload pp1 = ProfilePayload.newBuilder().profile(p).build();
    ProfilePayload pp2 = ProfilePayload.newBuilder().profile(p).build();

    assertEquals(pp1, pp2);
    assertEquals(pp1.hashCode(), pp2.hashCode());
  }

  // ========== UserPayload ==========

  @Test
  public void userPayload_default_constructor_and_setters() {
    UserPayload payload = new UserPayload();
    User user = User.newBuilder().email("e").build();

    payload.setUser(user);

    assertEquals(user, payload.getUser());
  }

  @Test
  public void userPayload_full_constructor() {
    User user = User.newBuilder().email("e").build();
    UserPayload payload = new UserPayload(user);

    assertEquals(user, payload.getUser());
  }

  @Test
  public void userPayload_toString() {
    UserPayload payload = UserPayload.newBuilder().build();
    String str = payload.toString();
    assertNotNull(str);
    assertTrue(str.contains("UserPayload{"));
  }

  @Test
  public void userPayload_equals_and_hashCode() {
    User u = User.newBuilder().email("e").build();
    UserPayload p1 = UserPayload.newBuilder().user(u).build();
    UserPayload p2 = UserPayload.newBuilder().user(u).build();

    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  // ========== DeletionStatus ==========

  @Test
  public void deletionStatus_default_constructor_and_setters() {
    DeletionStatus ds = new DeletionStatus();
    ds.setSuccess(true);

    assertTrue(ds.getSuccess());
  }

  @Test
  public void deletionStatus_full_constructor() {
    DeletionStatus ds = new DeletionStatus(true);
    assertTrue(ds.getSuccess());
  }

  @Test
  public void deletionStatus_toString() {
    DeletionStatus ds = DeletionStatus.newBuilder().success(true).build();
    String str = ds.toString();
    assertNotNull(str);
    assertTrue(str.contains("DeletionStatus{"));
  }

  @Test
  public void deletionStatus_equals_and_hashCode() {
    DeletionStatus d1 = DeletionStatus.newBuilder().success(true).build();
    DeletionStatus d2 = DeletionStatus.newBuilder().success(true).build();
    DeletionStatus d3 = DeletionStatus.newBuilder().success(false).build();

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
    assertNotEquals(d1, d3);
  }

  // ========== ErrorItem ==========

  @Test
  public void errorItem_default_constructor_and_setters() {
    ErrorItem item = new ErrorItem();
    item.setKey("field");
    item.setValue(Arrays.asList("error1", "error2"));

    assertEquals("field", item.getKey());
    assertEquals(Arrays.asList("error1", "error2"), item.getValue());
  }

  @Test
  public void errorItem_full_constructor() {
    ErrorItem item = new ErrorItem("field", Arrays.asList("error1"));
    assertEquals("field", item.getKey());
  }

  @Test
  public void errorItem_toString() {
    ErrorItem item = ErrorItem.newBuilder().key("k").build();
    String str = item.toString();
    assertNotNull(str);
    assertTrue(str.contains("ErrorItem{"));
  }

  @Test
  public void errorItem_equals_and_hashCode() {
    ErrorItem i1 = ErrorItem.newBuilder().key("k").build();
    ErrorItem i2 = ErrorItem.newBuilder().key("k").build();
    ErrorItem i3 = ErrorItem.newBuilder().key("other").build();

    assertEquals(i1, i2);
    assertEquals(i1.hashCode(), i2.hashCode());
    assertNotEquals(i1, i3);
  }

  // ========== Error ==========

  @Test
  public void error_default_constructor_and_setters() {
    Error error = new Error();
    List<ErrorItem> items =
        Arrays.asList(ErrorItem.newBuilder().key("k").value(Arrays.asList("v")).build());

    error.setMessage("BAD_REQUEST");
    error.setErrors(items);

    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(items, error.getErrors());
  }

  @Test
  public void error_full_constructor() {
    List<ErrorItem> items =
        Arrays.asList(ErrorItem.newBuilder().key("k").value(Arrays.asList("v")).build());
    Error error = new Error("BAD_REQUEST", items);

    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(items, error.getErrors());
  }

  @Test
  public void error_toString() {
    Error error = Error.newBuilder().message("msg").build();
    String str = error.toString();
    assertNotNull(str);
    assertTrue(str.contains("Error{"));
  }

  @Test
  public void error_equals_and_hashCode() {
    Error e1 = Error.newBuilder().message("m").build();
    Error e2 = Error.newBuilder().message("m").build();
    Error e3 = Error.newBuilder().message("other").build();

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
    assertNotEquals(e1, e3);
  }

  // ========== CreateArticleInput ==========

  @Test
  public void createArticleInput_default_constructor_and_setters() {
    CreateArticleInput input = new CreateArticleInput();
    input.setTitle("title");
    input.setDescription("desc");
    input.setBody("body");
    input.setTagList(Arrays.asList("java"));

    assertEquals("title", input.getTitle());
    assertEquals("desc", input.getDescription());
    assertEquals("body", input.getBody());
    assertEquals(Arrays.asList("java"), input.getTagList());
  }

  @Test
  public void createArticleInput_full_constructor() {
    CreateArticleInput input = new CreateArticleInput("body", "desc", Arrays.asList("j"), "title");
    assertEquals("title", input.getTitle());
  }

  @Test
  public void createArticleInput_toString() {
    CreateArticleInput input = CreateArticleInput.newBuilder().title("t").build();
    String str = input.toString();
    assertNotNull(str);
    assertTrue(str.contains("CreateArticleInput{"));
  }

  @Test
  public void createArticleInput_equals_and_hashCode() {
    CreateArticleInput i1 = CreateArticleInput.newBuilder().title("t").build();
    CreateArticleInput i2 = CreateArticleInput.newBuilder().title("t").build();
    CreateArticleInput i3 = CreateArticleInput.newBuilder().title("other").build();

    assertEquals(i1, i2);
    assertEquals(i1.hashCode(), i2.hashCode());
    assertNotEquals(i1, i3);
  }

  // ========== UpdateArticleInput ==========

  @Test
  public void updateArticleInput_default_constructor_and_setters() {
    UpdateArticleInput input = new UpdateArticleInput();
    input.setTitle("title");
    input.setDescription("desc");
    input.setBody("body");

    assertEquals("title", input.getTitle());
    assertEquals("desc", input.getDescription());
    assertEquals("body", input.getBody());
  }

  @Test
  public void updateArticleInput_full_constructor() {
    UpdateArticleInput input = new UpdateArticleInput("body", "desc", "title");
    assertEquals("title", input.getTitle());
  }

  @Test
  public void updateArticleInput_toString() {
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("t").build();
    String str = input.toString();
    assertNotNull(str);
    assertTrue(str.contains("UpdateArticleInput{"));
  }

  @Test
  public void updateArticleInput_equals_and_hashCode() {
    UpdateArticleInput i1 = UpdateArticleInput.newBuilder().title("t").build();
    UpdateArticleInput i2 = UpdateArticleInput.newBuilder().title("t").build();
    UpdateArticleInput i3 = UpdateArticleInput.newBuilder().title("other").build();

    assertEquals(i1, i2);
    assertEquals(i1.hashCode(), i2.hashCode());
    assertNotEquals(i1, i3);
  }

  // ========== CreateUserInput ==========

  @Test
  public void createUserInput_default_constructor_and_setters() {
    CreateUserInput input = new CreateUserInput();
    input.setEmail("test@test.com");
    input.setUsername("user1");
    input.setPassword("pass");

    assertEquals("test@test.com", input.getEmail());
    assertEquals("user1", input.getUsername());
    assertEquals("pass", input.getPassword());
  }

  @Test
  public void createUserInput_full_constructor() {
    CreateUserInput input = new CreateUserInput("test@test.com", "user1", "pass");
    assertEquals("test@test.com", input.getEmail());
  }

  @Test
  public void createUserInput_toString() {
    CreateUserInput input = CreateUserInput.newBuilder().email("e").build();
    String str = input.toString();
    assertNotNull(str);
    assertTrue(str.contains("CreateUserInput{"));
  }

  @Test
  public void createUserInput_equals_and_hashCode() {
    CreateUserInput i1 = CreateUserInput.newBuilder().email("e").build();
    CreateUserInput i2 = CreateUserInput.newBuilder().email("e").build();
    CreateUserInput i3 = CreateUserInput.newBuilder().email("other").build();

    assertEquals(i1, i2);
    assertEquals(i1.hashCode(), i2.hashCode());
    assertNotEquals(i1, i3);
  }

  // ========== UpdateUserInput ==========

  @Test
  public void updateUserInput_default_constructor_and_setters() {
    UpdateUserInput input = new UpdateUserInput();
    input.setEmail("test@test.com");
    input.setUsername("user1");
    input.setPassword("pass");
    input.setBio("bio");
    input.setImage("img.png");

    assertEquals("test@test.com", input.getEmail());
    assertEquals("user1", input.getUsername());
    assertEquals("pass", input.getPassword());
    assertEquals("bio", input.getBio());
    assertEquals("img.png", input.getImage());
  }

  @Test
  public void updateUserInput_full_constructor() {
    UpdateUserInput input = new UpdateUserInput("test@test.com", "user1", "pass", "img", "bio");
    assertEquals("test@test.com", input.getEmail());
    assertEquals("bio", input.getBio());
    assertEquals("img", input.getImage());
  }

  @Test
  public void updateUserInput_toString() {
    UpdateUserInput input = UpdateUserInput.newBuilder().email("e").build();
    String str = input.toString();
    assertNotNull(str);
    assertTrue(str.contains("UpdateUserInput{"));
  }

  @Test
  public void updateUserInput_equals_and_hashCode() {
    UpdateUserInput i1 = UpdateUserInput.newBuilder().email("e").bio("b").build();
    UpdateUserInput i2 = UpdateUserInput.newBuilder().email("e").bio("b").build();
    UpdateUserInput i3 = UpdateUserInput.newBuilder().email("other").bio("b").build();

    assertEquals(i1, i2);
    assertEquals(i1.hashCode(), i2.hashCode());
    assertNotEquals(i1, i3);
  }
}
