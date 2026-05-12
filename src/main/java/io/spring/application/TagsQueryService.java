package io.spring.application;

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

  public List<Tag> allTagEntities() {
    return tagRepository.findAll();
  }

  public Optional<Tag> findById(String id) {
    return tagRepository.findById(id);
  }

  public Optional<Tag> findByName(String name) {
    return tagRepository.findByName(name);
  }

  public Tag createTag(String name) {
    Optional<Tag> existing = tagRepository.findByName(name);
    if (existing.isPresent()) {
      return existing.get();
    }
    Tag tag = new Tag(name);
    tagRepository.save(tag);
    return tag;
  }

  public Tag updateTag(String id, String name) {
    Tag tag =
        tagRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));
    tag.setName(name);
    tagRepository.save(tag);
    return tag;
  }

  public void deleteTag(String id) {
    tagRepository.remove(id);
  }

  public List<Tag> findByArticleId(String articleId) {
    return tagRepository.findByArticleId(articleId);
  }
}
