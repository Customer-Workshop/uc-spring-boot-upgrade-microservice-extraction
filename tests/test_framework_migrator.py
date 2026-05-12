"""Tests for agent/framework_migrator.py"""

import os
import sys

import pytest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from agent.framework_migrator import FrameworkMigrator, extract_files_from_zip


SELENIUM_JAVA_CODE = '''
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import org.testng.Assert;

public class LoginTest {
    WebDriver driver = new ChromeDriver();

    @Test
    public void testLogin() {
        driver.get("https://example.com/login");
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("secret");
        driver.findElement(By.cssSelector(".btn-login")).click();
        Assert.assertTrue(driver.findElement(By.id("dashboard")).isDisplayed());
    }

    @Test
    public void testLogout() {
        driver.findElement(By.linkText("Logout")).click();
        Thread.sleep(2000);
    }
}
'''

CYPRESS_CODE = '''
describe('Login Page', () => {
    beforeEach(() => {
        cy.visit('https://example.com/login')
    })

    it('should login successfully', () => {
        cy.get('#username').type('admin')
        cy.get('#password').type('secret')
        cy.get('.btn-login').click()
        cy.url().should('include', '/dashboard')
        cy.get('#welcome').should('be.visible')
    })

    it('should show error for invalid credentials', () => {
        cy.get('#username').type('wrong')
        cy.get('#password').type('wrong')
        cy.get('.btn-login').click()
        cy.get('.error-msg').should('contain', 'Invalid')
    })
})
'''

SELENIUM_PYTHON_CODE = '''
from selenium import webdriver
from selenium.webdriver.common.by import By
import time

def test_login():
    driver = webdriver.Chrome()
    driver.get("https://example.com/login")
    driver.find_element(By.ID, "username").send_keys("admin")
    driver.find_element(By.CSS_SELECTOR, ".btn-login").click()
    time.sleep(2)
    assert "dashboard" in driver.current_url
    driver.quit()
'''

ROBOT_CODE = '''*** Test Cases ***
Login Test
    Open Browser    https://example.com/login    chrome
    Input Text    id=username    admin
    Input Text    id=password    secret
    Click Element    css=.btn-login
    Wait Until Element Is Visible    id=dashboard
    Page Should Contain    Welcome
    Close Browser

Logout Test
    Click Element    id=logout-btn
    Sleep    2s
'''


class TestDetectFramework:
    def setup_method(self):
        self.migrator = FrameworkMigrator()

    def test_detect_selenium_java(self):
        fw, lang = self.migrator.detect_framework({"LoginTest.java": SELENIUM_JAVA_CODE})
        assert lang == "java"
        assert fw in ("selenium_java", "testng", "junit")

    def test_detect_cypress(self):
        fw, lang = self.migrator.detect_framework({"login.cy.js": CYPRESS_CODE})
        assert fw == "cypress"
        assert lang == "javascript"

    def test_detect_selenium_python(self):
        fw, lang = self.migrator.detect_framework({"test_login.py": SELENIUM_PYTHON_CODE})
        assert fw == "selenium_python"
        assert lang == "python"

    def test_detect_robot(self):
        fw, lang = self.migrator.detect_framework({"login.robot": ROBOT_CODE})
        assert fw == "robot"
        assert lang == "robot"

    def test_detect_unknown(self):
        fw, lang = self.migrator.detect_framework({"readme.md": "# Just a readme"})
        assert fw == "unknown"


class TestMigrateJava:
    def setup_method(self):
        self.migrator = FrameworkMigrator()

    def test_migration_creates_result(self):
        result = self.migrator.migrate({"LoginTest.java": SELENIUM_JAVA_CODE})
        assert result.files_processed >= 1
        assert result.source_language == "java"
        assert len(result.migrated_files) >= 1

    def test_migration_converts_actions(self):
        result = self.migrator.migrate({"LoginTest.java": SELENIUM_JAVA_CODE})
        code = result.migrated_files[0].playwright_code
        assert "page.goto" in code
        assert "page.locator" in code

    def test_migration_converts_assertions(self):
        result = self.migrator.migrate({"LoginTest.java": SELENIUM_JAVA_CODE})
        code = result.migrated_files[0].playwright_code
        # Should have some form of assertion or TODO
        assert "assert" in code.lower() or "expect" in code.lower() or "TODO" in code

    def test_output_filename_is_python(self):
        result = self.migrator.migrate({"LoginTest.java": SELENIUM_JAVA_CODE})
        assert result.migrated_files[0].output_path.endswith(".py")

    def test_generates_project_structure(self):
        result = self.migrator.migrate({"LoginTest.java": SELENIUM_JAVA_CODE})
        assert "conftest.py" in result.project_structure
        assert "requirements.txt" in result.project_structure


class TestMigrateCypress:
    def setup_method(self):
        self.migrator = FrameworkMigrator()

    def test_migration_creates_result(self):
        result = self.migrator.migrate({"login.cy.js": CYPRESS_CODE})
        assert result.source_framework == "cypress"
        assert result.tests_migrated >= 1

    def test_converts_cy_visit(self):
        result = self.migrator.migrate({"login.cy.js": CYPRESS_CODE})
        code = result.migrated_files[0].playwright_code
        assert "page.goto" in code

    def test_converts_cy_get_type(self):
        result = self.migrator.migrate({"login.cy.js": CYPRESS_CODE})
        code = result.migrated_files[0].playwright_code
        assert "page.locator" in code
        assert ".fill(" in code

    def test_converts_cy_should(self):
        result = self.migrator.migrate({"login.cy.js": CYPRESS_CODE})
        code = result.migrated_files[0].playwright_code
        assert "expect" in code or "assert" in code or "TODO" in code


class TestMigrateSeleniumPython:
    def setup_method(self):
        self.migrator = FrameworkMigrator()

    def test_migration_creates_result(self):
        result = self.migrator.migrate({"test_login.py": SELENIUM_PYTHON_CODE})
        assert result.source_framework == "selenium_python"
        assert result.files_processed == 1

    def test_removes_selenium_imports(self):
        result = self.migrator.migrate({"test_login.py": SELENIUM_PYTHON_CODE})
        code = result.migrated_files[0].playwright_code
        assert "from selenium" not in code

    def test_converts_actions(self):
        result = self.migrator.migrate({"test_login.py": SELENIUM_PYTHON_CODE})
        code = result.migrated_files[0].playwright_code
        assert "page.goto" in code


class TestMigrateRobot:
    def setup_method(self):
        self.migrator = FrameworkMigrator()

    def test_migration_creates_result(self):
        result = self.migrator.migrate({"login.robot": ROBOT_CODE})
        assert result.source_framework == "robot"
        assert result.tests_migrated >= 1

    def test_converts_keywords(self):
        result = self.migrator.migrate({"login.robot": ROBOT_CODE})
        code = result.migrated_files[0].playwright_code
        assert "page.goto" in code or "page.locator" in code


class TestExtractFilesFromZip:
    def test_returns_empty_for_invalid(self):
        result = extract_files_from_zip(b"not a zip")
        assert result == {}

    def test_extracts_valid_zip(self):
        import zipfile
        from io import BytesIO
        buf = BytesIO()
        with zipfile.ZipFile(buf, 'w') as zf:
            zf.writestr("test/LoginTest.java", SELENIUM_JAVA_CODE)
            zf.writestr("test/readme.md", "readme")
        result = extract_files_from_zip(buf.getvalue())
        assert "test/LoginTest.java" in result
        assert "WebDriver" in result["test/LoginTest.java"]
