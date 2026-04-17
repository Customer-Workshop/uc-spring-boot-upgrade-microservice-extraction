package io.spring.articleservice.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<Tag, String> {

  Optional<Tag> findByName(String name);

  @Query("SELECT DISTINCT t.name FROM Tag t")
  List<String> findAllTagNames();
}
