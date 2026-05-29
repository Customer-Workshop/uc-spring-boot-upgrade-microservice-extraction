package io.spring.articleservice.repository;

import io.spring.articleservice.model.Article;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {

  Optional<Article> findBySlug(String slug);

  @Query(
      value =
          "SELECT DISTINCT a.* FROM articles a "
              + "LEFT JOIN article_tags at ON a.id = at.article_id "
              + "LEFT JOIN tags t ON at.tag_id = t.id "
              + "LEFT JOIN article_favorites af ON a.id = af.article_id "
              + "WHERE (:tag IS NULL OR t.name = :tag) "
              + "AND (:userId IS NULL OR a.user_id = :userId) "
              + "AND (:favoritedByUserId IS NULL OR af.user_id = :favoritedByUserId) "
              + "ORDER BY a.created_at DESC "
              + "LIMIT :limit OFFSET :offset",
      nativeQuery = true)
  List<Article> findByAllFilters(
      @Param("tag") String tag,
      @Param("userId") String userId,
      @Param("favoritedByUserId") String favoritedByUserId,
      @Param("offset") int offset,
      @Param("limit") int limit);

  @Query(
      value =
          "SELECT COUNT(DISTINCT a.id) FROM articles a "
              + "LEFT JOIN article_tags at ON a.id = at.article_id "
              + "LEFT JOIN tags t ON at.tag_id = t.id "
              + "LEFT JOIN article_favorites af ON a.id = af.article_id "
              + "WHERE (:tag IS NULL OR t.name = :tag) "
              + "AND (:userId IS NULL OR a.user_id = :userId) "
              + "AND (:favoritedByUserId IS NULL OR af.user_id = :favoritedByUserId)",
      nativeQuery = true)
  int countByAllFilters(
      @Param("tag") String tag,
      @Param("userId") String userId,
      @Param("favoritedByUserId") String favoritedByUserId);

  @Query(
      value =
          "SELECT a.* FROM articles a "
              + "WHERE a.user_id IN (:userIds) "
              + "ORDER BY a.created_at DESC "
              + "LIMIT :limit OFFSET :offset",
      nativeQuery = true)
  List<Article> findByUserIdsFeed(
      @Param("userIds") List<String> userIds,
      @Param("offset") int offset,
      @Param("limit") int limit);

  int countByUserIdIn(List<String> userIds);
}
