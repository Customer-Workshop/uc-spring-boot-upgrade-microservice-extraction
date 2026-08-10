package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserTest {

  private User newUser() {
    return new User("john@example.com", "john", "123", "bio", "image");
  }

  @Test
  public void should_set_all_fields_in_constructor() {
    User user = newUser();
    assertThat(user.getId(), notNullValue());
    assertThat(user.getEmail(), is("john@example.com"));
    assertThat(user.getUsername(), is("john"));
    assertThat(user.getPassword(), is("123"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image"));
  }

  @Test
  public void should_update_email_only() {
    User user = newUser();
    user.update("new@example.com", "", "", "", "");
    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("john"));
    assertThat(user.getPassword(), is("123"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image"));
  }

  @Test
  public void should_update_username_only() {
    User user = newUser();
    user.update("", "newname", "", "", "");
    assertThat(user.getUsername(), is("newname"));
    assertThat(user.getEmail(), is("john@example.com"));
  }

  @Test
  public void should_update_password_only() {
    User user = newUser();
    user.update("", "", "newpassword", "", "");
    assertThat(user.getPassword(), is("newpassword"));
    assertThat(user.getEmail(), is("john@example.com"));
  }

  @Test
  public void should_update_bio_only() {
    User user = newUser();
    user.update("", "", "", "new bio", "");
    assertThat(user.getBio(), is("new bio"));
    assertThat(user.getImage(), is("image"));
  }

  @Test
  public void should_update_image_only() {
    User user = newUser();
    user.update("", "", "", "", "new image");
    assertThat(user.getImage(), is("new image"));
    assertThat(user.getBio(), is("bio"));
  }

  @Test
  public void should_update_all_fields() {
    User user = newUser();
    user.update("new@example.com", "newname", "newpassword", "new bio", "new image");
    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("newname"));
    assertThat(user.getPassword(), is("newpassword"));
    assertThat(user.getBio(), is("new bio"));
    assertThat(user.getImage(), is("new image"));
  }

  @Test
  public void should_keep_original_values_when_updating_with_null() {
    User user = newUser();
    user.update(null, null, null, null, null);
    assertThat(user.getEmail(), is("john@example.com"));
    assertThat(user.getUsername(), is("john"));
    assertThat(user.getPassword(), is("123"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image"));
  }

  @Test
  public void should_keep_original_values_when_updating_with_empty_string() {
    User user = newUser();
    user.update("", "", "", "", "");
    assertThat(user.getEmail(), is("john@example.com"));
    assertThat(user.getUsername(), is("john"));
    assertThat(user.getPassword(), is("123"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image"));
  }

  @Test
  public void should_not_keep_id_unchanged_between_users() {
    assertThat(newUser().getId().equals(newUser().getId()), is(false));
  }

  @Test
  public void should_be_equal_by_id() {
    User user = newUser();
    assertThat(user.equals(user), is(true));
    assertThat(user.equals(newUser()), is(false));
    assertThat(user.hashCode(), is(user.hashCode()));
  }
}
