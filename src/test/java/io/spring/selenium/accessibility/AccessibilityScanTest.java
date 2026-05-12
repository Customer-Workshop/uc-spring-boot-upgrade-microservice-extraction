package io.spring.selenium.accessibility;

import io.spring.selenium.tests.BaseTest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * TestNG-based accessibility scan tests. Scans configured URLs for WCAG 2.1 Level AA compliance
 * using axe-core.
 *
 * <p>Configure target URLs via the config.properties file or override with system properties.
 */
public class AccessibilityScanTest extends BaseTest {

  private AxeCoreScanner axeScanner;
  private List<AccessibilityScanResult> allResults;

  @BeforeClass
  public void setupAccessibilityScanner() {
    axeScanner = new AxeCoreScanner(driver);
    allResults = new ArrayList<>();
  }

  @AfterClass
  public void generateAccessibilityReport() throws IOException {
    if (!allResults.isEmpty()) {
      AccessibilityReportGenerator generator = new AccessibilityReportGenerator();
      String reportDir = "build/reports/accessibility";
      generator.generateHtmlReport(allResults, reportDir + "/accessibility-report.html");
      generator.generateTextReport(allResults, reportDir + "/accessibility-report.txt");
      System.out.println("Accessibility reports generated in: " + reportDir);
    }
  }

  @DataProvider(name = "targetUrls")
  public Object[][] targetUrls() {
    String urlsProp =
        System.getProperty(
            "accessibility.urls",
            config.getProperty(
                "accessibility.urls", "https://www.example.com,https://www.google.com"));
    String[] urls = urlsProp.split(",");
    Object[][] data = new Object[urls.length][1];
    for (int i = 0; i < urls.length; i++) {
      data[i][0] = urls[i].trim();
    }
    return data;
  }

  @Test(
      dataProvider = "targetUrls",
      groups = {"accessibility"})
  public void testAccessibility(String url) {
    test = extent.createTest("Accessibility Scan: " + url);
    test.info("Scanning URL: " + url);

    AccessibilityScanResult result = axeScanner.scan(url);
    allResults.add(result);

    test.info("Page title: " + result.getPageTitle());
    test.info("Violations: " + result.getViolationCount());
    test.info("Passes: " + result.getPassCount());
    test.info("Incomplete: " + result.getIncompleteCount());

    if (result.hasViolations()) {
      StringBuilder violationSummary = new StringBuilder();
      violationSummary.append("Found ").append(result.getViolationCount()).append(" violations:\n");
      violationSummary.append("  Critical: ").append(result.getCriticalCount()).append("\n");
      violationSummary.append("  Serious: ").append(result.getSeriousCount()).append("\n");
      violationSummary.append("  Moderate: ").append(result.getModerateCount()).append("\n");
      violationSummary.append("  Minor: ").append(result.getMinorCount()).append("\n\n");

      for (AccessibilityViolation v : result.getViolations()) {
        violationSummary
            .append("[")
            .append(v.getImpact().toUpperCase())
            .append("] ")
            .append(v.getRuleId())
            .append(": ")
            .append(v.getHelp())
            .append(" (")
            .append(v.getNodeCount())
            .append(" elements)\n");
      }

      test.warning(violationSummary.toString());
    }

    long criticalCount = result.getCriticalCount();
    Assert.assertEquals(
        criticalCount,
        0,
        "Found "
            + criticalCount
            + " critical accessibility violations on "
            + url
            + ". See report for details.");
  }

  @Test(groups = {"accessibility"})
  public void testScanSummary() {
    test = extent.createTest("Accessibility Scan Summary");

    if (allResults.isEmpty()) {
      test.skip("No scan results available");
      return;
    }

    int totalViolations =
        allResults.stream().mapToInt(AccessibilityScanResult::getViolationCount).sum();
    long totalCritical =
        allResults.stream().mapToLong(AccessibilityScanResult::getCriticalCount).sum();
    long totalSerious =
        allResults.stream().mapToLong(AccessibilityScanResult::getSeriousCount).sum();

    test.info("Total pages scanned: " + allResults.size());
    test.info("Total violations: " + totalViolations);
    test.info("Total critical: " + totalCritical);
    test.info("Total serious: " + totalSerious);

    Assert.assertEquals(
        totalCritical,
        0,
        "Found " + totalCritical + " critical violations across all scanned pages.");
  }
}
