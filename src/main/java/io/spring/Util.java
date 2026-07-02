package io.spring;

/** Common utility methods used across the application. */
public class Util {

  /**
   * Checks whether a string is null or empty.
   *
   * @param value the string to check
   * @return {@code true} if the value is {@code null} or has zero length
   */
  public static boolean isEmpty(String value) {
    return value == null || value.isEmpty();
  }
}
