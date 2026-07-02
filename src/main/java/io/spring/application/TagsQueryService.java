package io.spring.application;

import io.spring.application.data.TagData;
import io.spring.core.article.Tag;
import io.spring.core.article.TagRepository;
import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TagsQueryService {
  private TagReadService tagReadService;
  private TagRepository tagRepository;

  public List<String> allTags() {
    return tagReadService.all();
  }

  public List<TagData> findAll() {
    return tagReadService.findAll();
  }

  public Optional<TagData> findById(String id) {
    return Optional.ofNullable(tagReadService.findById(id));
  }

  public List<TagData> findByArticleId(String articleId) {
    return tagReadService.findByArticleId(articleId);
  }

  public Tag createTag(String name) {
    Optional<Tag> existing = tagRepository.findByName(name);
    if (existing.isPresent()) {
      throw new IllegalArgumentException("Tag with name '" + name + "' already exists");
    }
    Tag tag = new Tag(name);
    tagRepository.save(tag);
    return tag;
  }

  public Tag updateTag(String id, String name) {
    Optional<Tag> tagOpt = tagRepository.findById(id);
    if (!tagOpt.isPresent()) {
      throw new IllegalArgumentException("Tag not found with id: " + id);
    }
    Optional<Tag> existingWithName = tagRepository.findByName(name);
    if (existingWithName.isPresent() && !existingWithName.get().getId().equals(id)) {
      throw new IllegalArgumentException("Tag with name '" + name + "' already exists");
    }
    Tag tag = tagOpt.get();
    tag.update(name);
    tagRepository.save(tag);
    return tag;
  }

  public void removeTag(String id) {
    Optional<Tag> tagOpt = tagRepository.findById(id);
    if (!tagOpt.isPresent()) {
      throw new IllegalArgumentException("Tag not found with id: " + id);
    }
    tagRepository.remove(id);
  }
}
