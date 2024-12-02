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


import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.url;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShoppingCartTest {

    private SelenideElement inputEmail = $("#email");
    private SelenideElement inputPassword = $("#password");
    private SelenideElement buttonSign = $("button[class$='btn-primary']");

    public SelenideElement linkShoppingCartButton = $("#shoppingCartButton");

    public SelenideElement inputStartDate = $("#startDate");

    public SelenideElement spanNovember = $("span[aria-label='November 30, 2024']");

    public SelenideElement inputTermsCheckbox = $("#termsCheckbox");

    public SelenideElement buttonCheckout = $("#checkoutButton");

    public SelenideElement spanArrow = $("span[class='arrowUp']");

    public SelenideElement imageRounded = $("html > body > div:nth-of-type(2) > div:nth-of-type(3) > div:nth-of-type(1) > div:nth-of-type(1) > div > img:nth-of-type(2)");

    public SelenideElement buttonRounded = $("html > body > div:nth-of-type(2) > div:nth-of-type(3) > div:nth-of-type(1) > div:nth-of-type(1) > div > div:nth-of-type(2) > div > div:nth-of-type(5) > button:nth-of-type(3)");

    public SelenideElement liRentals = $("html > body > div:nth-of-type(2) > nav > div > div:nth-of-type(2) > ul > li:nth-of-type(2)");

    public SelenideElement divStreetAddress = $("html > body > div:nth-of-type(5) > div:nth-of-type(1) > div > div:nth-of-type(2) > div:nth-of-type(1) > div:nth-of-type(2) > div > div:nth-of-type(1)");

    public SelenideElement divCity = $("html > body > div:nth-of-type(5) > div:nth-of-type(1) > div > div:nth-of-type(2) > div:nth-of-type(1) > div:nth-of-type(2) > div > div:nth-of-type(3)");

    public SelenideElement divState = $("html > body > div:nth-of-type(5) > div:nth-of-type(1) > div > div:nth-of-type(2) > div:nth-of-type(1) > div:nth-of-type(2) > div > div:nth-of-type(4) > div:nth-of-type(1)");

    public SelenideElement divZipCode = $("html > body > div:nth-of-type(5) > div:nth-of-type(1) > div > div:nth-of-type(2) > div:nth-of-type(1) > div:nth-of-type(2) > div > div:nth-of-type(4) > div:nth-of-type(2)");

    public SelenideElement buttonCalculateDeliveryFee = $("button[onclick='CalculateDeliveryFee()']");

    public SelenideElement inputStreet = $("#street");

    public SelenideElement inputCity = $("#city");

    public SelenideElement inputState = $("#state");

    public SelenideElement inputZip = $("#zip");
    

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
    public void EmptyCartCheckout() throws InterruptedException {

        //Logs in
        inputEmail.sendKeys("bkalathil1337@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        Thread.sleep(3000);
        //navigate to cart
        linkShoppingCartButton.click();
        //click start date
        inputStartDate.click();
        //click november 30
        spanNovember.click();
        //check the TOS
        inputTermsCheckbox.click();
        //checkout
        buttonCheckout.click();
        //check if the checkout failed and stayed on page
        webdriver().shouldHave(url("http://localhost:8080/shoppingCart"));
    }

    @Test
    public void NoDateSelectedCheckout() throws InterruptedException {
        //Logs in
        inputEmail.sendKeys("bkalathil1337@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        Thread.sleep(3000);
        //navigate to cart
        linkShoppingCartButton.click();
        //check the TOS
        inputTermsCheckbox.click();
        //checkout
        buttonCheckout.click();
        
        // Switch to the alert box
        Alert alert = switchTo().alert();

        // Retrieve the alert text
        String alertText = alert.getText();

        // Assert that the alert text contains specific words
        assertTrue(alertText.contains("Please select a rental start date before checking out."), "Alert text does not contain expected words!");

        // accept the alert to close it
        alert.accept();
    }
    
    @Test
    public void NoAddressSelectedCheckout() throws InterruptedException {
        //Logs in
        inputEmail.sendKeys("bkalathil1337@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        //navigate to rentals
        liRentals.click();
        //add delivery only item to cart
        imageRounded.hover();
        buttonRounded.click();
        //navigate to cart
        linkShoppingCartButton.click();
        //click start date
        inputStartDate.click();
        //click november 30
        spanNovember.click();
        //check the TOS
        inputTermsCheckbox.click();
        // Assert that the button is disabled
        buttonCheckout.shouldBe(disabled);

        //check if the checkout failed and stayed on page
        webdriver().shouldHave(url("http://localhost:8080/shoppingCart"));



    }

    @Test
    public void TOSNotCheckedCheckout() throws InterruptedException {
        //Logs in
        inputEmail.sendKeys("bkalathil1337@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        //navigate to rentals
        liRentals.click();
        //add delivery only item to cart
        imageRounded.hover();
        buttonRounded.click();
        //navigate to cart
        linkShoppingCartButton.click();
        //click start date
        inputStartDate.click();
        //click november 30
        spanNovember.click();
        // Assert that the button is disabled
        buttonCheckout.shouldBe(disabled);
        //check if the checkout failed and stayed on page
        webdriver().shouldHave(url("http://localhost:8080/shoppingCart"));
    }
    
    @Test
    public void SuccessfulCartCheckout() throws InterruptedException {
        //Logs in
        inputEmail.sendKeys("bkalathil1337@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        //navigate to rentals
        liRentals.click();
        //add delivery only item to cart
        imageRounded.hover();
        buttonRounded.click();
        //navigate to cart
        linkShoppingCartButton.click();
        //click start date
        inputStartDate.click();
        //click november 30
        spanNovember.click();
        //check the TOS
        inputTermsCheckbox.click();
        //fill out address form
        inputStreet.sendKeys("6000 Jed Smith Dr");
        inputCity.sendKeys("Sacramento");
        inputState.sendKeys("CA");
        inputZip.sendKeys("95819");
        //calculate delivery fee
        //buttonCalculateDeliveryFee.click();
        //checkout
        buttonCalculateDeliveryFee.click();
        //prevent asynch functions
        Thread.sleep(10000);
        //checkout
        buttonCheckout.click();
        //wait for payment page to load
        Thread.sleep(5000);
        //check if the checkout succeeded and navigated to payment
        webdriver().shouldHave(urlContaining("checkout"));
        
    }
}
