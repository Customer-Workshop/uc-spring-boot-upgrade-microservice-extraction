package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import graphql.relay.DefaultPageInfo;
import io.spring.application.data.ArticleData;
import io.spring.graphql.DgsConstants.ARTICLE;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentEdge;
import io.spring.graphql.types.CommentsConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@DgsComponent
public class CommentDatafetcher {

  private final RestTemplate restTemplate;
  private final String commentServiceBaseUrl;

  public CommentDatafetcher(
      RestTemplate restTemplate,
      @Value("${comment-service.base-url:http://localhost:8081}") String commentServiceBaseUrl) {
    this.restTemplate = restTemplate;
    this.commentServiceBaseUrl = commentServiceBaseUrl;
  }

  @DgsData(parentType = ARTICLE.TYPE_NAME, field = ARTICLE.Comments)
  @SuppressWarnings("unchecked")
  public DataFetcherResult<CommentsConnection> articleComments(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      DgsDataFetchingEnvironment dfe) {

    Article article = dfe.getSource();
    Map<String, ArticleData> map = dfe.getLocalContext();
    ArticleData articleData = map.get(article.getSlug());

    List<Comment> commentResults = new ArrayList<>();
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              commentServiceBaseUrl + "/articles/{slug}/comments",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              articleData.getSlug());
      if (response.getBody() != null && response.getBody().containsKey("comments")) {
        List<Map<String, Object>> comments =
            (List<Map<String, Object>>) response.getBody().get("comments");
        commentResults =
            comments.stream()
                .map(
                    c ->
                        Comment.newBuilder()
                            .id((String) c.get("id"))
                            .body((String) c.get("body"))
                            .createdAt((String) c.get("createdAt"))
                            .updatedAt((String) c.get("updatedAt"))
                            .build())
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      // fallback to empty comments on service unavailability
    }

    CommentsConnection result =
        CommentsConnection.newBuilder()
            .pageInfo(new DefaultPageInfo(null, null, false, false))
            .edges(
                commentResults.stream()
                    .map(
                        c ->
                            CommentEdge.newBuilder()
                                .cursor(c.getId())
                                .node(c)
                                .build())
                    .collect(Collectors.toList()))
            .build();
    return DataFetcherResult.<CommentsConnection>newResult().data(result).build();
  }
}
