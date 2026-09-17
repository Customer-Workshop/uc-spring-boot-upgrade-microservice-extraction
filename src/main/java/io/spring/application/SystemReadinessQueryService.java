package io.spring.application;

import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.ReadinessCheckData;
import io.spring.application.data.ReadinessData;
import io.spring.application.data.ReadinessRuntimeData;
import io.spring.application.data.ReadinessSummaryData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class SystemReadinessQueryService {
  private static final String PASS = "PASS";
  private static final String FAIL = "FAIL";
  private static final String TARGET_JAVA_VERSION = "17";
  private static final String TARGET_SPRING_BOOT_VERSION = "3.2";
  private static final String CLASS_PATTERN = "classpath*:io/spring/**/*.class";

  public ReadinessData readiness() {
    String javaVersion = System.getProperty("java.version");
    String springBootVersion = SpringBootVersion.getVersion();

    List<ReadinessCheckData> checks = new ArrayList<>();
    checks.add(javaRuntimeCheck(javaVersion));
    checks.add(springBootCheck(springBootVersion));
    checks.add(javaxToJakartaCheck());
    checks.add(jodaTimeCheck());
    checks.add(webSecurityAdapterCheck());

    int pass = (int) checks.stream().filter(c -> PASS.equals(c.getStatus())).count();
    return new ReadinessData(
        new ReadinessRuntimeData(
            javaVersion, springBootVersion, TARGET_JAVA_VERSION, TARGET_SPRING_BOOT_VERSION),
        checks,
        new ReadinessSummaryData(checks.size(), pass, checks.size() - pass));
  }

  private ReadinessCheckData javaRuntimeCheck(String javaVersion) {
    boolean pass = Runtime.version().feature() >= 17;
    return new ReadinessCheckData(
        "java-17-runtime",
        "Running on Java 17+",
        status(pass),
        "Current runtime is Java " + javaVersion);
  }

  private ReadinessCheckData springBootCheck(String springBootVersion) {
    boolean pass = majorVersion(springBootVersion) >= 3;
    return new ReadinessCheckData(
        "spring-boot-3",
        "Spring Boot 3.x",
        status(pass),
        "Current version is " + springBootVersion);
  }

  private ReadinessCheckData javaxToJakartaCheck() {
    int count = countClassesReferencing("javax/");
    return new ReadinessCheckData(
        "javax-to-jakarta",
        "No javax.* imports (jakarta migration)",
        status(count == 0),
        count == 0 ? "No javax.* imports found" : count + " classes still import javax.*");
  }

  private ReadinessCheckData jodaTimeCheck() {
    try {
      Class<?> dateTime = Class.forName("org.joda.time.DateTime");
      String version = dateTime.getPackage().getImplementationVersion();
      return new ReadinessCheckData(
          "joda-time",
          "No Joda-Time usage (java.time)",
          FAIL,
          "joda-time " + (version == null ? "unknown" : version) + " on classpath");
    } catch (ClassNotFoundException e) {
      return new ReadinessCheckData(
          "joda-time", "No Joda-Time usage (java.time)", PASS, "joda-time not on classpath");
    }
  }

  private ReadinessCheckData webSecurityAdapterCheck() {
    String superclass = WebSecurityConfig.class.getSuperclass().getSimpleName();
    boolean fail = "WebSecurityConfigurerAdapter".equals(superclass);
    return new ReadinessCheckData(
        "websecurity-adapter",
        "No WebSecurityConfigurerAdapter",
        status(!fail),
        fail
            ? "WebSecurityConfig extends WebSecurityConfigurerAdapter"
            : "WebSecurityConfig does not extend WebSecurityConfigurerAdapter");
  }

  private int countClassesReferencing(String marker) {
    try {
      Resource[] resources = new PathMatchingResourcePatternResolver().getResources(CLASS_PATTERN);
      int count = 0;
      for (Resource resource : resources) {
        String bytes =
            new String(
                StreamUtils.copyToByteArray(resource.getInputStream()),
                StandardCharsets.ISO_8859_1);
        if (bytes.contains(marker)) {
          count++;
        }
      }
      return count;
    } catch (IOException e) {
      return 0;
    }
  }

  private static int majorVersion(String version) {
    try {
      return Integer.parseInt(version.split("\\.")[0]);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private static String status(boolean pass) {
    return pass ? PASS : FAIL;
  }
}
