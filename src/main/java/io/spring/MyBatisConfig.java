package io.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** Spring configuration that enables transaction management for MyBatis persistence. */
@Configuration
@EnableTransactionManagement
public class MyBatisConfig {}
