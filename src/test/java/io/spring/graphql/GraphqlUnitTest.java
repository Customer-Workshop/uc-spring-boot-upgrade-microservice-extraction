package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.ProfileQueryService;
import io.spring.application.UserQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.user.UserService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.exception.GraphQLCustomizeExceptionHandler;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.UpdateArticleInput;
import io.spring.graphql.types.UpdateUserInput;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

class GraphqlUnitTest {
  private final User user = new User("user@test.com", "user", "password", "bio", "image");
  private final Article article = new Article("title", "description", "body", Arrays.asList("java"), user.getId());
  private final ArticleData articleData =
      new ArticleData(
          article.getId(),
          article.getSlug(),
          article.getTitle(),
          article.getDescription(),
          article.getBody(),
          false,
          1,
          new DateTime(),
          new DateTime(),
          Arrays.asList("java"),
          new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
  private final Comment comment = new Comment("body", user.getId(), article.getId());
  private final CommentData commentData =
      new CommentData(
          comment.getId(),
          comment.getBody(),
          article.getId(),
          new DateTime(),
          new DateTime(),
          new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));

  @Test
  void securityUtilHandlesAnonymousAndAuthenticatedUsers() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));
    assertTrue(SecurityUtil.getCurrentUser().isEmpty());
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
    assertEquals(user, SecurityUtil.getCurrentUser().orElseThrow());
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));
  }

  @Test
  void articleDatafetcherQueriesAndRejectsInvalidPagination() {
    ArticleQueryService service = mock(ArticleQueryService.class);
    UserRepository users = mock(UserRepository.class);
    ArticleDatafetcher fetcher = new ArticleDatafetcher(service, users);
    CursorPager<ArticleData> page = new CursorPager<>(Collections.singletonList(articleData), CursorPager.Direction.NEXT, true);
    when(service.findUserFeedWithCursor(any(), any())).thenReturn(page);
    when(service.findRecentArticlesWithCursor(any(), any(), any(), any(), any())).thenReturn(page);
    Profile source = Profile.newBuilder().username(user.getUsername()).build();
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(source);
    when(users.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertEquals(1, fetcher.getFeed(1, null, null, null, dfe).getData().getEdges().size());
      assertEquals(1, fetcher.getFeed(null, null, 1, null, dfe).getData().getEdges().size());
      assertEquals(1, fetcher.userFeed(1, null, null, null, dfe).getData().getEdges().size());
      assertEquals(1, fetcher.userFavorites(1, null, null, null, dfe).getData().getEdges().size());
      assertEquals(1, fetcher.userArticles(1, null, null, null, dfe).getData().getEdges().size());
      assertEquals(1, fetcher.getArticles(1, null, null, null, null, null, null, dfe).getData().getEdges().size());
      assertThrows(IllegalArgumentException.class, () -> fetcher.getFeed(null, null, null, null, dfe));
    }
    when(users.findByUsername(user.getUsername())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> fetcher.userFeed(1, null, null, null, dfe));
  }

  @Test
  void articleDatafetcherResolvesArticlesAndMissingData() {
    ArticleQueryService service = mock(ArticleQueryService.class);
    ArticleDatafetcher fetcher = new ArticleDatafetcher(service, mock(UserRepository.class));
    DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
    when(environment.getLocalContext()).thenReturn(article);
    when(service.findById(article.getId(), null)).thenReturn(Optional.of(articleData));
    when(service.findBySlug(article.getSlug(), null)).thenReturn(Optional.of(articleData));
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertEquals(article.getSlug(), fetcher.getArticle(environment).getData().getSlug());
      assertEquals(article.getSlug(), fetcher.findArticleBySlug(article.getSlug()).getData().getSlug());
      when(environment.getLocalContext()).thenReturn(commentData);
      assertEquals(article.getSlug(), fetcher.getCommentArticle(environment).getData().getSlug());
      when(environment.getLocalContext()).thenReturn(article);
      when(service.findById(article.getId(), null)).thenReturn(Optional.empty());
      assertThrows(ResourceNotFoundException.class, () -> fetcher.getArticle(environment));
      assertThrows(ResourceNotFoundException.class, () -> fetcher.findArticleBySlug("missing"));
    }
  }

  @Test
  void commentDatafetcherQueriesAndBuildsComments() {
    CommentQueryService service = mock(CommentQueryService.class);
    CommentDatafetcher fetcher = new CommentDatafetcher(service);
    DataFetcherResult<io.spring.graphql.types.Comment> one =
        fetcher.getComment(dgsEnvironmentWithLocalContext(commentData));
    assertEquals(comment.getId(), one.getData().getId());
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Article source = article;
    when(dfe.getSource()).thenReturn(io.spring.graphql.types.Article.newBuilder().slug(article.getSlug()).build());
    when(dfe.getLocalContext()).thenReturn(new HashMap<String, ArticleData>() {{ put(article.getSlug(), articleData); }});
    when(service.findByArticleIdWithCursor(eq(article.getId()), any(), any()))
        .thenReturn(new CursorPager<>(Collections.singletonList(commentData), CursorPager.Direction.NEXT, false));
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertEquals(1, fetcher.articleComments(1, null, null, null, dfe).getData().getEdges().size());
      assertThrows(IllegalArgumentException.class, () -> fetcher.articleComments(null, null, null, null, dfe));
    }
  }

  @Test
  void articleMutationCoversAuthzAndCrudBranches() {
    ArticleCommandService command = mock(ArticleCommandService.class);
    ArticleFavoriteRepository favorites = mock(ArticleFavoriteRepository.class);
    ArticleRepository articles = mock(ArticleRepository.class);
    ArticleMutation mutation = new ArticleMutation(command, favorites, articles);
    CreateArticleInput input = CreateArticleInput.newBuilder().title("new").description("d").body("b").build();
    when(command.createArticle(any(), eq(user))).thenReturn(article);
    when(articles.findBySlug(article.getSlug())).thenReturn(Optional.of(article));
    when(command.updateArticle(eq(article), any())).thenReturn(article);
    when(favorites.find(article.getId(), user.getId())).thenReturn(Optional.of(new ArticleFavorite(article.getId(), user.getId())));
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertEquals(article, mutation.createArticle(input).getLocalContext());
      assertEquals(article, mutation.updateArticle(article.getSlug(), UpdateArticleInput.newBuilder().title("x").build()).getLocalContext());
      mutation.favoriteArticle(article.getSlug());
      mutation.unfavoriteArticle(article.getSlug());
      assertTrue(mutation.deleteArticle(article.getSlug()).getSuccess());
      verify(articles).remove(article);
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertThrows(AuthenticationException.class, () -> mutation.createArticle(input));
    }
    when(articles.findBySlug("missing")).thenReturn(Optional.empty());
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertThrows(ResourceNotFoundException.class, () -> mutation.updateArticle("missing", UpdateArticleInput.newBuilder().build()));
      assertThrows(ResourceNotFoundException.class, () -> mutation.favoriteArticle("missing"));
    }
    User owner = new User("owner@test.com", "owner", "p", "", "");
    Article ownedByOther = new Article("other", "d", "b", Collections.emptyList(), owner.getId());
    when(articles.findBySlug(ownedByOther.getSlug())).thenReturn(Optional.of(ownedByOther));
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertThrows(NoAuthorizationException.class, () -> mutation.deleteArticle(ownedByOther.getSlug()));
    }
  }

  @Test
  void commentMutationCoversAuthzAndCrudBranches() {
    ArticleRepository articles = mock(ArticleRepository.class);
    CommentRepository comments = mock(CommentRepository.class);
    CommentQueryService queries = mock(CommentQueryService.class);
    CommentMutation mutation = new CommentMutation(articles, comments, queries);
    when(articles.findBySlug(article.getSlug())).thenReturn(Optional.of(article));
    when(queries.findById(any(), eq(user))).thenReturn(Optional.of(commentData));
    when(comments.findById(article.getId(), comment.getId())).thenReturn(Optional.of(comment));
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertEquals(commentData, mutation.createComment(article.getSlug(), "body").getLocalContext());
      assertTrue(mutation.removeComment(article.getSlug(), comment.getId()).getSuccess());
      verify(comments).remove(comment);
    }
    when(comments.findById(article.getId(), "missing")).thenReturn(Optional.empty());
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertThrows(ResourceNotFoundException.class, () -> mutation.removeComment(article.getSlug(), "missing"));
    }
    User other = new User("other@test.com", "other", "p", "", "");
    Article otherArticle = new Article("other article", "d", "b", Collections.emptyList(), other.getId());
    Comment otherComment = new Comment("x", other.getId(), article.getId());
    when(articles.findBySlug(otherArticle.getSlug())).thenReturn(Optional.of(otherArticle));
    when(comments.findById(otherArticle.getId(), otherComment.getId())).thenReturn(Optional.of(otherComment));
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertThrows(NoAuthorizationException.class, () -> mutation.removeComment(otherArticle.getSlug(), otherComment.getId()));
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertThrows(AuthenticationException.class, () -> mutation.createComment(article.getSlug(), "x"));
    }
  }

  @Test
  void relationAndUserMutationsCoverAuthenticationAndNotFound() {
    UserRepository users = mock(UserRepository.class);
    ProfileQueryService profiles = mock(ProfileQueryService.class);
    RelationMutation relation = new RelationMutation(users, profiles);
    User target = new User("target@test.com", "target", "p", "", "");
    ProfileData profileData = new ProfileData(target.getId(), target.getUsername(), "bio", "image", true);
    when(users.findByUsername("target")).thenReturn(Optional.of(target));
    when(profiles.findByUsername("target", user)).thenReturn(Optional.of(profileData));
    when(users.findRelation(user.getId(), user.getId())).thenReturn(Optional.empty());
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertEquals("target", relation.follow("target").getProfile().getUsername());
      assertThrows(ResourceNotFoundException.class, () -> relation.unfollow("target"));
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertThrows(AuthenticationException.class, () -> relation.follow("target"));
    }
    when(users.findByUsername("missing")).thenReturn(Optional.empty());
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertThrows(ResourceNotFoundException.class, () -> relation.follow("missing"));
    }

    UserRepository userRepository = mock(UserRepository.class);
    UserService userService = mock(UserService.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);
    UserMutation mutation = new UserMutation(userRepository, encoder, userService);
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(encoder.matches("password", user.getPassword())).thenReturn(true);
    when(userService.createUser(any())).thenReturn(user);
    assertEquals(user, mutation.login("password", user.getEmail()).getLocalContext());
    assertEquals(user, mutation.createUser(CreateUserInput.newBuilder().email("e").username("u").password("p").build()).getLocalContext());
    assertThrows(Exception.class, () -> mutation.login("bad", user.getEmail()));
    when(userRepository.findByEmail("missing")).thenReturn(Optional.empty());
    assertThrows(Exception.class, () -> mutation.login("p", "missing"));
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));
    assertEquals(null, mutation.updateUser(UpdateUserInput.newBuilder().build()));
  }

  @Test
  void meAndProfileDatafetchersCoverAuthenticatedAndMissingData() {
    UserQueryService users = mock(UserQueryService.class);
    JwtService jwt = mock(JwtService.class);
    MeDatafetcher me = new MeDatafetcher(users, jwt);
    UserData data = new UserData(user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage());
    when(users.findById(user.getId())).thenReturn(Optional.of(data));
    when(jwt.toToken(user)).thenReturn("jwt");
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));
    assertEquals(null, me.getMe("Token jwt", mock(DataFetchingEnvironment.class)));
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
    assertEquals(user.getUsername(), me.getMe("Token jwt", mock(DataFetchingEnvironment.class)).getData().getUsername());
    assertEquals(user.getUsername(), me.getUserPayloadUser(environmentWithLocalContext(user)).getData().getUsername());
    when(users.findById(user.getId())).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> me.getMe("Token jwt", mock(DataFetchingEnvironment.class)));
    SecurityContextHolder.clearContext();

    ProfileQueryService profiles = mock(ProfileQueryService.class);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));
    ProfileData profile = new ProfileData(user.getId(), user.getUsername(), "bio", "image", true);
    when(profiles.findByUsername(user.getUsername(), null)).thenReturn(Optional.of(profile));
    ProfileDatafetcher fetcher = new ProfileDatafetcher(profiles);
    assertEquals(user.getUsername(), fetcher.getUserProfile(environmentWithLocalContext(user)).getUsername());
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn(user.getUsername());
    assertEquals(user.getUsername(), fetcher.queryProfile("ignored", dfe).getProfile().getUsername());
    when(profiles.findByUsername(user.getUsername(), null)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> fetcher.getUserProfile(environmentWithLocalContext(user)));
  }

  private DataFetchingEnvironment environmentWithLocalContext(Object context) {
    DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
    when(environment.getLocalContext()).thenReturn(context);
    return environment;
  }

  private DgsDataFetchingEnvironment dgsEnvironmentWithLocalContext(Object context) {
    DgsDataFetchingEnvironment environment = mock(DgsDataFetchingEnvironment.class);
    when(environment.getLocalContext()).thenReturn(context);
    return environment;
  }
}
