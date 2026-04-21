package io.spring.profileservice.infrastructure.mybatis.mapper;

import io.spring.profileservice.core.FollowRelation;
import io.spring.profileservice.core.FollowRelationRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class MyBatisFollowRelationRepository implements FollowRelationRepository {

  private FollowRelationMapper followRelationMapper;

  @Override
  public void saveRelation(FollowRelation followRelation) {
    followRelationMapper.saveRelation(followRelation);
  }

  @Override
  public Optional<FollowRelation> findRelation(String userId, String targetId) {
    return Optional.ofNullable(followRelationMapper.findRelation(userId, targetId));
  }

  @Override
  public void removeRelation(FollowRelation followRelation) {
    followRelationMapper.deleteRelation(followRelation);
  }
}
