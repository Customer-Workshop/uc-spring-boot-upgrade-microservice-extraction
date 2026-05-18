package io.spring.userservice.service;

import io.spring.userservice.domain.User;
import io.spring.userservice.repository.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserMapper userMapper;
  private final String defaultImage;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserMapper userMapper,
      @Value("${image.default}") String defaultImage,
      PasswordEncoder passwordEncoder) {
    this.userMapper = userMapper;
    this.defaultImage = defaultImage;
    this.passwordEncoder = passwordEncoder;
  }

  public User createUser(String email, String username, String password) {
    User user = new User(email, username, passwordEncoder.encode(password), "", defaultImage);
    userMapper.insert(user);
    return user;
  }

  public void updateUser(User user, String email, String username, String password, String bio, String image) {
    if (password != null && !password.isEmpty()) {
      password = passwordEncoder.encode(password);
    }
    user.update(email, username, password, bio, image);
    userMapper.update(user);
  }
}
