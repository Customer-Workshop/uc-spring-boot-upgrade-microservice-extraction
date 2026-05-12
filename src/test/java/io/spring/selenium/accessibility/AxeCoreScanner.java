package io.spring.selenium.accessibility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Accessibility scanner powered by axe-core. Injects the axe-core JavaScript library into web pages
 * via Selenium WebDriver and runs automated WCAG 2.1 compliance checks.
 */
public class AxeCoreScanner {

  private static final String AXE_CORE_CDN =
      "https://cdnjs.cloudflare.com/ajax/libs/axe-core/4.8.4/axe.min.js";
  private static String axeCoreScript = null;

  private final WebDriver driver;
  private final List<String> tags;

  public AxeCoreScanner(WebDriver driver) {
    this(driver, List.of("wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "best-practice"));
  }

  public AxeCoreScanner(WebDriver driver, List<String> tags) {
    this.driver = driver;
    this.tags = tags;
  }

  /**
   * Scans the given URL for accessibility violations.
   *
   * @param url the URL to scan
   * @return AccessibilityScanResult containing violations and metadata
   */
  public AccessibilityScanResult scan(String url) {
    driver.get(url);
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return runAxeAnalysis(url);
  }

  /** Scans the currently loaded page for accessibility violations. */
  public AccessibilityScanResult scanCurrentPage() {
    String url = driver.getCurrentUrl();
    return runAxeAnalysis(url);
  }

  @SuppressWarnings("unchecked")
  private AccessibilityScanResult runAxeAnalysis(String url) {
    JavascriptExecutor js = (JavascriptExecutor) driver;

    injectAxeCore(js);

    String tagsJson = tags.stream().map(t -> "'" + t + "'").collect(Collectors.joining(","));

    String script =
        "var callback = arguments[arguments.length - 1];"
            + "axe.run(document, {"
            + "  runOnly: { type: 'tag', values: ["
            + tagsJson
            + "] }"
            + "}).then(function(results) {"
            + "  callback(results);"
            + "}).catch(function(err) {"
            + "  callback({error: err.message});"
            + "});";

    Object rawResult = js.executeAsyncScript(script);

    if (rawResult == null) {
      return new AccessibilityScanResult(url, driver.getTitle(), Collections.emptyList(), 0, 0, 0);
    }

    Map<String, Object> result = (Map<String, Object>) rawResult;

    if (result.containsKey("error")) {
      System.err.println("axe-core error: " + result.get("error"));
      return new AccessibilityScanResult(url, driver.getTitle(), Collections.emptyList(), 0, 0, 0);
    }

    List<Map<String, Object>> violations =
        (List<Map<String, Object>>) result.getOrDefault("violations", Collections.emptyList());
    List<Map<String, Object>> passes =
        (List<Map<String, Object>>) result.getOrDefault("passes", Collections.emptyList());
    List<Map<String, Object>> incomplete =
        (List<Map<String, Object>>) result.getOrDefault("incomplete", Collections.emptyList());

    List<AccessibilityViolation> parsedViolations = parseViolations(violations);

    return new AccessibilityScanResult(
        url,
        driver.getTitle(),
        parsedViolations,
        passes.size(),
        incomplete.size(),
        violations.size());
  }

  @SuppressWarnings("unchecked")
  private List<AccessibilityViolation> parseViolations(List<Map<String, Object>> violations) {
    List<AccessibilityViolation> result = new ArrayList<>();

    for (Map<String, Object> violation : violations) {
      String id = (String) violation.getOrDefault("id", "unknown");
      String description = (String) violation.getOrDefault("description", "");
      String help = (String) violation.getOrDefault("help", "");
      String helpUrl = (String) violation.getOrDefault("helpUrl", "");
      String impact = (String) violation.getOrDefault("impact", "unknown");

      List<String> wcagTags = new ArrayList<>();
      List<String> rawTags = (List<String>) violation.getOrDefault("tags", Collections.emptyList());
      for (String tag : rawTags) {
        if (tag.startsWith("wcag") || tag.equals("best-practice")) {
          wcagTags.add(tag);
        }
      }

      List<Map<String, Object>> nodes =
          (List<Map<String, Object>>) violation.getOrDefault("nodes", Collections.emptyList());

      List<AccessibilityViolation.AffectedNode> affectedNodes = new ArrayList<>();
      for (Map<String, Object> node : nodes) {
        String html = (String) node.getOrDefault("html", "");
        List<String> target = (List<String>) node.getOrDefault("target", Collections.emptyList());
        String selector = target.isEmpty() ? "" : target.get(0);
        String failureSummary = (String) node.getOrDefault("failureSummary", "");
        affectedNodes.add(new AccessibilityViolation.AffectedNode(html, selector, failureSummary));
      }

      result.add(
          new AccessibilityViolation(
              id, description, help, helpUrl, impact, wcagTags, affectedNodes));
    }

    return result;
  }

  private void injectAxeCore(JavascriptExecutor js) {
    if (axeCoreScript == null) {
      axeCoreScript = loadAxeCoreScript();
    }
    js.executeScript(axeCoreScript);
  }

  private String loadAxeCoreScript() {
    try {
      URL url = new URL(AXE_CORE_CDN);
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
        return reader.lines().collect(Collectors.joining("\n"));
      }
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to load axe-core from CDN. Ensure internet access is available.", e);
    }
  }
}
