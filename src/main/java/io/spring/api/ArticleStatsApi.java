package io.spring.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/stats")
public class ArticleStatsApi {
  // TDD: endpoint not yet implemented - tests should fail
}
