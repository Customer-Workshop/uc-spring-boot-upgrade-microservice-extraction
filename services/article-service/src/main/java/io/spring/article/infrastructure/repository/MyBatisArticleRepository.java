package io.spring.article.infrastructure.repository;

import io.spring.article.core.Article;
import io.spring.article.core.ArticleRepository;
import io.spring.article.core.Tag;
import io.spring.article.infrastructure.mybatis.mapper.ArticleMapper;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class MyBatisArticleRepository implements ArticleRepository {
  private ArticleMapper articleMapper;

  @Override
  public void save(Article article) {
    if (articleMapper.findById(article.getId()) == null) {
      createNew(article);
    } else {
      articleMapper.update(article);
    }
  }

  private void createNew(Article article) {
    for (Tag tag : article.getTags()) {
      Tag tagInDB = articleMapper.findTag(tag.getName());
      if (tagInDB == null) {
        articleMapper.insertTag(tag);
      } else {
        tag.setId(tagInDB.getId());
      }
      articleMapper.insertArticleTagRelation(article.getId(), tag.getId());
    }
    articleMapper.insert(article);
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
