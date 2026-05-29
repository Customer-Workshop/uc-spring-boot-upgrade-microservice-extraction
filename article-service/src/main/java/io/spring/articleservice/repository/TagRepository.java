package io.spring.articleservice.repository;

import io.spring.articleservice.model.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

  Optional<Tag> findByName(String name);
}
