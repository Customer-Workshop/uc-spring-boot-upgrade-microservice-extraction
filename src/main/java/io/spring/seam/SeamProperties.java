package io.spring.seam;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "seam")
public class SeamProperties {

  private Map<String, DomainRouting> domains = new HashMap<>();

  public Map<String, DomainRouting> getDomains() {
    return domains;
  }

  public void setDomains(Map<String, DomainRouting> domains) {
    this.domains = domains;
  }

  public RoutingMode getModeForDomain(String domain) {
    DomainRouting routing = domains.get(domain);
    if (routing == null) {
      return RoutingMode.MONOLITH;
    }
    return routing.getMode();
  }

  public String getUrlForDomain(String domain) {
    DomainRouting routing = domains.get(domain);
    if (routing == null) {
      return null;
    }
    return routing.getUrl();
  }

  public static class DomainRouting {
    private RoutingMode mode = RoutingMode.MONOLITH;
    private String url = "";

    public RoutingMode getMode() {
      return mode;
    }

    public void setMode(RoutingMode mode) {
      this.mode = mode;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }
  }
}
