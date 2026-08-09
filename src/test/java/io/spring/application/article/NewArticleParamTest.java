package io.spring.application.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Set;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NewArticleParamTest {
  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  public static void setUpValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  public static void closeValidator() {
    validatorFactory.close();
  }

  @Test
  public void should_map_all_fields_through_builder() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("title")
            .description("description")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    assertThat(param.getTitle(), is("title"));
    assertThat(param.getDescription(), is("description"));
    assertThat(param.getBody(), is("body"));
    assertThat(param.getTagList(), is(Arrays.asList("java", "spring")));
  }

  @Test
  public void should_report_blank_description_and_body() {
    NewArticleParam param = new NewArticleParam("title", "", "", Arrays.asList("java"));

    Set<javax.validation.ConstraintViolation<NewArticleParam>> descriptionViolations =
        validator.validateProperty(param, "description");
    Set<javax.validation.ConstraintViolation<NewArticleParam>> bodyViolations =
        validator.validateProperty(param, "body");

    assertThat(descriptionViolations.size(), is(1));
    assertThat(descriptionViolations.iterator().next().getMessage(), is("can't be empty"));
    assertThat(bodyViolations.size(), is(1));
    assertThat(bodyViolations.iterator().next().getMessage(), is("can't be empty"));
  }
}
