package io.spring.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/** Page Object for the Article Editor page. */
public class EditorPage extends BasePage {

  @FindBy(css = "input[placeholder='Article Title']")
  private WebElement titleInput;

  @FindBy(css = "input[placeholder=\"What's this article about?\"]")
  private WebElement descriptionInput;

  @FindBy(css = "textarea[placeholder='Write your article (in markdown)']")
  private WebElement bodyInput;

  @FindBy(css = "input[placeholder='Enter tags']")
  private WebElement tagInput;

  @FindBy(css = "button[type='submit']")
  private WebElement publishButton;

  public EditorPage(WebDriver driver) {
    super(driver);
  }

  public void enterTitle(String title) {
    type(titleInput, title);
  }

  public void enterDescription(String description) {
    type(descriptionInput, description);
  }

  public void enterBody(String body) {
    type(bodyInput, body);
  }

  public void enterTag(String tag) {
    type(tagInput, tag);
  }

  public void clickPublish() {
    click(publishButton);
  }

  public void createArticle(String title, String description, String body) {
    enterTitle(title);
    enterDescription(description);
    enterBody(body);
    clickPublish();
  }
}
