package io.spring.infrastructure.repository;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.infrastructure.mybatis.mapper.ArticleMapper;
import io.spring.infrastructure.service.TagServiceClient;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MyBatisArticleRepository implements ArticleRepository {
  private ArticleMapper articleMapper;
  private TagServiceClient tagServiceClient;

  public MyBatisArticleRepository(ArticleMapper articleMapper, TagServiceClient tagServiceClient) {
    this.articleMapper = articleMapper;
    this.tagServiceClient = tagServiceClient;
  }

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
      String tagId = tagServiceClient.findOrCreateTag(tag.getName());
      tagServiceClient.createArticleTagRelation(article.getId(), tagId);
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
