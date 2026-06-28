package com.softserve.academy;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.stream.Stream;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

class GreenCityNegativeRegistrationTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static JavascriptExecutor js;


    @BeforeAll
    static void setUp() {
        ChromeOptions options = new ChromeOptions();



        options.addArguments("--lang=en-GB");
        options.setExperimentalOption("prefs", java.util.Map.of("intl.accept_languages", "en-GB,en"));

        if (System.getenv("GITHUB_ACTIONS") != null) {
            options.addArguments(
                    "--headless=new",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--window-size=1920,1080");
        }

        driver = WebDriverManager.chromedriver().capabilities(options).create();
        if (System.getenv("GITHUB_ACTIONS") == null) {
            driver.manage().window().maximize();
        }
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        js = (JavascriptExecutor) driver;
    }



    @BeforeEach
    void openRegistrationForm() {
        driver.manage().deleteAllCookies();
        driver.navigate().to("https://www.greencity.cx.ua/#/greenCity");

        WebElement signUpBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".header_sign-up-btn > span")));
        signUpBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidEmailValues")
    @DisplayName("Invalid email values -> email error")
    void shouldShowErrorForInvalidEmail(String scenario, String email) {
        typeEmail(email);
        blur();

        assertEmailErrorVisible();
        assertSignUpButtonDisabled();
    }


    @Test
    @DisplayName("All fields empty → required errors shown")
    void shouldShowErrorsForAllEmptyFields() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("repeatPassword"))).click();
        blur();

        assertEmailErrorVisible();
        assertUsernameErrorVisible();
        assertSignUpButtonDisabled();
    }


    @Test
    @DisplayName("Empty username → username required")
    void shouldShowErrorForEmptyUsername() {
        typeEmail("valid@email.com");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).click();
        blur();

        assertUsernameErrorVisible();
        assertSignUpButtonDisabled();
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPasswords")
    @DisplayName("Invalid password values -> password rule error")
    void shouldShowErrorForInvalidPassword(String scenario, String password) {
        fillValidRegistrationDataWithoutConfirm();
        typePassword(password);
        blur();

        assertPasswordErrorVisible();
        assertSignUpButtonDisabled();
    }@ParameterizedTest(name = "{0}")
    @MethodSource("invalidConfirmPasswordActions")
    @DisplayName("Invalid confirm password scenarios -> confirm error")
    void shouldShowErrorForInvalidConfirmPassword(
                    String scenario,
                    boolean shouldTypeConfirmPassword,
                    String confirmPasswordValue,
                    String expectedMessagePart
            ) {
        fillValidRegistrationDataWithoutConfirm();
        typePassword("ValidPass123!");
        applyConfirmPasswordState(shouldTypeConfirmPassword, confirmPasswordValue);
        blur();

        assertConfirmPasswordErrorVisible();
        assertConfirmPasswordErrorContains(expectedMessagePart);
        assertSignUpButtonDisabled();
    }

    private static Stream<Arguments> invalidEmailValues() {
        return Stream.of(
                Arguments.of("Email without @", "invalid-email")
        );
    }

    private static Stream<Arguments> invalidPasswords() {
        return Stream.of(
                Arguments.of("Password shorter than 8 chars", "pass12!"),
                Arguments.of("Password with space", "Pass 123!")
        );
    }

    private static Stream<Arguments> invalidConfirmPasswordActions() {
        return Stream.of(
                Arguments.of("Confirm password mismatch", true, "DifferentPass123!", ""),
                Arguments.of("Empty confirm password", false, "", "required")
        );
    }


    private void typeEmail(String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        field.click();
        js.executeScript("arguments[0].value = arguments[1];", field, value);

    }

    private void typeUsername(String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
        field.click();
        js.executeScript("arguments[0].value = arguments[1];", field, value);

    }


    private void typePassword(String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        field.click();
        js.executeScript("arguments[0].value = arguments[1];", field, value);

    }

    private void typeConfirm(String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("repeatPassword")));
        field.click();
        js.executeScript("arguments[0].value = arguments[1];", field, value);

    }

    private void fillValidRegistrationDataWithoutConfirm() {
        typeEmail("valid@email.com");
        typeUsername("ValidUsername");
    }

    private void applyConfirmPasswordState(boolean shouldTypeConfirmPassword, String confirmPasswordValue) {
        if (shouldTypeConfirmPassword) {
            typeConfirm(confirmPasswordValue);
            return;
        }
        wait.until(ExpectedConditions.elementToBeClickable(By.id("repeatPassword"))).click();
    }


    private void blur() {
        js.executeScript("if (document.activeElement) { document.activeElement.blur(); }");
    }

    private void assertEmailErrorVisible() {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email-err-msg")));
        assertTrue(error.isDisplayed(), "Email error message should be visible");
    }


    private void assertUsernameErrorVisible() {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='firstName']/following-sibling::div")));
        assertTrue(error.isDisplayed(), "Username error message should be visible");
    }

    private void assertPasswordErrorVisible() {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.password-not-valid")));
        assertTrue(error.isDisplayed(), "Password validation rules should be visible");
    }


    private void assertConfirmPasswordErrorVisible() {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirm-err-msg")));
        assertTrue(error.isDisplayed(), "Confirm password error message should be visible");
    }


    private void assertConfirmPasswordErrorContains(String expectedMessagePart) {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirm-err-msg")));
        String actualMessage = error.getText().toLowerCase();
        assertTrue(
                actualMessage.contains(expectedMessagePart.toLowerCase()),
                "Confirm password error '" + actualMessage + "' should contain '" + expectedMessagePart + "'"
        );
    }



    private void assertSignUpButtonDisabled() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type='submit'].greenStyle")));
        assertFalse(btn.isEnabled(), "The 'Sign Up' button should be disabled");
    }




    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}