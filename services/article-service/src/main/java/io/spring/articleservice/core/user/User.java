package io.spring.articleservice.core.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class User {
  private String id;
  private String email;
  private String username;
  private String bio;
  private String image;

  public User(String id, String email, String username, String bio, String image) {
    this.id = id;
    this.email = email;
    this.username = username;
    this.bio = bio;
    this.image = image;
  }
}
