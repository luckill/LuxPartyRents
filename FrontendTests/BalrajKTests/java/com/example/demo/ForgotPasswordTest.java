package com.example.demo;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.demo.MainPage;
import org.openqa.selenium.Alert;
import org.openqa.selenium.chrome.ChromeOptions;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;


import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.url;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgotPasswordTest {

    SelenideElement userGreeting = $("#user-greeting");
    private SelenideElement inputEmail = $("#email");
    private SelenideElement inputPassword = $("#password");
    private SelenideElement buttonSign = $("button[class$='btn-primary']");
    private SelenideElement divError = $("#error");
    private SelenideElement successMessage = $("#successMessage");
    

    public SelenideElement buttonLogin = $("button[class$='btn-primary']");

    public SelenideElement linkForgetPassword = $("html > body > div:nth-of-type(2) > form > a:nth-of-type(1)");

    public SelenideElement inputEmail2 = $("#email");

    public SelenideElement buttonFindAccount = $("#findAccountButton");

    @BeforeAll
    public static void setUpAll() {
        Configuration.browserSize = "1280x800";
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @BeforeEach
    public void setUp() {
        // Fix the issue https://github.com/SeleniumHQ/selenium/issues/11750
        Configuration.browserCapabilities = new ChromeOptions().addArguments("--remote-allow-origins=*");
        open("http://localhost:8080/login");
    }

    @AfterEach
    public void tearDown() {
        Selenide.closeWebDriver();
    }

    @Test
    public void SuccessfulPasswordReset() {
        //Logs in
       inputEmail.sendKeys("bkalathil1337@gmail.com");
       inputPassword.sendKeys("Password1!");
       buttonSign.click();
       //Initiate forget password
       linkForgetPassword.click();
       inputEmail2.sendKeys("bkalathil1337@gmail.com");
       buttonFindAccount.click();

        // Switch to the alert box
        Alert alert = switchTo().alert();

        // Retrieve the alert text
        String alertText = alert.getText();

        // Assert that the alert text contains specific words
        assertTrue(alertText.contains("account found and we send a password reset link to your email."), "Alert text does not contain expected words!");

        // accept the alert to close it
        alert.accept();
    }

    @Test
    public void WrongEmailPasswordReset() {

        //Initiate forget password
        linkForgetPassword.click();
        inputEmail2.sendKeys("123@email.com");
        buttonFindAccount.click();

        // Switch to the alert box
        Alert alert = switchTo().alert();

        // Retrieve the alert text
        String alertText = alert.getText();

        // Assert that the alert text contains specific words
        assertTrue(alertText.contains("we did not find an account associate with the email in our record"), "Alert text does not contain expected words!");

        // Optionally accept the alert to close it
        alert.accept();
    }
}
