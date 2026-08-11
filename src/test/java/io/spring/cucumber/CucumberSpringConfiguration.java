package io.spring.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import io.spring.RealWorldApplication;
import io.spring.cucumber.support.CucumberTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@ActiveProfiles("cucumber")
@SpringBootTest(
    classes = {RealWorldApplication.class, CucumberTestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {}
