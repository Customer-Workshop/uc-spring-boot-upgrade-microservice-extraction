package io.spring.articleservice.repository;

import io.spring.articleservice.model.ArticleFavorite;
import io.spring.articleservice.model.ArticleFavoriteId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleFavoriteRepository
    extends JpaRepository<ArticleFavorite, ArticleFavoriteId> {

  Optional<ArticleFavorite> findByArticleIdAndUserId(String articleId, String userId);

  int countByArticleId(String articleId);

  boolean existsByArticleIdAndUserId(String articleId, String userId);

  @Query("SELECT af.articleId FROM ArticleFavorite af WHERE af.userId = :userId")
  List<String> findArticleIdsByUserId(@Param("userId") String userId);
}
