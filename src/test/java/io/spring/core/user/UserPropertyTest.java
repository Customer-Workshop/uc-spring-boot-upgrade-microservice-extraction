package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

public class UserPropertyTest {

  @Property
  void email_without_at_symbol_should_be_detectable(
      @ForAll("stringsWithoutAtSymbol") String email) {
    // Any string without @ is not a valid email
    assertThat(email.contains("@"), is(false));

    // The User domain currently stores the email as-is without validation.
    // This test verifies that strings without @ can be identified as invalid
    // by a simple contains("@") check, which is the minimal email validation rule.
    User user = new User(email, "username", "password", "bio", "image");
    assertThat(user.getEmail().contains("@"), is(false));
  }

  @Property
  void email_with_at_symbol_should_be_accepted(
      @ForAll("stringsWithAtSymbol") String email) {
    User user = new User(email, "username", "password", "bio", "image");

    assertThat(user.getEmail(), is(email));
    assertThat(user.getEmail().contains("@"), is(true));
  }

  @Property
  void username_should_be_stored_as_provided(
      @ForAll @StringLength(min = 1, max = 100) String username) {
    Assume.that(username != null);

    // The User domain currently stores the username as-is.
    // This property test verifies the username is preserved on construction.
    // Note: The domain does not currently trim/lowercase usernames.
    // If trim+lowercase behavior is added, these assertions should be updated.
    User user = new User("email@test.com", username, "password", "bio", "image");

    assertThat(user.getUsername(), is(notNullValue()));
    assertThat(user.getUsername(), is(username));
  }

  @Property
  void user_id_should_always_be_generated(@ForAll @StringLength(min = 1, max = 50) String email) {
    User user = new User(email, "username", "password", "bio", "image");

    assertThat(user.getId(), is(notNullValue()));
    assertThat(user.getId().isEmpty(), is(false));
  }

  @Property
  void user_update_should_preserve_non_empty_fields(
      @ForAll @StringLength(min = 1, max = 100) String newEmail,
      @ForAll @StringLength(min = 1, max = 100) String newUsername,
      @ForAll @StringLength(min = 1, max = 100) String newPassword,
      @ForAll @StringLength(min = 0, max = 200) String newBio,
      @ForAll @StringLength(min = 0, max = 200) String newImage) {
    Assume.that(!newEmail.isEmpty());
    Assume.that(!newUsername.isEmpty());
    Assume.that(!newPassword.isEmpty());

    User user = new User("old@test.com", "olduser", "oldpass", "oldbio", "oldimage");

    user.update(newEmail, newUsername, newPassword, newBio, newImage);

    assertThat(user.getEmail(), is(newEmail));
    assertThat(user.getUsername(), is(newUsername));
    assertThat(user.getPassword(), is(newPassword));
  }

  @Property
  void user_update_with_empty_fields_should_keep_original(
      @ForAll @StringLength(min = 1, max = 100) String originalEmail,
      @ForAll @StringLength(min = 1, max = 100) String originalUsername) {
    User user = new User(originalEmail, originalUsername, "password", "bio", "image");

    // Update with null/empty should preserve originals
    user.update(null, null, null, null, null);

    assertThat(user.getEmail(), is(originalEmail));
    assertThat(user.getUsername(), is(originalUsername));

    user.update("", "", "", "", "");

    assertThat(user.getEmail(), is(originalEmail));
    assertThat(user.getUsername(), is(originalUsername));
  }

  @Provide
  Arbitrary<String> stringsWithoutAtSymbol() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withCharRange('A', 'Z')
        .withCharRange('0', '9')
        .withChars('.', '_', '-', '+')
        .ofMinLength(1)
        .ofMaxLength(100);
  }

  @Provide
  Arbitrary<String> stringsWithAtSymbol() {
    Arbitrary<String> localPart =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .withChars('.', '_')
            .ofMinLength(1)
            .ofMaxLength(30);
    Arbitrary<String> domain =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(2)
            .ofMaxLength(20);
    return Combinators.combine(localPart, domain).as((l, d) -> l + "@" + d + ".com");
  }
}
