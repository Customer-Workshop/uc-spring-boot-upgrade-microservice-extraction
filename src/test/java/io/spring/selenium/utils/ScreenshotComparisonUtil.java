package io.spring.selenium.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import javax.imageio.ImageIO;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

/** Utility class for capturing full-page screenshots and comparing them against baselines. */
public class ScreenshotComparisonUtil {

  public static final String BASELINE_DIR = "src/test/resources/selenium/baselines/";
  public static final String ACTUAL_DIR = "build/reports/selenium/visual-regression/actual/";
  public static final String DIFF_DIR = "build/reports/selenium/visual-regression/diffs/";
  public static final double DEFAULT_DIFF_THRESHOLD = 0.5;

  private static double getDiffThreshold() {
    try {
      Properties config = new Properties();
      File configFile = new File("src/test/resources/selenium/config.properties");
      if (configFile.exists()) {
        config.load(new FileInputStream(configFile));
        String threshold = config.getProperty("visual.diff.threshold");
        if (threshold != null) {
          return Double.parseDouble(threshold);
        }
      }
    } catch (IOException | NumberFormatException e) {
      System.out.println(
          "Could not read visual.diff.threshold from config, using default: "
              + DEFAULT_DIFF_THRESHOLD);
    }
    return DEFAULT_DIFF_THRESHOLD;
  }

  /**
   * Captures a full-page screenshot using ashot's viewport pasting strategy.
   *
   * @param driver the WebDriver instance
   * @param pageName a descriptive name used for the output filename
   * @return the captured BufferedImage
   */
  public static BufferedImage captureFullPageScreenshot(WebDriver driver, String pageName)
      throws IOException {
    Files.createDirectories(Paths.get(ACTUAL_DIR));

    Screenshot screenshot =
        new AShot()
            .shootingStrategy(ShootingStrategies.viewportPasting(100))
            .takeScreenshot(driver);

    BufferedImage image = screenshot.getImage();
    File outputFile = new File(ACTUAL_DIR + pageName + ".png");
    ImageIO.write(image, "PNG", outputFile);
    return image;
  }

  /**
   * Compares the current page screenshot against the stored baseline.
   *
   * <p>If no baseline exists, the current screenshot is saved as the new baseline.
   *
   * @param driver the WebDriver instance
   * @param pageName a descriptive name used for file lookup
   * @return a ComparisonResult describing the outcome
   */
  public static ComparisonResult compareWithBaseline(WebDriver driver, String pageName)
      throws IOException {
    Files.createDirectories(Paths.get(BASELINE_DIR));
    Files.createDirectories(Paths.get(ACTUAL_DIR));
    Files.createDirectories(Paths.get(DIFF_DIR));

    BufferedImage actualImage = captureFullPageScreenshot(driver, pageName);
    String actualPath = ACTUAL_DIR + pageName + ".png";
    String baselinePath = BASELINE_DIR + pageName + ".png";
    File baselineFile = new File(baselinePath);

    if (!baselineFile.exists()) {
      System.out.println("WARNING: No baseline found, creating initial baseline for " + pageName);
      Files.copy(
          Paths.get(actualPath), Paths.get(baselinePath), StandardCopyOption.REPLACE_EXISTING);
      return new ComparisonResult(true, 0.0, baselinePath, actualPath, null, 0);
    }

    BufferedImage baselineImage = ImageIO.read(baselineFile);

    ImageDiffer differ = new ImageDiffer();
    ImageDiff diff = differ.makeDiff(baselineImage, actualImage);

    String diffPath = DIFF_DIR + pageName + "_diff.png";
    BufferedImage diffImage = diff.getMarkedImage();
    ImageIO.write(diffImage, "PNG", new File(diffPath));

    int diffPixelCount = diff.getDiffSize();
    int totalPixels = actualImage.getWidth() * actualImage.getHeight();
    double diffPercentage = (totalPixels > 0) ? ((double) diffPixelCount / totalPixels) * 100.0 : 0;

    double threshold = getDiffThreshold();
    boolean passed = diffPercentage <= threshold;

    return new ComparisonResult(
        passed, diffPercentage, baselinePath, actualPath, diffPath, diffPixelCount);
  }

  /**
   * Copies the actual screenshot over the baseline to update it (for intentional UI changes).
   *
   * @param pageName the page whose baseline should be updated
   */
  public static void updateBaseline(String pageName) throws IOException {
    Path actualPath = Paths.get(ACTUAL_DIR + pageName + ".png");
    Path baselinePath = Paths.get(BASELINE_DIR + pageName + ".png");

    if (!Files.exists(actualPath)) {
      throw new IOException("No actual screenshot found for " + pageName + ". Run the test first.");
    }

    Files.createDirectories(baselinePath.getParent());
    Files.copy(actualPath, baselinePath, StandardCopyOption.REPLACE_EXISTING);
    System.out.println("Baseline updated for " + pageName);
  }
}
