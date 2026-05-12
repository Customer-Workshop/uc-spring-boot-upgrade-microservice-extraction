package io.spring.selenium.accessibility;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Multi-site accessibility scanner that can scan any list of URLs for WCAG compliance. Produces
 * comprehensive HTML and text reports with violation details and remediation guidance.
 *
 * <p>Usage:
 *
 * <pre>
 * WebsiteAccessibilityScanner scanner = new WebsiteAccessibilityScanner();
 * scanner.addUrl("https://example.com");
 * scanner.addUrl("https://google.com");
 * scanner.scanAll();
 * scanner.generateReports("build/reports/accessibility");
 * scanner.close();
 * </pre>
 */
public class WebsiteAccessibilityScanner {

  private WebDriver driver;
  private AxeCoreScanner axeScanner;
  private final List<String> urls;
  private final List<AccessibilityScanResult> results;
  private boolean headless;

  public WebsiteAccessibilityScanner() {
    this.urls = new ArrayList<>();
    this.results = new ArrayList<>();
    this.headless = true;
  }

  public WebsiteAccessibilityScanner(boolean headless) {
    this.urls = new ArrayList<>();
    this.results = new ArrayList<>();
    this.headless = headless;
  }

  /** Add a URL to the scan queue. */
  public void addUrl(String url) {
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      url = "https://" + url;
    }
    urls.add(url);
  }

  /** Add multiple URLs to the scan queue. */
  public void addUrls(List<String> urlList) {
    for (String url : urlList) {
      addUrl(url);
    }
  }

  /** Initialize the WebDriver and scanner. */
  public void initialize() {
    WebDriverManager.chromedriver().setup();
    ChromeOptions options = new ChromeOptions();
    if (headless) {
      options.addArguments("--headless");
    }
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--window-size=1920,1080");
    driver = new ChromeDriver(options);
    axeScanner = new AxeCoreScanner(driver);
  }

  /** Initialize with an existing WebDriver instance. */
  public void initialize(WebDriver existingDriver) {
    this.driver = existingDriver;
    this.axeScanner = new AxeCoreScanner(driver);
  }

  /** Scan all queued URLs and collect results. */
  public List<AccessibilityScanResult> scanAll() {
    if (driver == null) {
      initialize();
    }
    results.clear();
    for (String url : urls) {
      System.out.println("Scanning: " + url);
      try {
        AccessibilityScanResult result = axeScanner.scan(url);
        results.add(result);
        System.out.println(
            "  Found "
                + result.getViolationCount()
                + " violations, "
                + result.getPassCount()
                + " passes");
      } catch (Exception e) {
        System.err.println("  Error scanning " + url + ": " + e.getMessage());
      }
    }
    return results;
  }

  /** Generate HTML and text reports from scan results. */
  public void generateReports(String outputDir) throws IOException {
    AccessibilityReportGenerator generator = new AccessibilityReportGenerator();
    generator.generateHtmlReport(results, outputDir + "/accessibility-report.html");
    generator.generateTextReport(results, outputDir + "/accessibility-report.txt");
    System.out.println("Reports generated in: " + outputDir);
  }

  /** Get all scan results. */
  public List<AccessibilityScanResult> getResults() {
    return results;
  }

  /** Close the WebDriver. */
  public void close() {
    if (driver != null) {
      driver.quit();
      driver = null;
    }
  }

  /**
   * Main entry point for command-line usage.
   *
   * @param args list of URLs to scan
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Accessibility Scanner - Scan any website for WCAG compliance");
      System.out.println();
      System.out.println("Usage: java WebsiteAccessibilityScanner <url1> [url2] [url3] ...");
      System.out.println();
      System.out.println("Examples:");
      System.out.println("  java WebsiteAccessibilityScanner https://example.com");
      System.out.println(
          "  java WebsiteAccessibilityScanner https://google.com https://github.com");
      System.out.println();
      System.out.println("Reports are generated in: build/reports/accessibility/");
      System.exit(1);
    }

    WebsiteAccessibilityScanner scanner = new WebsiteAccessibilityScanner();
    try {
      for (String url : args) {
        scanner.addUrl(url);
      }
      scanner.scanAll();
      scanner.generateReports("build/reports/accessibility");

      int totalViolations =
          scanner.getResults().stream().mapToInt(AccessibilityScanResult::getViolationCount).sum();
      System.out.println();
      System.out.println("Scan complete. Total violations found: " + totalViolations);
      System.exit(totalViolations > 0 ? 1 : 0);
    } catch (Exception e) {
      System.err.println("Scanner error: " + e.getMessage());
      e.printStackTrace();
      System.exit(2);
    } finally {
      scanner.close();
    }
  }
}
