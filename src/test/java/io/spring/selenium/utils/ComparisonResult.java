package io.spring.selenium.utils;

/** Result of a visual regression comparison between a baseline and actual screenshot. */
public class ComparisonResult {

  private final boolean passed;
  private final double diffPercentage;
  private final String baselinePath;
  private final String actualPath;
  private final String diffPath;
  private final int diffPixelCount;

  public ComparisonResult(
      boolean passed,
      double diffPercentage,
      String baselinePath,
      String actualPath,
      String diffPath,
      int diffPixelCount) {
    this.passed = passed;
    this.diffPercentage = diffPercentage;
    this.baselinePath = baselinePath;
    this.actualPath = actualPath;
    this.diffPath = diffPath;
    this.diffPixelCount = diffPixelCount;
  }

  public boolean isPassed() {
    return passed;
  }

  public double getDiffPercentage() {
    return diffPercentage;
  }

  public String getBaselinePath() {
    return baselinePath;
  }

  public String getActualPath() {
    return actualPath;
  }

  public String getDiffPath() {
    return diffPath;
  }

  public int getDiffPixelCount() {
    return diffPixelCount;
  }

  @Override
  public String toString() {
    return "ComparisonResult{"
        + "passed="
        + passed
        + ", diffPercentage="
        + String.format("%.2f", diffPercentage)
        + "%, diffPixelCount="
        + diffPixelCount
        + ", baselinePath='"
        + baselinePath
        + "', actualPath='"
        + actualPath
        + "', diffPath='"
        + diffPath
        + "'}";
  }
}
