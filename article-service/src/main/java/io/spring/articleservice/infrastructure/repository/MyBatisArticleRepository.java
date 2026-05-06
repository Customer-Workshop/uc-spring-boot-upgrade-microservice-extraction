package io.spring.articleservice.infrastructure.repository;

import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.core.article.Tag;
import io.spring.articleservice.infrastructure.mybatis.mapper.ArticleMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MyBatisArticleRepository implements ArticleRepository {
  private final ArticleMapper articleMapper;

  public MyBatisArticleRepository(ArticleMapper articleMapper) {
    this.articleMapper = articleMapper;
  }

  @Override
  @Transactional
  public void save(Article article) {
    articleMapper.insert(article);
    for (Tag tag : article.getTags()) {
      Tag existingTag = articleMapper.findTag(tag.getName());
      if (existingTag == null) {
        articleMapper.insertTag(tag);
      } else {
        tag.setId(existingTag.getId());
      }
      articleMapper.insertArticleTagRelation(article.getId(), tag.getId());
    }
  }

  @Override
  public Optional<Article> findById(String id) {
    return Optional.ofNullable(articleMapper.findById(id));
  }

  @Override
  public Optional<Article> findBySlug(String slug) {
    return Optional.ofNullable(articleMapper.findBySlug(slug));
  }

  @Override
  public void remove(Article article) {
    articleMapper.delete(article.getId());
  }
}
