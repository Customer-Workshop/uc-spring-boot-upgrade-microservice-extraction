package io.spring.core.user;

import io.spring.Util;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing a registered user.
 *
 * <p>Users are identified by a UUID and have a unique email and username. The password is stored in
 * encoded form.
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class User {
  private String id;
  private String email;
  private String username;
  private String password;
  private String bio;
  private String image;

  public User(String email, String username, String password, String bio, String image) {
    this.id = UUID.randomUUID().toString();
    this.email = email;
    this.username = username;
    this.password = password;
    this.bio = bio;
    this.image = image;
  }

  /**
   * Updates the user's mutable profile fields. Only non-empty values are applied.
   *
   * @param email the new email, or empty/null to keep the current value
   * @param username the new username, or empty/null to keep the current value
   * @param password the new password, or empty/null to keep the current value
   * @param bio the new bio, or empty/null to keep the current value
   * @param image the new image URL, or empty/null to keep the current value
   */
  public void update(String email, String username, String password, String bio, String image) {
    if (!Util.isEmpty(email)) {
      this.email = email;
    }

    if (!Util.isEmpty(username)) {
      this.username = username;
    }

    if (!Util.isEmpty(password)) {
      this.password = password;
    }

    if (!Util.isEmpty(bio)) {
      this.bio = bio;
    }

    if (!Util.isEmpty(image)) {
      this.image = image;
    }
  }
}
