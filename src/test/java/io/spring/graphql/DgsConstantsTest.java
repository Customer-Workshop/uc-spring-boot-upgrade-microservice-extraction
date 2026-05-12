package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DgsConstantsTest {

  @Test
  public void should_have_query_constants() {
    assertNotNull(DgsConstants.QUERY_TYPE);
    assertEquals("Query", DgsConstants.QUERY_TYPE);
  }

  @Test
  public void should_have_article_constants() {
    assertNotNull(DgsConstants.ARTICLE.TYPE_NAME);
    assertNotNull(DgsConstants.ARTICLE.Comments);
  }

  @Test
  public void should_have_profile_constants() {
    assertNotNull(DgsConstants.PROFILE.TYPE_NAME);
    assertNotNull(DgsConstants.PROFILE.Feed);
    assertNotNull(DgsConstants.PROFILE.Favorites);
    assertNotNull(DgsConstants.PROFILE.Articles);
  }

  @Test
  public void should_have_query_field_constants() {
    assertNotNull(DgsConstants.QUERY.Feed);
    assertNotNull(DgsConstants.QUERY.Articles);
    assertNotNull(DgsConstants.QUERY.Article);
  }

  @Test
  public void should_have_mutation_constants() {
    assertNotNull(DgsConstants.MUTATION.TYPE_NAME);
    assertNotNull(DgsConstants.MUTATION.CreateUser);
  }

  @Test
  public void should_have_user_constants() {
    assertNotNull(DgsConstants.USER.TYPE_NAME);
  }

  @Test
  public void should_have_comment_constants() {
    assertNotNull(DgsConstants.COMMENT.TYPE_NAME);
    assertNotNull(DgsConstants.COMMENT.Article);
  }

  @Test
  public void should_have_payload_constants() {
    assertNotNull(DgsConstants.ARTICLEPAYLOAD.TYPE_NAME);
    assertNotNull(DgsConstants.COMMENTPAYLOAD.TYPE_NAME);
    assertNotNull(DgsConstants.USERPAYLOAD.TYPE_NAME);
    assertNotNull(DgsConstants.PROFILEPAYLOAD.TYPE_NAME);
    assertNotNull(DgsConstants.DELETIONSTATUS.TYPE_NAME);
    assertNotNull(DgsConstants.COMMENTPAYLOAD.Comment);
    assertNotNull(DgsConstants.ARTICLEPAYLOAD.Article);
  }

  @Test
  public void should_have_connection_constants() {
    assertNotNull(DgsConstants.ARTICLESCONNECTION.TYPE_NAME);
    assertNotNull(DgsConstants.COMMENTSCONNECTION.TYPE_NAME);
    assertNotNull(DgsConstants.ARTICLEEDGE.TYPE_NAME);
    assertNotNull(DgsConstants.COMMENTEDGE.TYPE_NAME);
    assertNotNull(DgsConstants.PAGEINFO.TYPE_NAME);
  }

  @Test
  public void should_have_input_constants() {
    assertNotNull(DgsConstants.CREATEARTICLEINPUT.TYPE_NAME);
    assertNotNull(DgsConstants.UPDATEARTICLEINPUT.TYPE_NAME);
    assertNotNull(DgsConstants.CREATEUSERINPUT.TYPE_NAME);
    assertNotNull(DgsConstants.UPDATEUSERINPUT.TYPE_NAME);
  }

  @Test
  public void should_have_error_constants() {
    assertNotNull(DgsConstants.ERROR.TYPE_NAME);
    assertNotNull(DgsConstants.ERRORITEM.TYPE_NAME);
  }
}
