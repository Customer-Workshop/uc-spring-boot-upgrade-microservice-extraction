package io.spring.favoriteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"io.spring.favoriteservice", "io.spring.common"})
public class FavoriteServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(FavoriteServiceApplication.class, args);
  }
}
