package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleMutation articleMutation;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_create_article_success() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article =
        new Article(
            "Test Title", "Test Desc", "Test Body", Arrays.asList("java", "spring"), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    var result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  public void should_create_article_with_null_taglist() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .build();

    Article article =
        new Article("Test Title", "Test Desc", "Test Body", Arrays.asList(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    var result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  public void should_throw_authentication_when_creating_article_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Test").description("Desc").body("Body").build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  public void should_update_article_success() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList("java"), user.getId());
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("New Body")
            .description("New Desc")
            .build();

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    var result = articleMutation.updateArticle(article.getSlug(), input);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  public void should_throw_not_found_when_updating_nonexistent_article() {
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("New Title").build();

    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.updateArticle("nonexistent", input));
  }

  @Test
  public void should_throw_no_authorization_when_updating_others_article() {
    User otherUser = new User("other@test.com", "other", "password", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), otherUser.getId());
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("New Title").build();

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), input));
  }

  @Test
  public void should_throw_authentication_when_updating_article_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("New").build();

    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    assertThrows(
        AuthenticationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), input));
  }

  @Test
  public void should_favorite_article_success() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    var result = articleMutation.favoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  public void should_throw_not_found_when_favoriting_nonexistent_article() {
    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("nonexistent"));
  }

  @Test
  public void should_throw_authentication_when_favoriting_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("slug"));
  }

  @Test
  public void should_unfavorite_article_success() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(favorite));

    var result = articleMutation.unfavoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(eq(favorite));
  }

  @Test
  public void should_unfavorite_article_when_not_favorited() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.empty());

    var result = articleMutation.unfavoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  public void should_delete_article_success() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle(article.getSlug());

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(eq(article));
  }

  @Test
  public void should_throw_no_authorization_when_deleting_others_article() {
    User otherUser = new User("other@test.com", "other", "password", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), otherUser.getId());

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle(article.getSlug()));
  }

  @Test
  public void should_throw_not_found_when_deleting_nonexistent_article() {
    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("nonexistent"));
  }

  @Test
  public void should_throw_authentication_when_deleting_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("slug"));
  }
}
