package io.spring.articleservice.service;

import io.spring.articleservice.domain.Article;
import io.spring.articleservice.domain.Tag;
import io.spring.articleservice.domain.User;
import io.spring.articleservice.repository.ArticleMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ArticleCommandService {

  private ArticleMapper articleMapper;

  @Transactional
  public Article createArticle(String title, String description, String body,
      java.util.List<String> tagList, User creator) {
    Article article = new Article(title, description, body,
        tagList != null ? tagList : java.util.Collections.emptyList(), creator.getId());
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
    return article;
  }

  public Article updateArticle(Article article, String title, String description, String body) {
    article.update(title, description, body);
    articleMapper.update(article);
    return article;
  }
}
