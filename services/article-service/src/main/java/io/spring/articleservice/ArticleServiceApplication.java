package io.spring.articleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"io.spring.articleservice", "io.spring.common"})
public class ArticleServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ArticleServiceApplication.class, args);
  }
}
