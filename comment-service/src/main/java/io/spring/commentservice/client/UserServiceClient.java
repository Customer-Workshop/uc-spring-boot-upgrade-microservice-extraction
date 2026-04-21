package io.spring.commentservice.client;

import java.util.Optional;

public interface UserServiceClient {

  Optional<UserResponse> getUserById(String userId);

  boolean isFollowing(String userId, String targetUserId);

  class UserResponse {
    private String id;
    private String username;
    private String bio;
    private String image;

    public UserResponse() {}

    public UserResponse(String id, String username, String bio, String image) {
      this.id = id;
      this.username = username;
      this.bio = bio;
      this.image = image;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getBio() {
      return bio;
    }

    public void setBio(String bio) {
      this.bio = bio;
    }

    public String getImage() {
      return image;
    }

    public void setImage(String image) {
      this.image = image;
    }
  }
}
