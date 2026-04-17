package io.spring.articleservice.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<Tag, String> {

  @Query("SELECT DISTINCT t.name FROM Tag t")
  List<String> findAllTagNames();
}
