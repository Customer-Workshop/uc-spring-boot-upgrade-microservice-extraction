package io.spring.tags.application;

import io.spring.tags.domain.Tag;
import io.spring.tags.infrastructure.mybatis.mapper.TagMapper;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TagCommandService {
  private TagMapper tagMapper;

  @Transactional
  public Tag findOrCreateTag(String name) {
    return Optional.ofNullable(tagMapper.findByName(name))
        .orElseGet(
            () -> {
              Tag tag = new Tag(name);
              tagMapper.insert(tag);
              return tag;
            });
  }

  @Transactional
  public void createArticleTagRelation(String articleId, String tagId) {
    tagMapper.insertArticleTagRelation(articleId, tagId);
  }
}
