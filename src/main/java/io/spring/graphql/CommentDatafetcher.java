package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultPageInfo;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.ARTICLE;
import io.spring.graphql.DgsConstants.COMMENTPAYLOAD;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentEdge;
import io.spring.graphql.types.CommentsConnection;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.service.comments.CommentServiceClient;
import io.spring.infrastructure.service.comments.CommentServiceResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

@DgsComponent
@AllArgsConstructor
public class CommentDatafetcher {
  private CommentServiceClient commentServiceClient;
  private UserReadService userReadService;

  @DgsData(parentType = COMMENTPAYLOAD.TYPE_NAME, field = COMMENTPAYLOAD.Comment)
  public DataFetcherResult<Comment> getComment(DgsDataFetchingEnvironment dfe) {
    CommentData comment = dfe.getLocalContext();
    Comment commentResult = buildCommentResult(comment);
    return DataFetcherResult.<Comment>newResult()
        .data(commentResult)
        .localContext(
            new HashMap<String, Object>() {
              {
                put(comment.getId(), comment);
              }
            })
        .build();
  }

  @DgsData(parentType = ARTICLE.TYPE_NAME, field = ARTICLE.Comments)
  public DataFetcherResult<CommentsConnection> articleComments(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      DgsDataFetchingEnvironment dfe) {

    if (first == null && last == null) {
      throw new IllegalArgumentException("first 和 last 必须只存在一个");
    }

    User current = SecurityUtil.getCurrentUser().orElse(null);
    Article article = dfe.getSource();
    Map<String, ArticleData> map = dfe.getLocalContext();
    ArticleData articleData = map.get(article.getSlug());

    List<CommentServiceResponse> responses =
        commentServiceClient.getCommentsByArticleId(articleData.getId());
    List<CommentData> commentDataList =
        responses.stream()
            .map(r -> toCommentData(r, current))
            .collect(Collectors.toList());

    int limit = first != null ? first : last;
    List<CommentData> paged =
        commentDataList.size() > limit
            ? commentDataList.subList(0, limit)
            : commentDataList;

    CommentsConnection result =
        CommentsConnection.newBuilder()
            .pageInfo(
                new DefaultPageInfo(
                    paged.isEmpty()
                        ? null
                        : new DefaultConnectionCursor(paged.get(0).getCursor().toString()),
                    paged.isEmpty()
                        ? null
                        : new DefaultConnectionCursor(
                            paged.get(paged.size() - 1).getCursor().toString()),
                    false,
                    commentDataList.size() > limit))
            .edges(
                paged.stream()
                    .map(
                        a ->
                            CommentEdge.newBuilder()
                                .cursor(a.getCursor().toString())
                                .node(buildCommentResult(a))
                                .build())
                    .collect(Collectors.toList()))
            .build();
    return DataFetcherResult.<CommentsConnection>newResult()
        .data(result)
        .localContext(paged.stream().collect(Collectors.toMap(CommentData::getId, c -> c)))
        .build();
  }

  private CommentData toCommentData(CommentServiceResponse response, User user) {
    UserData userData = userReadService.findById(response.getUserId());
    ProfileData profileData;
    if (userData != null) {
      profileData =
          new ProfileData(
              userData.getId(),
              userData.getUsername(),
              userData.getBio(),
              userData.getImage(),
              false);
    } else {
      profileData = new ProfileData(response.getUserId(), "", "", "", false);
    }
    DateTime createdAt =
        response.getCreatedAt() != null ? DateTime.parse(response.getCreatedAt()) : new DateTime();
    return new CommentData(
        response.getId(), response.getBody(), response.getArticleId(), createdAt, createdAt,
        profileData);
  }

  private Comment buildCommentResult(CommentData comment) {
    return Comment.newBuilder()
        .id(comment.getId())
        .body(comment.getBody())
        .updatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt()))
        .createdAt(ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt()))
        .build();
  }
}
