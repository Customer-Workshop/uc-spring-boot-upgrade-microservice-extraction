package io.spring.cucumber;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/** Entry point for running the Gherkin features from an IDE. Gradle uses the cucumberTest task. */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.spring.cucumber")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, summary, html:build/reports/cucumber/cucumber.html")
public class RunCucumberTest {}
