package io.spring.articleservice.application.data;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleDataList {
  private final List<ArticleData> articleDatas;
  private final int count;
}
