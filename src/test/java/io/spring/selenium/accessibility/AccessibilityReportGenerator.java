package io.spring.selenium.accessibility;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Generates HTML and text reports from accessibility scan results. */
public class AccessibilityReportGenerator {

  private static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /**
   * Generates a comprehensive HTML report for multiple scan results.
   *
   * @param results list of scan results
   * @param outputPath file path for the HTML report
   */
  public void generateHtmlReport(List<AccessibilityScanResult> results, String outputPath)
      throws IOException {
    Files.createDirectories(Paths.get(outputPath).getParent());

    int totalViolations =
        results.stream().mapToInt(AccessibilityScanResult::getViolationCount).sum();
    long totalCritical =
        results.stream().mapToLong(AccessibilityScanResult::getCriticalCount).sum();
    long totalSerious = results.stream().mapToLong(AccessibilityScanResult::getSeriousCount).sum();
    long totalModerate =
        results.stream().mapToLong(AccessibilityScanResult::getModerateCount).sum();
    long totalMinor = results.stream().mapToLong(AccessibilityScanResult::getMinorCount).sum();

    try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
      writer.println("<!DOCTYPE html>");
      writer.println("<html lang=\"en\">");
      writer.println("<head>");
      writer.println("<meta charset=\"UTF-8\">");
      writer.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
      writer.println("<title>Accessibility Scan Report</title>");
      writer.println("<style>");
      writeStyles(writer);
      writer.println("</style>");
      writer.println("</head>");
      writer.println("<body>");

      writer.println("<div class=\"container\">");
      writer.println("<header>");
      writer.println("<h1>Accessibility Scan Report</h1>");
      writer.println(
          "<p class=\"timestamp\">Generated: "
              + LocalDateTime.now().format(TIMESTAMP_FORMAT)
              + "</p>");
      writer.println("</header>");

      writer.println("<section class=\"summary\">");
      writer.println("<h2>Summary</h2>");
      writer.println("<div class=\"stats-grid\">");
      writer.println(
          "<div class=\"stat-card\"><span class=\"stat-value\">"
              + results.size()
              + "</span><span class=\"stat-label\">Pages Scanned</span></div>");
      writer.println(
          "<div class=\"stat-card critical\"><span class=\"stat-value\">"
              + totalViolations
              + "</span><span class=\"stat-label\">Total Violations</span></div>");
      writer.println(
          "<div class=\"stat-card critical\"><span class=\"stat-value\">"
              + totalCritical
              + "</span><span class=\"stat-label\">Critical</span></div>");
      writer.println(
          "<div class=\"stat-card serious\"><span class=\"stat-value\">"
              + totalSerious
              + "</span><span class=\"stat-label\">Serious</span></div>");
      writer.println(
          "<div class=\"stat-card moderate\"><span class=\"stat-value\">"
              + totalModerate
              + "</span><span class=\"stat-label\">Moderate</span></div>");
      writer.println(
          "<div class=\"stat-card minor\"><span class=\"stat-value\">"
              + totalMinor
              + "</span><span class=\"stat-label\">Minor</span></div>");
      writer.println("</div>");
      writer.println("</section>");

      for (AccessibilityScanResult result : results) {
        writePageResult(writer, result);
      }

      writer.println("</div>");
      writer.println("</body>");
      writer.println("</html>");
    }
  }

  /**
   * Generates a plain-text console report.
   *
   * @param results list of scan results
   * @param outputPath file path for the text report
   */
  public void generateTextReport(List<AccessibilityScanResult> results, String outputPath)
      throws IOException {
    Files.createDirectories(Paths.get(outputPath).getParent());

    try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
      writer.println("=".repeat(80));
      writer.println("ACCESSIBILITY SCAN REPORT");
      writer.println("Generated: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
      writer.println("=".repeat(80));
      writer.println();

      int totalViolations =
          results.stream().mapToInt(AccessibilityScanResult::getViolationCount).sum();
      writer.println("SUMMARY");
      writer.println("-".repeat(40));
      writer.println("Pages Scanned: " + results.size());
      writer.println("Total Violations: " + totalViolations);
      writer.println(
          "Critical: "
              + results.stream().mapToLong(AccessibilityScanResult::getCriticalCount).sum());
      writer.println(
          "Serious: " + results.stream().mapToLong(AccessibilityScanResult::getSeriousCount).sum());
      writer.println(
          "Moderate: "
              + results.stream().mapToLong(AccessibilityScanResult::getModerateCount).sum());
      writer.println(
          "Minor: " + results.stream().mapToLong(AccessibilityScanResult::getMinorCount).sum());
      writer.println();

      for (AccessibilityScanResult result : results) {
        writer.println("=".repeat(80));
        writer.println("PAGE: " + result.getUrl());
        writer.println("Title: " + result.getPageTitle());
        writer.println("Scanned: " + result.getScanTimestamp().format(TIMESTAMP_FORMAT));
        writer.println(
            "Violations: "
                + result.getViolationCount()
                + " | Passes: "
                + result.getPassCount()
                + " | Incomplete: "
                + result.getIncompleteCount());
        writer.println("-".repeat(80));

        if (!result.hasViolations()) {
          writer.println("  No violations found.");
        } else {
          for (AccessibilityViolation violation : result.getViolations()) {
            writer.println();
            writer.println(
                "  [" + violation.getImpact().toUpperCase() + "] " + violation.getRuleId());
            writer.println("  Description: " + violation.getDescription());
            writer.println("  Help: " + violation.getHelp());
            writer.println("  WCAG: " + String.join(", ", violation.getWcagTags()));
            writer.println("  More info: " + violation.getHelpUrl());
            writer.println("  Affected elements: " + violation.getNodeCount());
            for (AccessibilityViolation.AffectedNode node : violation.getAffectedNodes()) {
              writer.println("    - Selector: " + node.getSelector());
              writer.println("      HTML: " + truncate(node.getHtml(), 120));
              if (!node.getFailureSummary().isEmpty()) {
                writer.println("      Fix: " + node.getFailureSummary());
              }
            }
          }
        }
        writer.println();
      }
    }
  }

  private void writePageResult(PrintWriter writer, AccessibilityScanResult result) {
    writer.println("<section class=\"page-result\">");
    writer.println("<h2>" + escapeHtml(result.getUrl()) + "</h2>");
    writer.println(
        "<p class=\"page-title\">Page Title: " + escapeHtml(result.getPageTitle()) + "</p>");
    writer.println(
        "<p class=\"page-stats\">Violations: "
            + result.getViolationCount()
            + " | Passes: "
            + result.getPassCount()
            + " | Incomplete: "
            + result.getIncompleteCount()
            + "</p>");

    if (!result.hasViolations()) {
      writer.println("<p class=\"no-violations\">No accessibility violations found.</p>");
    } else {
      for (AccessibilityViolation violation : result.getViolations()) {
        writer.println("<div class=\"violation " + violation.getImpact() + "\">");
        writer.println(
            "<div class=\"violation-header\">"
                + "<span class=\"impact-badge "
                + violation.getImpact()
                + "\">"
                + violation.getImpact().toUpperCase()
                + "</span>"
                + "<strong>"
                + escapeHtml(violation.getRuleId())
                + "</strong>"
                + "</div>");
        writer.println("<p>" + escapeHtml(violation.getHelp()) + "</p>");
        writer.println(
            "<p class=\"description\">" + escapeHtml(violation.getDescription()) + "</p>");
        writer.println(
            "<p class=\"wcag-tags\">WCAG: " + String.join(", ", violation.getWcagTags()) + "</p>");
        writer.println(
            "<p><a href=\""
                + escapeHtml(violation.getHelpUrl())
                + "\" target=\"_blank\">Learn more</a></p>");

        writer.println(
            "<details><summary>Affected Elements (" + violation.getNodeCount() + ")</summary>");
        writer.println("<ul class=\"node-list\">");
        for (AccessibilityViolation.AffectedNode node : violation.getAffectedNodes()) {
          writer.println("<li>");
          writer.println("<code class=\"selector\">" + escapeHtml(node.getSelector()) + "</code>");
          writer.println("<pre class=\"html\">" + escapeHtml(node.getHtml()) + "</pre>");
          if (!node.getFailureSummary().isEmpty()) {
            writer.println("<p class=\"fix\">" + escapeHtml(node.getFailureSummary()) + "</p>");
          }
          writer.println("</li>");
        }
        writer.println("</ul>");
        writer.println("</details>");
        writer.println("</div>");
      }
    }
    writer.println("</section>");
  }

  private void writeStyles(PrintWriter writer) {
    writer.println(
        "* { box-sizing: border-box; margin: 0; padding: 0; }"
            + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;"
            + " line-height: 1.6; color: #333; background: #f5f5f5; }"
            + ".container { max-width: 1200px; margin: 0 auto; padding: 2rem; }"
            + "header { text-align: center; margin-bottom: 2rem; }"
            + "header h1 { color: #1a1a2e; font-size: 2rem; }"
            + ".timestamp { color: #666; margin-top: 0.5rem; }"
            + ".summary { background: white; padding: 2rem; border-radius: 8px;"
            + " box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 2rem; }"
            + ".stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));"
            + " gap: 1rem; margin-top: 1rem; }"
            + ".stat-card { text-align: center; padding: 1rem; border-radius: 8px;"
            + " background: #f8f9fa; border: 1px solid #e9ecef; }"
            + ".stat-card.critical { border-color: #dc3545; background: #fff5f5; }"
            + ".stat-card.serious { border-color: #fd7e14; background: #fff8f0; }"
            + ".stat-card.moderate { border-color: #ffc107; background: #fffdf0; }"
            + ".stat-card.minor { border-color: #17a2b8; background: #f0fdff; }"
            + ".stat-value { display: block; font-size: 2rem; font-weight: bold; }"
            + ".stat-label { display: block; font-size: 0.85rem; color: #666; }"
            + ".page-result { background: white; padding: 2rem; border-radius: 8px;"
            + " box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 1.5rem; }"
            + ".page-result h2 { font-size: 1.1rem; word-break: break-all; color: #0066cc; }"
            + ".page-title, .page-stats { color: #666; margin: 0.3rem 0; }"
            + ".no-violations { color: #28a745; font-weight: bold; margin-top: 1rem; }"
            + ".violation { border: 1px solid #e9ecef; border-radius: 6px; padding: 1rem;"
            + " margin: 1rem 0; border-left: 4px solid #ccc; }"
            + ".violation.critical { border-left-color: #dc3545; }"
            + ".violation.serious { border-left-color: #fd7e14; }"
            + ".violation.moderate { border-left-color: #ffc107; }"
            + ".violation.minor { border-left-color: #17a2b8; }"
            + ".violation-header { display: flex; align-items: center; gap: 0.5rem;"
            + " margin-bottom: 0.5rem; }"
            + ".impact-badge { padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.7rem;"
            + " font-weight: bold; color: white; }"
            + ".impact-badge.critical { background: #dc3545; }"
            + ".impact-badge.serious { background: #fd7e14; }"
            + ".impact-badge.moderate { background: #ffc107; color: #333; }"
            + ".impact-badge.minor { background: #17a2b8; }"
            + ".description { color: #555; font-size: 0.9rem; }"
            + ".wcag-tags { color: #6c757d; font-size: 0.85rem; }"
            + "details { margin-top: 0.5rem; }"
            + "summary { cursor: pointer; color: #0066cc; font-weight: 500; }"
            + ".node-list { list-style: none; margin-top: 0.5rem; }"
            + ".node-list li { padding: 0.5rem; background: #f8f9fa; border-radius: 4px;"
            + " margin-bottom: 0.5rem; }"
            + ".selector { background: #e9ecef; padding: 0.2rem 0.4rem; border-radius: 3px;"
            + " font-size: 0.85rem; }"
            + ".html { background: #2d2d2d; color: #f8f8f2; padding: 0.5rem; border-radius: 4px;"
            + " font-size: 0.8rem; overflow-x: auto; margin-top: 0.3rem; white-space: pre-wrap;"
            + " word-break: break-all; }"
            + ".fix { color: #28a745; font-size: 0.85rem; margin-top: 0.3rem; }");
  }

  private String escapeHtml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  private String truncate(String str, int maxLen) {
    if (str == null) return "";
    return str.length() <= maxLen ? str : str.substring(0, maxLen) + "...";
  }
}
