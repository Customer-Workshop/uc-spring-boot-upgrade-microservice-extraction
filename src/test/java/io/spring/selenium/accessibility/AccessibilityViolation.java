package io.spring.selenium.accessibility;

import java.util.List;

/** Represents a single accessibility violation found during a scan. */
public class AccessibilityViolation {

  private final String ruleId;
  private final String description;
  private final String help;
  private final String helpUrl;
  private final String impact;
  private final List<String> wcagTags;
  private final List<AffectedNode> affectedNodes;

  public AccessibilityViolation(
      String ruleId,
      String description,
      String help,
      String helpUrl,
      String impact,
      List<String> wcagTags,
      List<AffectedNode> affectedNodes) {
    this.ruleId = ruleId;
    this.description = description;
    this.help = help;
    this.helpUrl = helpUrl;
    this.impact = impact;
    this.wcagTags = wcagTags;
    this.affectedNodes = affectedNodes;
  }

  public String getRuleId() {
    return ruleId;
  }

  public String getDescription() {
    return description;
  }

  public String getHelp() {
    return help;
  }

  public String getHelpUrl() {
    return helpUrl;
  }

  public String getImpact() {
    return impact;
  }

  public List<String> getWcagTags() {
    return wcagTags;
  }

  public List<AffectedNode> getAffectedNodes() {
    return affectedNodes;
  }

  public int getNodeCount() {
    return affectedNodes.size();
  }

  @Override
  public String toString() {
    return String.format(
        "[%s] %s - %s (%d nodes affected)",
        impact.toUpperCase(), ruleId, help, affectedNodes.size());
  }

  /** Represents a single DOM node affected by a violation. */
  public static class AffectedNode {

    private final String html;
    private final String selector;
    private final String failureSummary;

    public AffectedNode(String html, String selector, String failureSummary) {
      this.html = html;
      this.selector = selector;
      this.failureSummary = failureSummary;
    }

    public String getHtml() {
      return html;
    }

    public String getSelector() {
      return selector;
    }

    public String getFailureSummary() {
      return failureSummary;
    }

    @Override
    public String toString() {
      return String.format("Node{selector='%s', html='%s'}", selector, truncate(html, 80));
    }

    private String truncate(String str, int maxLen) {
      if (str == null) return "";
      return str.length() <= maxLen ? str : str.substring(0, maxLen) + "...";
    }
  }
}
