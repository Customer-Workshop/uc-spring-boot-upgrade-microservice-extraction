package io.spring.article.application.data;

import lombok.Value;

@Value
public class ArticleFavoriteCount {
  String id;
  Integer count;
}
