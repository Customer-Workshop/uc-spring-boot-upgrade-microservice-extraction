package io.spring.articleservice.controller;

import io.spring.articleservice.service.TagService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagsController {

  private final TagService tagService;

  @GetMapping
  public ResponseEntity<Map<String, List<String>>> getTags() {
    return ResponseEntity.ok(Map.of("tags", tagService.getAllTags()));
  }
}
