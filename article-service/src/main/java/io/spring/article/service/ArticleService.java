package io.spring.article.service;

import io.spring.article.domain.Article;
import io.spring.article.domain.Tag;
import io.spring.article.repository.ArticleMapper;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ArticleService {
  private ArticleMapper articleMapper;

  @Transactional
  public Article createArticle(String title, String description, String body,
      java.util.List<String> tagList, String userId) {
    Article article = new Article(title, description, body,
        tagList != null ? tagList : java.util.Collections.emptyList(), userId);
    articleMapper.insert(article);
    for (Tag tag : article.getTags()) {
      Tag existingTag = articleMapper.findTag(tag.getName());
      if (existingTag != null) {
        articleMapper.insertArticleTagRelation(article.getId(), existingTag.getId());
      } else {
        articleMapper.insertTag(tag);
        articleMapper.insertArticleTagRelation(article.getId(), tag.getId());
      }
    }
    return article;
  }

  public Optional<Article> findById(String id) {
    return Optional.ofNullable(articleMapper.findById(id));
  }

  public Optional<Article> findBySlug(String slug) {
    return Optional.ofNullable(articleMapper.findBySlug(slug));
  }

  @Transactional
  public Article updateArticle(String slug, String title, String description, String body) {
    Article article = articleMapper.findBySlug(slug);
    if (article == null) {
      return null;
    }
    article.update(title, description, body);
    articleMapper.update(article);
    return article;
  }

  @Transactional
  public boolean deleteArticle(String slug) {
    Article article = articleMapper.findBySlug(slug);
    if (article == null) {
      return false;
    }
    articleMapper.delete(article.getId());
    return true;
  }
}
