package io.spring.articleservice.repository;

import io.spring.articleservice.model.Article;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {

  Optional<Article> findBySlug(String slug);

  @Query(
      "SELECT DISTINCT a FROM Article a LEFT JOIN a.tags t "
          + "WHERE (:tag IS NULL OR t.name = :tag) "
          + "AND (:userId IS NULL OR a.userId = :userId) "
          + "ORDER BY a.createdAt DESC")
  List<Article> findByFilters(
      @Param("tag") String tag, @Param("userId") String userId, Pageable pageable);

  @Query(
      "SELECT COUNT(DISTINCT a) FROM Article a LEFT JOIN a.tags t "
          + "WHERE (:tag IS NULL OR t.name = :tag) "
          + "AND (:userId IS NULL OR a.userId = :userId)")
  int countByFilters(@Param("tag") String tag, @Param("userId") String userId);

  List<Article> findByUserIdInOrderByCreatedAtDesc(List<String> userIds, Pageable pageable);

  int countByUserIdIn(List<String> userIds);

  List<Article> findByIdInOrderByCreatedAtDesc(List<String> articleIds, Pageable pageable);

  int countByIdIn(List<String> articleIds);

  @Query(
      "SELECT DISTINCT a FROM Article a JOIN a.tags t "
          + "WHERE t.name = :tag ORDER BY a.createdAt DESC")
  List<Article> findByTag(@Param("tag") String tag, Pageable pageable);
}
