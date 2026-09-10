package io.spring.application;

import io.spring.application.data.ReadinessData;
import io.spring.application.data.ReadinessData.ReadinessCheck;
import io.spring.application.data.ReadinessData.ReadinessSummary;
import io.spring.application.data.ReadinessData.RuntimeInfo;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

@Service
public class SystemReadinessQueryService {
  private static final String TARGET_JAVA_VERSION = "17";
  private static final String TARGET_SPRING_BOOT_VERSION = "3.2";
  private static final String PASS = "PASS";
  private static final String FAIL = "FAIL";
  private static final String CLASS_PATTERN = "classpath*:io/spring/**/*.class";

  public ReadinessData readiness() {
    String javaVersion = System.getProperty("java.version");
    String bootVersion = SpringBootVersion.getVersion();

    List<ReadinessCheck> checks = new ArrayList<>();
    checks.add(javaCheck(javaVersion));
    checks.add(springBootCheck(bootVersion));
    checks.add(javaxCheck());
    checks.add(jodaTimeCheck());
    checks.add(webSecurityAdapterCheck());

    int pass = (int) checks.stream().filter(c -> PASS.equals(c.getStatus())).count();
    ReadinessSummary summary = new ReadinessSummary(checks.size(), pass, checks.size() - pass);

    return new ReadinessData(
        new RuntimeInfo(javaVersion, bootVersion, TARGET_JAVA_VERSION, TARGET_SPRING_BOOT_VERSION),
        checks,
        summary);
  }

  private ReadinessCheck javaCheck(String javaVersion) {
    boolean pass = Runtime.version().feature() >= 17;
    return new ReadinessCheck(
        "java-17-runtime",
        "Running on Java 17+",
        status(pass),
        "Current runtime is Java " + javaVersion);
  }

  private ReadinessCheck springBootCheck(String bootVersion) {
    boolean pass = majorVersion(bootVersion) >= 3;
    return new ReadinessCheck(
        "spring-boot-3", "Spring Boot 3.x", status(pass), "Current version is " + bootVersion);
  }

  private ReadinessCheck javaxCheck() {
    int count = countClassesReferencingJavax();
    return new ReadinessCheck(
        "javax-to-jakarta",
        "No javax.* imports (jakarta migration)",
        status(count == 0),
        count + " classes still import javax.*");
  }

  private ReadinessCheck jodaTimeCheck() {
    String detail;
    boolean pass;
    try {
      Class<?> dateTime = Class.forName("org.joda.time.DateTime");
      String version = dateTime.getPackage().getImplementationVersion();
      detail = "joda-time " + (version == null ? "present" : version) + " on classpath";
      pass = false;
    } catch (ClassNotFoundException e) {
      detail = "Joda-Time not on classpath";
      pass = true;
    }
    return new ReadinessCheck("joda-time", "No Joda-Time usage (java.time)", status(pass), detail);
  }

  private ReadinessCheck webSecurityAdapterCheck() {
    boolean extendsAdapter = false;
    try {
      Class<?> superclass =
          Class.forName("io.spring.api.security.WebSecurityConfig").getSuperclass();
      extendsAdapter =
          superclass != null && "WebSecurityConfigurerAdapter".equals(superclass.getSimpleName());
    } catch (ClassNotFoundException e) {
      extendsAdapter = false;
    }
    return new ReadinessCheck(
        "websecurity-adapter",
        "No WebSecurityConfigurerAdapter",
        status(!extendsAdapter),
        extendsAdapter
            ? "WebSecurityConfig extends WebSecurityConfigurerAdapter"
            : "WebSecurityConfig does not extend WebSecurityConfigurerAdapter");
  }

  private int countClassesReferencingJavax() {
    try {
      Resource[] resources = new PathMatchingResourcePatternResolver().getResources(CLASS_PATTERN);
      int count = 0;
      for (Resource resource : resources) {
        if (referencesJavax(resource)) {
          count++;
        }
      }
      return count;
    } catch (IOException e) {
      return 0;
    }
  }

  private boolean referencesJavax(Resource resource) throws IOException {
    try (InputStream in = resource.getInputStream()) {
      String content = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
      return content.contains("javax/");
    }
  }

  private int majorVersion(String version) {
    if (version == null || version.isEmpty()) {
      return 0;
    }
    try {
      return Integer.parseInt(version.split("\\.")[0]);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private String status(boolean pass) {
    return pass ? PASS : FAIL;
  }
}
