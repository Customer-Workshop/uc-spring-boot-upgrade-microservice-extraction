package io.spring.articleservice.dto;

import java.util.List;

public record ArticleListDto(List<ArticleDto> articles, int articlesCount) {}
