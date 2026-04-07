package io.spring.comments;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.comments.controller.CommentController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommentsServiceApplicationTests {

  @Autowired private CommentController commentController;

  @Test
  void contextLoads() {
    assertThat(commentController).isNotNull();
  }
}
