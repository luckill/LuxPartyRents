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
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static org.junit.jupiter.api.Assertions.assertTrue;



public class UserOrderTest {

    private SelenideElement inputEmail = $("#email");
    private SelenideElement inputPassword = $("#password");
    private SelenideElement buttonSign = $("button[class$='btn-primary']");

    public SelenideElement liUserOrders = $("li[class$='user-only']");

    public SelenideElement buttonTogglePastOrders = $("#togglePastOrders");

    public SelenideElement trUnpaidUndefinedReceived = $("tr[class='clickable-row']");

    public SelenideElement buttonModal = $("#cancelOrderButton");

    public SelenideElement inputCancelOrderTermsCheckbox = $("#CancelOrderTermsCheckbox");

    public SelenideElement buttonConfirmCancelOrder = $("#confirmCancelOrderButton");

    public SelenideElement buttonModal2 = $("html > body > div:nth-of-type(4) > div:nth-of-type(2) > div > div > div:nth-of-type(3) > div > div > div:nth-of-type(1) > button");

    public SelenideElement trUnpaidUndefinedCancelled = $("tr[class='clickable-row']");
    

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
    public void EmptyCurrentOrdersAlert() {
        //Logs in
        inputEmail.sendKeys("bkalathil1337@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();

        //Have no current orders present on user
        //Access User Orders
        liUserOrders.click();
        
        // Switch to the alert box
        Alert alert = switchTo().alert();

        // Retrieve the alert text
        String alertText = alert.getText();

        // Assert that the alert text contains specific words
        assertTrue(alertText.contains("Error fetching current orders:"), "Alert text does not contain expected words!");

        // accept the alert to close it
        alert.accept();


    }

    @Test
    public void EmptyViewPastOrdersAlert() {
        //Logs in
        inputEmail.sendKeys("bkalathil1337@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();

        //Have no past orders present on user
        //Access User Orders
        liUserOrders.click();

        buttonTogglePastOrders.click();

        // Switch to the alert box
        Alert alert = switchTo().alert();

        // Retrieve the alert text
        String alertText = alert.getText();

        // Assert that the alert text contains specific words
        assertTrue(alertText.contains("Error fetching past orders:"), "Alert text does not contain expected words!");

        // accept the alert to close it
        alert.accept();

    }

    @Test
    public void SuccessfulViewCurrentOrders() {
        //Logs in
        inputEmail.sendKeys("bkalathil1337@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        
        //Access User Orders
        liUserOrders.click();

        trUnpaidUndefinedReceived.click();

        webdriver().shouldHave(urlContaining("http://localhost:8080/order_detail?id="));
        


    }

    @Test
    public void SuccessfulCancelCurrentOrders() {
        //Logs in
        inputEmail.sendKeys("bkalathil1337@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();

        //Access User Orders
        liUserOrders.click();
        //click on order
        trUnpaidUndefinedReceived.click();

        webdriver().shouldHave(urlContaining("http://localhost:8080/order_detail?id="));
        buttonModal.click();
        
        inputCancelOrderTermsCheckbox.click();
        
        buttonConfirmCancelOrder.click();
        // Switch to the alert box
        Alert alert = switchTo().alert();
        alert.accept();
        Alert alert2 = switchTo().alert();
        alert2.accept();
        buttonModal2.click();
        open("http://localhost:8080/user_order");
        Alert alert3 = switchTo().alert();
        // Retrieve the alert text
        String alertText = alert3.getText();

        // Assert that the alert text contains specific words
        assertTrue(alertText.contains("Error fetching current orders:"), "Alert text does not contain expected words!");
        alert3.accept();
    }
    
    @Test
    public void SuccessfulViewPastOrders() {
        //Logs in
        inputEmail.sendKeys("bkalathil1337@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();

        //Access User Orders
        liUserOrders.click();

        Alert alert = switchTo().alert();
        alert.accept();

        buttonTogglePastOrders.click();
        //click on order
        trUnpaidUndefinedReceived.click();
        webdriver().shouldHave(urlContaining("http://localhost:8080/order_detail?id="));



    }



}
