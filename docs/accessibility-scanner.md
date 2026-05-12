# Accessibility Scanner

A comprehensive WCAG 2.1 accessibility scanner that can test **any website** for compliance issues. Built on [axe-core](https://github.com/dequelabs/axe-core) (the industry-standard accessibility testing engine) and Selenium WebDriver.

## Features

- **Scan any website** - Test any public URL for accessibility violations
- **WCAG 2.1 Level AA compliance** - Checks against WCAG 2.0/2.1 Level A, AA, and best practices
- **Detailed HTML reports** - Color-coded, interactive reports with violation details
- **Plain-text reports** - Console-friendly output for CI/CD pipelines
- **Impact classification** - Violations categorized as Critical, Serious, Moderate, or Minor
- **Remediation guidance** - Each violation includes fix suggestions and reference links
- **Multi-page scanning** - Scan multiple URLs in a single run
- **Configurable** - Configure via properties file, command line, or programmatically
- **CI/CD ready** - Gradle task with configurable failure thresholds

## Quick Start

### Command-Line Usage

```bash
# Scan a single website
./accessibility-scan.sh https://www.example.com

# Scan multiple websites
./accessibility-scan.sh https://google.com https://github.com https://wikipedia.org

# Scan with default URLs
./accessibility-scan.sh --defaults
```

### Gradle Task

```bash
# Scan using URLs from config.properties
./gradlew accessibilityScan

# Scan specific URLs via command line
./gradlew accessibilityScan -Daccessibility.urls=https://example.com,https://google.com
```

### Programmatic Usage (Java)

```java
WebsiteAccessibilityScanner scanner = new WebsiteAccessibilityScanner();
scanner.addUrl("https://example.com");
scanner.addUrl("https://myapp.com/login");
scanner.addUrl("https://myapp.com/dashboard");

List<AccessibilityScanResult> results = scanner.scanAll();
scanner.generateReports("build/reports/accessibility");
scanner.close();
```

## Configuration

Edit `src/test/resources/selenium/config.properties`:

```properties
# Comma-separated list of URLs to scan
accessibility.urls=https://www.example.com,https://www.google.com,https://www.wikipedia.org
```

## Reports

Reports are generated in `build/reports/accessibility/`:

| File | Description |
|------|-------------|
| `accessibility-report.html` | Interactive HTML report with color-coded violations |
| `accessibility-report.txt` | Plain-text report for terminal/CI output |

### HTML Report Contents

- **Summary dashboard** - Total violations by severity level
- **Per-page breakdown** - Violations grouped by scanned page
- **Violation details** - Rule ID, description, impact level, WCAG criteria
- **Affected elements** - CSS selectors and HTML snippets of violating elements
- **Remediation links** - Direct links to Deque University fix guidance

## What It Checks

The scanner tests against the following rulesets:

| Ruleset | Description |
|---------|-------------|
| `wcag2a` | WCAG 2.0 Level A |
| `wcag2aa` | WCAG 2.0 Level AA |
| `wcag21a` | WCAG 2.1 Level A |
| `wcag21aa` | WCAG 2.1 Level AA |
| `best-practice` | Common accessibility best practices |

### Common Issues Detected

- Missing alt text on images
- Insufficient color contrast
- Missing form labels
- Keyboard navigation issues
- ARIA attribute misuse
- Missing document language
- Empty links and buttons
- Duplicate IDs
- Missing heading hierarchy

## Architecture

```
src/test/java/io/spring/selenium/accessibility/
├── AxeCoreScanner.java              # Core scanner - injects axe-core via Selenium
├── AccessibilityScanResult.java     # Scan result data model
├── AccessibilityViolation.java      # Individual violation data model
├── AccessibilityReportGenerator.java # HTML and text report generation
├── WebsiteAccessibilityScanner.java # Multi-site scanner with CLI main()
└── AccessibilityScanTest.java       # TestNG test integration
```

## Requirements

- Java 11+
- Chrome or Chromium browser
- Internet access (to load axe-core from CDN)
- Gradle 7.4+ (via wrapper)

## Integration with CI/CD

The `accessibilityScan` Gradle task can be integrated into CI pipelines:

```yaml
# Example GitHub Actions step
- name: Run Accessibility Scan
  run: ./gradlew accessibilityScan -Daccessibility.urls=${{ env.DEPLOY_URL }}

- name: Upload Report
  uses: actions/upload-artifact@v3
  with:
    name: accessibility-report
    path: build/reports/accessibility/
```
