package io.spring.articleservice.api;

import io.spring.articleservice.domain.Article;
import io.spring.articleservice.domain.ArticleRepository;
import io.spring.articleservice.domain.Tag;
import io.spring.articleservice.domain.TagRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/articles")
@AllArgsConstructor
public class ArticleController {

  private final ArticleRepository articleRepository;
  private final TagRepository tagRepository;

  @PostMapping
  public ResponseEntity<ArticleDTO> createArticle(
      @Valid @RequestBody CreateArticleRequest request) {
    Article article =
        new Article(
            request.getTitle(),
            request.getDescription(),
            request.getBody(),
            request.getUserId());
    if (request.getTagList() != null) {
      List<Tag> resolvedTags =
          request.getTagList().stream()
              .distinct()
              .map(name -> tagRepository.findByName(name).orElseGet(() -> new Tag(name)))
              .collect(Collectors.toList());
      article.setTags(resolvedTags);
    }
    articleRepository.save(article);
    return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(article));
  }

  @GetMapping("/{slug}")
  public ResponseEntity<ArticleDTO> getArticle(@PathVariable String slug) {
    Article article =
        articleRepository
            .findBySlug(slug)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
    return ResponseEntity.ok(toDTO(article));
  }

  @GetMapping
  public ResponseEntity<List<ArticleDTO>> listArticles() {
    List<ArticleDTO> articles =
        articleRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    return ResponseEntity.ok(articles);
  }

  @PutMapping("/{slug}")
  public ResponseEntity<ArticleDTO> updateArticle(
      @PathVariable String slug, @Valid @RequestBody UpdateArticleRequest request) {
    Article article =
        articleRepository
            .findBySlug(slug)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
    article.update(request.getTitle(), request.getDescription(), request.getBody());
    articleRepository.save(article);
    return ResponseEntity.ok(toDTO(article));
  }

  @DeleteMapping("/{slug}")
  public ResponseEntity<Void> deleteArticle(@PathVariable String slug) {
    Article article =
        articleRepository
            .findBySlug(slug)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
    articleRepository.delete(article);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/tags")
  public ResponseEntity<List<String>> getTags() {
    return ResponseEntity.ok(tagRepository.findAllTagNames());
  }

  private ArticleDTO toDTO(Article article) {
    return ArticleDTO.builder()
        .id(article.getId())
        .slug(article.getSlug())
        .title(article.getTitle())
        .description(article.getDescription())
        .body(article.getBody())
        .userId(article.getUserId())
        .tagList(
            article.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toList()))
        .createdAt(article.getCreatedAt())
        .updatedAt(article.getUpdatedAt())
        .build();
  }
}
