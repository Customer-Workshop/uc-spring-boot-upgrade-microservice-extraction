package io.spring.articleservice.service;

import io.spring.articleservice.model.Tag;
import io.spring.articleservice.repository.TagRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {

  private final TagRepository tagRepository;

  public List<String> getAllTags() {
    return tagRepository.findAll().stream().map(Tag::getName).sorted().collect(Collectors.toList());
  }
}
