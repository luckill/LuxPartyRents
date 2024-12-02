package com.example.demo;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;
import org.openqa.selenium.chrome.ChromeOptions;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;


import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDetailsTest {

    private SelenideElement inputEmail = $("#email");
    private SelenideElement inputPassword = $("#password");
    private SelenideElement buttonSign = $("button[class$='btn-primary']");

    public SelenideElement linkAccountDetails = $("li[class$='logged-in-only'] a");

    public SelenideElement inputFName = $("#fname");

    public SelenideElement inputLName = $("#lname");

    public SelenideElement buttonEdit = $("#button1");

    public SelenideElement buttonDelAcc = $("#del_acc");
    
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
    public void EditAccount() {
        //Logs in
        inputEmail.sendKeys("123@email.com"); //enter desired email here
        inputPassword.sendKeys("Password1!"); //enter desired password here
        buttonSign.click();
        linkAccountDetails.click();
        //change credentials
        inputFName.clear();
        inputLName.clear();
        inputFName.sendKeys("John");
        inputLName.sendKeys("Doe");
        buttonEdit.click();

        //check if details were updated
        inputFName.shouldHave(value("John"));
        inputLName.shouldHave(value("Doe"));

    }

    @Test
    public void DeleteAccount() {
        //Logs in
        inputEmail.sendKeys("theunlegitclasher@gmail.com"); //enter desired email here
        inputPassword.sendKeys("Password1!"); //enter desired password here
        buttonSign.click();
        linkAccountDetails.click();
        buttonDelAcc.click();
        // Switch to the alert box
        Alert alert = switchTo().alert();

        // Retrieve the alert text
        String alertText = alert.getText();

        // Assert that the alert text contains specific words
        assertTrue(alertText.contains("Account deleted successfully!"), "Alert text does not contain expected words!");

        // accept the alert to close it
        alert.accept();

    }


}
