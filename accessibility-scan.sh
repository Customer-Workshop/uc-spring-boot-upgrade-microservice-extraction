#!/bin/bash
# =============================================================================
# Accessibility Scanner - Scan any website for WCAG 2.1 compliance
# =============================================================================
#
# Usage:
#   ./accessibility-scan.sh <url1> [url2] [url3] ...
#
# Examples:
#   ./accessibility-scan.sh https://www.example.com
#   ./accessibility-scan.sh https://google.com https://github.com https://wikipedia.org
#
# The scanner uses axe-core to check pages against WCAG 2.1 Level A and AA
# success criteria, plus accessibility best practices.
#
# Reports are generated in: build/reports/accessibility/
#   - accessibility-report.html (detailed HTML report)
#   - accessibility-report.txt  (plain text summary)
#
# Requirements:
#   - Java 11+
#   - Chrome/Chromium browser
#   - Internet access (for axe-core CDN)
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Default URLs if none provided
if [ $# -eq 0 ]; then
    echo "=============================================="
    echo "  Accessibility Scanner (axe-core + Selenium)"
    echo "=============================================="
    echo ""
    echo "Usage: $0 <url1> [url2] [url3] ..."
    echo ""
    echo "Examples:"
    echo "  $0 https://www.example.com"
    echo "  $0 https://google.com https://github.com"
    echo ""
    echo "To scan default URLs (example.com, google.com, wikipedia.org):"
    echo "  $0 --defaults"
    echo ""
    exit 1
fi

# Join URLs with commas
if [ "$1" == "--defaults" ]; then
    URLS="https://www.example.com,https://www.google.com,https://www.wikipedia.org"
else
    URLS=$(IFS=,; echo "$*")
fi

echo "=============================================="
echo "  Accessibility Scanner"
echo "=============================================="
echo ""
echo "Target URLs: $URLS"
echo ""

export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-11-openjdk-amd64}

# Run the accessibility scan via Gradle
./gradlew accessibilityScan -Daccessibility.urls="$URLS" --no-daemon

echo ""
echo "=============================================="
echo "  Scan Complete!"
echo "=============================================="
echo ""
echo "Reports available at:"
echo "  HTML: build/reports/accessibility/accessibility-report.html"
echo "  Text: build/reports/accessibility/accessibility-report.txt"
echo ""
