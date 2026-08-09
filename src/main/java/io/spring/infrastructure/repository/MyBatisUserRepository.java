package io.spring.infrastructure.repository;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.mapper.UserMapper;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** Persists users and follow relationships through the MyBatis mapper. */
@Repository
public class MyBatisUserRepository implements UserRepository {
  private final UserMapper userMapper;

  @Autowired
  public MyBatisUserRepository(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  /** Inserts a new user or updates an existing user. */
  @Override
  public void save(User user) {
    if (userMapper.findById(user.getId()) == null) {
      userMapper.insert(user);
    } else {
      userMapper.update(user);
    }
  }

  /** Finds a user by identifier. */
  @Override
  public Optional<User> findById(String id) {
    return Optional.ofNullable(userMapper.findById(id));
  }

  /** Finds a user by username. */
  @Override
  public Optional<User> findByUsername(String username) {
    return Optional.ofNullable(userMapper.findByUsername(username));
  }

  /** Finds a user by email address. */
  @Override
  public Optional<User> findByEmail(String email) {
    return Optional.ofNullable(userMapper.findByEmail(email));
  }

  /** Saves a follow relationship only when it is not already present. */
  @Override
  public void saveRelation(FollowRelation followRelation) {
    if (!findRelation(followRelation.getUserId(), followRelation.getTargetId()).isPresent()) {
      userMapper.saveRelation(followRelation);
    }
  }

  /** Finds a follow relationship between two users. */
  @Override
  public Optional<FollowRelation> findRelation(String userId, String targetId) {
    return Optional.ofNullable(userMapper.findRelation(userId, targetId));
  }

  /** Deletes a follow relationship. */
  @Override
  public void removeRelation(FollowRelation followRelation) {
    userMapper.deleteRelation(followRelation);
  }
}
