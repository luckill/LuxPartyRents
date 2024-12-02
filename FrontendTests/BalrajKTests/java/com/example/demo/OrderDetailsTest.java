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

public class OrderDetailsTest {
    private SelenideElement inputEmail = $("#email");
    private SelenideElement inputPassword = $("#password");
    private SelenideElement buttonSign = $("button[class$='btn-primary']");

    public SelenideElement trPaid1 = $("html > body > div:nth-of-type(5) > section:nth-of-type(2) > div > div > table > tbody > tr:nth-of-type(1)");


    public SelenideElement linkReadyForPickup = $("a[onclick*='FOR']");

    public SelenideElement liCurrentStatusConfirmed = $("html > body > div:nth-of-type(4) > div:nth-of-type(2) > div > div > div:nth-of-type(5) > div:nth-of-type(2) > div > ul > li:nth-of-type(1)");

    public SelenideElement liOrdersAdmin = $("html > body > div:nth-of-type(2) > nav > div > div:nth-of-type(2) > ul > li:nth-of-type(6)");

    public SelenideElement buttonChangeStatus = $("#changeOrderStatusBtn");

    public SelenideElement buttonCancelOrder = $("#cancelOrderButton");

    public SelenideElement buttonReturnPayment = $("#returnPaymentBtn");

    public SelenideElement refundWholeAmount = $("html > body > div:nth-of-type(4) > div:nth-of-type(2) > div > div > div:nth-of-type(7) > div:nth-of-type(2) > ul > li:nth-of-type(2) > a");

    public SelenideElement buttonProcessReturn = $("#processReturnButton");

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
    public void ChangeOrderStatus() throws InterruptedException {
        //Logs in
        inputEmail.sendKeys("officialbkalathil@gmail.com"); //log in as admin put your credentials here
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        //navigate to order details
        liOrdersAdmin.click();
        //select order
        trPaid1.click();
        //press change order status and change it to ready for pickup
        buttonChangeStatus.click();
        linkReadyForPickup.click();
        //wait for changes to take effect and check that current status changed
        Thread.sleep(3000);
        liCurrentStatusConfirmed.shouldHave(text("Ready For Pick Up"));

    }

    @Test
    public void CancelOrder() {
        //Logs in
        inputEmail.sendKeys("officialbkalathil@gmail.com"); //log in as admin put your credentials here
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        //navigate to order details
        liOrdersAdmin.click();
        //select order
        trPaid1.click();
        buttonCancelOrder.click();
        // Switch to the alert box
        Alert alert = switchTo().alert();

        // Retrieve the alert text
        String alertText = alert.getText();

        // Assert that the alert text contains specific words
        assertTrue(alertText.contains("Order cancelled successful"), "Alert text does not contain expected words!");

        // accept the alert to close it
        alert.accept();


    }

    @Test
    public void RefundDeposit() throws InterruptedException {
        //Logs in
        inputEmail.sendKeys("officialbkalathil@gmail.com"); //log in as admin put your credentials here
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        //navigate to order details
        liOrdersAdmin.click();
        //select order
        trPaid1.click();
        //click return deposit
        buttonReturnPayment.click();   
        refundWholeAmount.click();
        Thread.sleep(2000);
        liCurrentStatusConfirmed.shouldHave(text("Completed"));

    }

    @Test
    public void MarkAsReturned() throws InterruptedException {
        //Logs in
        inputEmail.sendKeys("officialbkalathil@gmail.com"); //log in as admin put your credentials here
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        //navigate to order details
        liOrdersAdmin.click();
        //select order
        trPaid1.click();
        buttonProcessReturn.click();
        Thread.sleep(2000);
        liCurrentStatusConfirmed.shouldHave(text("Returned"));


    }


}


