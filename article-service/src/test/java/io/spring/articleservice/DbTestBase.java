package io.spring.articleservice;

import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.test.context.ActiveProfiles;

@MybatisTest
@ActiveProfiles("test")
public abstract class DbTestBase {}
