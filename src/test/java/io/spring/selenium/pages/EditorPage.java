package io.spring.selenium.pages;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class EditorPage extends BasePage {

  @FindBy(css = "input[placeholder='Article Title']")
  private WebElement titleInput;

  @FindBy(css = "input[placeholder=\"What's this article about?\"]")
  private WebElement descriptionInput;

  @FindBy(css = "textarea[placeholder='Write your article (in markdown)']")
  private WebElement bodyTextarea;

  @FindBy(css = "input[placeholder='Enter tags']")
  private WebElement tagsInput;

  @FindBy(css = "button.btn-primary")
  private WebElement publishButton;

  public EditorPage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl) {
    driver.get(baseUrl + "/editor/new");
  }

  public void createArticle(String title, String description, String body, String... tags) {
    type(titleInput, title);
    type(descriptionInput, description);
    type(bodyTextarea, body);
    for (String tag : tags) {
      type(tagsInput, tag);
      tagsInput.sendKeys(Keys.ENTER);
    }
    click(publishButton);
  }

  public void updateArticle(String title, String description, String body) {
    if (title != null) {
      type(titleInput, title);
    }
    if (description != null) {
      type(descriptionInput, description);
    }
    if (body != null) {
      type(bodyTextarea, body);
    }
    click(publishButton);
  }
}
