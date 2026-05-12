package io.spring.graphql.types;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class TypesBuildersTest {

  @Test
  public void should_build_article() {
    Article article =
        Article.newBuilder()
            .body("body")
            .createdAt("2023-01-01")
            .description("desc")
            .favorited(true)
            .favoritesCount(5)
            .slug("test-slug")
            .tagList(Arrays.asList("java", "spring"))
            .title("Test Title")
            .updatedAt("2023-01-02")
            .build();

    assertEquals("body", article.getBody());
    assertEquals("2023-01-01", article.getCreatedAt());
    assertEquals("desc", article.getDescription());
    assertTrue(article.getFavorited());
    assertEquals(5, article.getFavoritesCount());
    assertEquals("test-slug", article.getSlug());
    assertEquals(2, article.getTagList().size());
    assertEquals("Test Title", article.getTitle());
    assertEquals("2023-01-02", article.getUpdatedAt());
    assertNotNull(article.toString());
  }

  @Test
  public void should_build_user() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    User user =
        User.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .token("token")
            .profile(profile)
            .build();

    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("token", user.getToken());
    assertNotNull(user.getProfile());
    assertNotNull(user.toString());
  }

  @Test
  public void should_build_profile() {
    Profile profile =
        Profile.newBuilder().username("testuser").bio("bio").image("image").following(true).build();

    assertEquals("testuser", profile.getUsername());
    assertEquals("bio", profile.getBio());
    assertEquals("image", profile.getImage());
    assertTrue(profile.getFollowing());
    assertNotNull(profile.toString());
  }

  @Test
  public void should_build_comment() {
    Comment comment =
        Comment.newBuilder()
            .id("commentId")
            .body("comment body")
            .createdAt("2023-01-01")
            .updatedAt("2023-01-02")
            .build();

    assertEquals("commentId", comment.getId());
    assertEquals("comment body", comment.getBody());
    assertEquals("2023-01-01", comment.getCreatedAt());
    assertEquals("2023-01-02", comment.getUpdatedAt());
    assertNotNull(comment.toString());
  }

  @Test
  public void should_build_article_payload() {
    Article article = Article.newBuilder().slug("slug").build();
    ArticlePayload payload = ArticlePayload.newBuilder().article(article).build();

    assertNotNull(payload);
    assertEquals("slug", payload.getArticle().getSlug());
    assertNotNull(payload.toString());
  }

  @Test
  public void should_build_user_payload() {
    User user = User.newBuilder().email("e").build();
    UserPayload payload = UserPayload.newBuilder().user(user).build();
    assertNotNull(payload);
    assertNotNull(payload.toString());
    assertTrue(payload instanceof UserResult);
  }

  @Test
  public void should_build_profile_payload() {
    Profile profile = Profile.newBuilder().username("test").build();
    ProfilePayload payload = ProfilePayload.newBuilder().profile(profile).build();

    assertNotNull(payload);
    assertEquals("test", payload.getProfile().getUsername());
    assertNotNull(payload.toString());
  }

  @Test
  public void should_build_comment_payload() {
    Comment comment = Comment.newBuilder().id("id").build();
    CommentPayload payload = CommentPayload.newBuilder().comment(comment).build();
    assertNotNull(payload);
    assertNotNull(payload.toString());
  }

  @Test
  public void should_build_deletion_status() {
    DeletionStatus status = DeletionStatus.newBuilder().success(true).build();
    assertTrue(status.getSuccess());
    assertNotNull(status.toString());
  }

  @Test
  public void should_build_article_edge() {
    Article article = Article.newBuilder().slug("slug").build();
    ArticleEdge edge = ArticleEdge.newBuilder().cursor("cursor1").node(article).build();

    assertEquals("cursor1", edge.getCursor());
    assertNotNull(edge.getNode());
    assertNotNull(edge.toString());
  }

  @Test
  public void should_build_comment_edge() {
    Comment comment = Comment.newBuilder().id("id").build();
    CommentEdge edge = CommentEdge.newBuilder().cursor("cursor1").node(comment).build();

    assertEquals("cursor1", edge.getCursor());
    assertNotNull(edge.getNode());
    assertNotNull(edge.toString());
  }

  @Test
  public void should_build_articles_connection() {
    ArticlesConnection conn =
        ArticlesConnection.newBuilder().edges(Collections.emptyList()).build();

    assertNotNull(conn);
    assertTrue(conn.getEdges().isEmpty());
    assertNotNull(conn.toString());
  }

  @Test
  public void should_build_comments_connection() {
    CommentsConnection conn =
        CommentsConnection.newBuilder().edges(Collections.emptyList()).build();

    assertNotNull(conn);
    assertTrue(conn.getEdges().isEmpty());
    assertNotNull(conn.toString());
  }

  @Test
  public void should_build_create_article_input() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test")
            .description("Desc")
            .body("Body")
            .tagList(Arrays.asList("java"))
            .build();

    assertEquals("Test", input.getTitle());
    assertEquals("Desc", input.getDescription());
    assertEquals("Body", input.getBody());
    assertEquals(1, input.getTagList().size());
    assertNotNull(input.toString());
  }

  @Test
  public void should_build_update_article_input() {
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .description("New Desc")
            .body("New Body")
            .build();

    assertEquals("New Title", input.getTitle());
    assertEquals("New Desc", input.getDescription());
    assertEquals("New Body", input.getBody());
    assertNotNull(input.toString());
  }

  @Test
  public void should_build_create_user_input() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("password")
            .build();

    assertEquals("test@test.com", input.getEmail());
    assertEquals("testuser", input.getUsername());
    assertEquals("password", input.getPassword());
    assertNotNull(input.toString());
  }

  @Test
  public void should_build_update_user_input() {
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("updated@test.com")
            .username("updated")
            .password("newpass")
            .bio("new bio")
            .image("new image")
            .build();

    assertEquals("updated@test.com", input.getEmail());
    assertEquals("updated", input.getUsername());
    assertEquals("newpass", input.getPassword());
    assertEquals("new bio", input.getBio());
    assertEquals("new image", input.getImage());
    assertNotNull(input.toString());
  }

  @Test
  public void should_build_error() {
    ErrorItem item =
        ErrorItem.newBuilder().key("email").value(Arrays.asList("already exists")).build();

    assertEquals("email", item.getKey());
    assertEquals(1, item.getValue().size());
    assertNotNull(item.toString());

    Error error = Error.newBuilder().message("BAD_REQUEST").errors(Arrays.asList(item)).build();

    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(1, error.getErrors().size());
    assertNotNull(error.toString());
    assertTrue(error instanceof UserResult);
  }

  @Test
  public void should_build_page_info() {
    PageInfo pageInfo =
        PageInfo.newBuilder()
            .endCursor("end")
            .startCursor("start")
            .hasNextPage(true)
            .hasPreviousPage(false)
            .build();

    assertEquals("end", pageInfo.getEndCursor());
    assertEquals("start", pageInfo.getStartCursor());
    assertTrue(pageInfo.getHasNextPage());
    assertFalse(pageInfo.getHasPreviousPage());
    assertNotNull(pageInfo.toString());
  }

  @Test
  public void should_build_article_with_setters() {
    Article article = new Article();
    article.setBody("body");
    article.setCreatedAt("2023-01-01");
    article.setDescription("desc");
    article.setFavorited(true);
    article.setFavoritesCount(3);
    article.setSlug("slug");
    article.setTagList(Arrays.asList("tag"));
    article.setTitle("title");
    article.setUpdatedAt("2023-01-02");

    assertEquals("body", article.getBody());
    assertEquals("2023-01-01", article.getCreatedAt());
    assertEquals("desc", article.getDescription());
    assertTrue(article.getFavorited());
    assertEquals(3, article.getFavoritesCount());
    assertEquals("slug", article.getSlug());
    assertEquals("title", article.getTitle());
    assertEquals("2023-01-02", article.getUpdatedAt());
  }

  @Test
  public void should_build_user_with_setters() {
    User user = new User();
    user.setEmail("e@e.com");
    user.setUsername("u");
    user.setToken("t");
    user.setProfile(null);

    assertEquals("e@e.com", user.getEmail());
    assertEquals("u", user.getUsername());
    assertEquals("t", user.getToken());
    assertNull(user.getProfile());
  }

  @Test
  public void should_build_profile_with_setters() {
    Profile profile = new Profile();
    profile.setUsername("u");
    profile.setBio("b");
    profile.setImage("i");
    profile.setFollowing(false);

    assertEquals("u", profile.getUsername());
    assertEquals("b", profile.getBio());
    assertEquals("i", profile.getImage());
    assertFalse(profile.getFollowing());
  }

  @Test
  public void should_build_comment_with_setters() {
    Comment comment = new Comment();
    comment.setId("id");
    comment.setBody("body");
    comment.setCreatedAt("c");
    comment.setUpdatedAt("u");

    assertEquals("id", comment.getId());
    assertEquals("body", comment.getBody());
    assertEquals("c", comment.getCreatedAt());
    assertEquals("u", comment.getUpdatedAt());
  }

  @Test
  public void should_build_deletion_status_with_setters() {
    DeletionStatus ds = new DeletionStatus();
    ds.setSuccess(false);
    assertFalse(ds.getSuccess());
  }

  @Test
  public void should_build_edge_with_setters() {
    ArticleEdge ae = new ArticleEdge();
    ae.setNode(Article.newBuilder().slug("s").build());
    ae.setCursor("c");
    assertEquals("c", ae.getCursor());
    assertNotNull(ae.getNode());

    CommentEdge ce = new CommentEdge();
    ce.setNode(Comment.newBuilder().id("id").build());
    ce.setCursor("c");
    assertEquals("c", ce.getCursor());
    assertNotNull(ce.getNode());
  }

  @Test
  public void should_build_connections_with_setters() {
    ArticlesConnection ac = new ArticlesConnection();
    ac.setEdges(Collections.emptyList());
    ac.setPageInfo(null);
    assertTrue(ac.getEdges().isEmpty());
    assertNull(ac.getPageInfo());

    CommentsConnection cc = new CommentsConnection();
    cc.setEdges(Collections.emptyList());
    cc.setPageInfo(null);
    assertTrue(cc.getEdges().isEmpty());
    assertNull(cc.getPageInfo());
  }

  @Test
  public void should_build_inputs_with_setters() {
    CreateArticleInput cai = new CreateArticleInput();
    cai.setTitle("t");
    cai.setDescription("d");
    cai.setBody("b");
    cai.setTagList(Arrays.asList("tag"));
    assertEquals("t", cai.getTitle());

    UpdateArticleInput uai = new UpdateArticleInput();
    uai.setTitle("t");
    uai.setDescription("d");
    uai.setBody("b");
    assertEquals("t", uai.getTitle());

    CreateUserInput cui = new CreateUserInput();
    cui.setEmail("e");
    cui.setUsername("u");
    cui.setPassword("p");
    assertEquals("e", cui.getEmail());

    UpdateUserInput uui = new UpdateUserInput();
    uui.setEmail("e");
    uui.setUsername("u");
    uui.setPassword("p");
    uui.setBio("b");
    uui.setImage("i");
    assertEquals("e", uui.getEmail());
  }

  @Test
  public void should_build_error_with_setters() {
    ErrorItem item = new ErrorItem();
    item.setKey("k");
    item.setValue(Arrays.asList("v"));
    assertEquals("k", item.getKey());

    Error error = new Error();
    error.setMessage("msg");
    error.setErrors(Arrays.asList(item));
    assertEquals("msg", error.getMessage());
  }

  @Test
  public void should_build_payloads_with_setters() {
    ArticlePayload ap = new ArticlePayload();
    ap.setArticle(null);
    assertNull(ap.getArticle());

    CommentPayload cp = new CommentPayload();
    cp.setComment(null);
    assertNull(cp.getComment());

    UserPayload up = new UserPayload();
    up.setUser(null);
    assertNull(up.getUser());

    ProfilePayload pp = new ProfilePayload();
    pp.setProfile(null);
    assertNull(pp.getProfile());
  }

  @Test
  public void should_build_page_info_with_setters() {
    PageInfo pi = new PageInfo();
    pi.setEndCursor("e");
    pi.setStartCursor("s");
    pi.setHasNextPage(true);
    pi.setHasPreviousPage(false);
    assertEquals("e", pi.getEndCursor());
    assertEquals("s", pi.getStartCursor());
    assertTrue(pi.getHasNextPage());
    assertFalse(pi.getHasPreviousPage());
  }

  @Test
  public void should_test_equals_and_hashcode() {
    Article a1 = Article.newBuilder().slug("slug").title("t").build();
    Article a2 = Article.newBuilder().slug("slug").title("t").build();
    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());

    Profile p1 = Profile.newBuilder().username("u").build();
    Profile p2 = Profile.newBuilder().username("u").build();
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());

    Comment c1 = Comment.newBuilder().id("id").build();
    Comment c2 = Comment.newBuilder().id("id").build();
    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());

    User u1 = User.newBuilder().email("e").build();
    User u2 = User.newBuilder().email("e").build();
    assertEquals(u1, u2);
    assertEquals(u1.hashCode(), u2.hashCode());

    DeletionStatus d1 = DeletionStatus.newBuilder().success(true).build();
    DeletionStatus d2 = DeletionStatus.newBuilder().success(true).build();
    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());

    ErrorItem ei1 = ErrorItem.newBuilder().key("k").build();
    ErrorItem ei2 = ErrorItem.newBuilder().key("k").build();
    assertEquals(ei1, ei2);

    Error e1 = Error.newBuilder().message("m").build();
    Error e2 = Error.newBuilder().message("m").build();
    assertEquals(e1, e2);

    PageInfo pi1 = PageInfo.newBuilder().endCursor("e").build();
    PageInfo pi2 = PageInfo.newBuilder().endCursor("e").build();
    assertEquals(pi1, pi2);

    ArticleEdge ae1 = ArticleEdge.newBuilder().cursor("c").build();
    ArticleEdge ae2 = ArticleEdge.newBuilder().cursor("c").build();
    assertEquals(ae1, ae2);

    CommentEdge ce1 = CommentEdge.newBuilder().cursor("c").build();
    CommentEdge ce2 = CommentEdge.newBuilder().cursor("c").build();
    assertEquals(ce1, ce2);

    ArticlesConnection ac1 = ArticlesConnection.newBuilder().edges(Collections.emptyList()).build();
    ArticlesConnection ac2 = ArticlesConnection.newBuilder().edges(Collections.emptyList()).build();
    assertEquals(ac1, ac2);

    CommentsConnection cc1 = CommentsConnection.newBuilder().edges(Collections.emptyList()).build();
    CommentsConnection cc2 = CommentsConnection.newBuilder().edges(Collections.emptyList()).build();
    assertEquals(cc1, cc2);

    ArticlePayload ap1 = ArticlePayload.newBuilder().build();
    ArticlePayload ap2 = ArticlePayload.newBuilder().build();
    assertEquals(ap1, ap2);

    CommentPayload cop1 = CommentPayload.newBuilder().build();
    CommentPayload cop2 = CommentPayload.newBuilder().build();
    assertEquals(cop1, cop2);

    UserPayload up1 = UserPayload.newBuilder().build();
    UserPayload up2 = UserPayload.newBuilder().build();
    assertEquals(up1, up2);

    ProfilePayload pp1 = ProfilePayload.newBuilder().build();
    ProfilePayload pp2 = ProfilePayload.newBuilder().build();
    assertEquals(pp1, pp2);

    CreateArticleInput cai1 = CreateArticleInput.newBuilder().title("t").build();
    CreateArticleInput cai2 = CreateArticleInput.newBuilder().title("t").build();
    assertEquals(cai1, cai2);

    UpdateArticleInput uai1 = UpdateArticleInput.newBuilder().title("t").build();
    UpdateArticleInput uai2 = UpdateArticleInput.newBuilder().title("t").build();
    assertEquals(uai1, uai2);

    CreateUserInput cui1 = CreateUserInput.newBuilder().email("e").build();
    CreateUserInput cui2 = CreateUserInput.newBuilder().email("e").build();
    assertEquals(cui1, cui2);

    UpdateUserInput uui1 = UpdateUserInput.newBuilder().email("e").build();
    UpdateUserInput uui2 = UpdateUserInput.newBuilder().email("e").build();
    assertEquals(uui1, uui2);
  }

  @Test
  public void should_test_simple_constructors() {
    DeletionStatus ds = new DeletionStatus(true);
    assertNotNull(ds);

    ErrorItem ei = new ErrorItem("key", Arrays.asList("val"));
    assertNotNull(ei);

    Error error = new Error("message", Arrays.asList(ei));
    assertNotNull(error);

    ArticleEdge ae = new ArticleEdge("cursor", Article.newBuilder().slug("s").build());
    assertNotNull(ae);

    CommentEdge ce = new CommentEdge("cursor", Comment.newBuilder().id("i").build());
    assertNotNull(ce);

    ArticlePayload ap = new ArticlePayload(Article.newBuilder().slug("s").build());
    assertNotNull(ap);

    CommentPayload cop = new CommentPayload(Comment.newBuilder().id("i").build());
    assertNotNull(cop);

    UserPayload up = new UserPayload(User.newBuilder().email("e").build());
    assertNotNull(up);

    ProfilePayload pp = new ProfilePayload(Profile.newBuilder().username("u").build());
    assertNotNull(pp);

    UpdateArticleInput uai = new UpdateArticleInput("body", "desc", "title");
    assertNotNull(uai);

    CreateUserInput cui = new CreateUserInput("email", "password", "username");
    assertNotNull(cui);

    UpdateUserInput uui = new UpdateUserInput("bio", "email", "image", "password", "username");
    assertNotNull(uui);
  }
}
