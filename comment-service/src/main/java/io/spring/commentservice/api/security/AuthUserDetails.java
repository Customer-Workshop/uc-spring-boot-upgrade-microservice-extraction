package io.spring.commentservice.api.security;

import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class AuthUserDetails extends User {
  private String id;

  public AuthUserDetails(String id, String username) {
    super(username, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
