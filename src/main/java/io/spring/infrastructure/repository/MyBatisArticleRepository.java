package io.spring.infrastructure.repository;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.infrastructure.mybatis.mapper.ArticleMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Persists articles and their tags through MyBatis mappers. */
@Repository
public class MyBatisArticleRepository implements ArticleRepository {
  private ArticleMapper articleMapper;

  public MyBatisArticleRepository(ArticleMapper articleMapper) {
    this.articleMapper = articleMapper;
  }

  /** Inserts new articles and missing tags, or updates an existing article. */
  @Override
  @Transactional
  public void save(Article article) {
    if (articleMapper.findById(article.getId()) == null) {
      createNew(article);
    } else {
      articleMapper.update(article);
    }
  }

  private void createNew(Article article) {
    for (Tag tag : article.getTags()) {
      Tag targetTag =
          Optional.ofNullable(articleMapper.findTag(tag.getName()))
              .orElseGet(
                  () -> {
                    articleMapper.insertTag(tag);
                    return tag;
                  });
      articleMapper.insertArticleTagRelation(article.getId(), targetTag.getId());
    }
    articleMapper.insert(article);
  }

  /** Finds an article by identifier. */
  @Override
  public Optional<Article> findById(String id) {
    return Optional.ofNullable(articleMapper.findById(id));
  }

  /** Finds an article by slug. */
  @Override
  public Optional<Article> findBySlug(String slug) {
    return Optional.ofNullable(articleMapper.findBySlug(slug));
  }

  /** Deletes an article by identifier. */
  @Override
  public void remove(Article article) {
    articleMapper.delete(article.getId());
  }
}
