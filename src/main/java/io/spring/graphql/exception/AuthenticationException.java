package io.spring.graphql.exception;

/** Thrown when a GraphQL operation requires authentication but no valid user is present. */
public class AuthenticationException extends RuntimeException {}
