package io.spring.cucumber;

import io.cucumber.java.Before;

/** Resets the database and the scenario state before every scenario. */
public class Hooks {

  private final DatabaseCleaner databaseCleaner;
  private final ScenarioContext scenarioContext;

  public Hooks(DatabaseCleaner databaseCleaner, ScenarioContext scenarioContext) {
    this.databaseCleaner = databaseCleaner;
    this.scenarioContext = scenarioContext;
  }

  @Before
  public void resetState() {
    databaseCleaner.clean();
    scenarioContext.reset();
  }
}
