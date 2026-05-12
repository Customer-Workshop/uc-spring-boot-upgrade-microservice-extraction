package io.spring.selenium.accessibility;

import java.time.LocalDateTime;
import java.util.List;

/** Contains the results of an accessibility scan for a single page. */
public class AccessibilityScanResult {

  private final String url;
  private final String pageTitle;
  private final List<AccessibilityViolation> violations;
  private final int passCount;
  private final int incompleteCount;
  private final int violationCount;
  private final LocalDateTime scanTimestamp;

  public AccessibilityScanResult(
      String url,
      String pageTitle,
      List<AccessibilityViolation> violations,
      int passCount,
      int incompleteCount,
      int violationCount) {
    this.url = url;
    this.pageTitle = pageTitle;
    this.violations = violations;
    this.passCount = passCount;
    this.incompleteCount = incompleteCount;
    this.violationCount = violationCount;
    this.scanTimestamp = LocalDateTime.now();
  }

  public String getUrl() {
    return url;
  }

  public String getPageTitle() {
    return pageTitle;
  }

  public List<AccessibilityViolation> getViolations() {
    return violations;
  }

  public int getPassCount() {
    return passCount;
  }

  public int getIncompleteCount() {
    return incompleteCount;
  }

  public int getViolationCount() {
    return violationCount;
  }

  public LocalDateTime getScanTimestamp() {
    return scanTimestamp;
  }

  public boolean hasViolations() {
    return !violations.isEmpty();
  }

  public long getCriticalCount() {
    return violations.stream().filter(v -> "critical".equals(v.getImpact())).count();
  }

  public long getSeriousCount() {
    return violations.stream().filter(v -> "serious".equals(v.getImpact())).count();
  }

  public long getModerateCount() {
    return violations.stream().filter(v -> "moderate".equals(v.getImpact())).count();
  }

  public long getMinorCount() {
    return violations.stream().filter(v -> "minor".equals(v.getImpact())).count();
  }

  @Override
  public String toString() {
    return String.format(
        "AccessibilityScanResult{url='%s', violations=%d, passes=%d, incomplete=%d}",
        url, violationCount, passCount, incompleteCount);
  }
}
