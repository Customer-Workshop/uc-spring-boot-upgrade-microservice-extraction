package io.spring.articleservice.service;

import io.spring.articleservice.dto.ArticleDto;
import io.spring.articleservice.exception.ResourceNotFoundException;
import io.spring.articleservice.model.Article;
import io.spring.articleservice.model.ArticleFavorite;
import io.spring.articleservice.repository.ArticleFavoriteRepository;
import io.spring.articleservice.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final ArticleFavoriteRepository favoriteRepository;
  private final ArticleRepository articleRepository;
  private final ArticleService articleService;

  @Transactional
  public ArticleDto favoriteArticle(String slug, String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    if (!favoriteRepository.existsByArticleIdAndUserId(article.getId(), userId)) {
      favoriteRepository.save(new ArticleFavorite(article.getId(), userId));
    }

    return articleService.toDto(article, userId);
  }

  @Transactional
  public ArticleDto unfavoriteArticle(String slug, String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    favoriteRepository
        .findByArticleIdAndUserId(article.getId(), userId)
        .ifPresent(favoriteRepository::delete);

    return articleService.toDto(article, userId);
  }
}
