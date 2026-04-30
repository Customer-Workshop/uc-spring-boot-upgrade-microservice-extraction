package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultPageInfo;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.DgsConstants.ARTICLE;
import io.spring.graphql.DgsConstants.COMMENTPAYLOAD;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentEdge;
import io.spring.graphql.types.CommentsConnection;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
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
  private UserRepository userRepository;

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

    Article article = dfe.getSource();
    Map<String, ArticleData> map = dfe.getLocalContext();
    ArticleData articleData = map.get(article.getSlug());

    List<CommentResponse> remoteComments =
        commentServiceClient.getCommentsByArticleId(articleData.getId());
    List<CommentData> allComments =
        remoteComments.stream().map(this::toCommentData).collect(Collectors.toList());

    CursorPager<CommentData> comments = paginateInMemory(allComments, first, after, last, before);

    graphql.relay.PageInfo pageInfo = buildCommentPageInfo(comments);
    CommentsConnection result =
        CommentsConnection.newBuilder()
            .pageInfo(pageInfo)
            .edges(
                comments.getData().stream()
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
        .localContext(
            comments.getData().stream().collect(Collectors.toMap(CommentData::getId, c -> c)))
        .build();
  }

  private CursorPager<CommentData> paginateInMemory(
      List<CommentData> allComments, Integer first, String after, Integer last, String before) {
    if (allComments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    }

    List<CommentData> filtered = new ArrayList<>(allComments);

    if (first != null) {
      if (after != null) {
        DateTime cursor = DateTimeCursor.parse(after);
        if (cursor != null) {
          filtered =
              filtered.stream()
                  .filter(c -> c.getCreatedAt().isBefore(cursor))
                  .collect(Collectors.toList());
        }
      }
      boolean hasMore = filtered.size() > first;
      if (hasMore) {
        filtered = filtered.subList(0, first);
      }
      return new CursorPager<>(filtered, Direction.NEXT, hasMore);
    } else {
      if (before != null) {
        DateTime cursor = DateTimeCursor.parse(before);
        if (cursor != null) {
          filtered =
              filtered.stream()
                  .filter(c -> c.getCreatedAt().isAfter(cursor))
                  .collect(Collectors.toList());
        }
      }
      boolean hasMore = filtered.size() > last;
      if (hasMore) {
        filtered = filtered.subList(filtered.size() - last, filtered.size());
      }
      Collections.reverse(filtered);
      return new CursorPager<>(filtered, Direction.PREV, hasMore);
    }
  }

  private CommentData toCommentData(CommentResponse response) {
    User author = userRepository.findById(response.getUserId()).orElse(null);
    ProfileData profileData;
    if (author != null) {
      profileData =
          new ProfileData(
              author.getId(), author.getUsername(), author.getBio(), author.getImage(), false);
    } else {
      profileData = new ProfileData(response.getUserId(), "", "", "", false);
    }
    return new CommentData(
        response.getId(),
        response.getBody(),
        response.getArticleId(),
        new DateTime(),
        new DateTime(),
        profileData);
  }

  private DefaultPageInfo buildCommentPageInfo(CursorPager<CommentData> comments) {
    return new DefaultPageInfo(
        comments.getStartCursor() == null
            ? null
            : new DefaultConnectionCursor(comments.getStartCursor().toString()),
        comments.getEndCursor() == null
            ? null
            : new DefaultConnectionCursor(comments.getEndCursor().toString()),
        comments.hasPrevious(),
        comments.hasNext());
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
