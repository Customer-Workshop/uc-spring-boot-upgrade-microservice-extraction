package io.spring.application;

/** Marker interface for entities that support cursor-based pagination in GraphQL connections. */
public interface Node {

  /**
   * Returns the cursor for this node's position in a paginated result.
   *
   * @return the page cursor
   */
  PageCursor getCursor();
}
