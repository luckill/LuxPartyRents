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


import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.url;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UploadPictureTest {

    private SelenideElement inputEmail = $("#email");
    private SelenideElement inputPassword = $("#password");
    private SelenideElement buttonSign = $("button[class$='btn-primary']");

    public SelenideElement liProducts = $("html > body > div:nth-of-type(2) > nav > div > div:nth-of-type(2) > ul > li:nth-of-type(5)");

    public SelenideElement buttonAdd = $("div[class$='justify-content-around'] button[type='button']");

    public SelenideElement inputName = $("input[name='name']");

    public SelenideElement inputQuantity = $("input[tabindex='2']");

    public SelenideElement inputPrice = $("input[name='price']");

    public SelenideElement inputType = $("input[name='type']");

    public SelenideElement textareaDescription = $("textarea[tabindex='5']");

    public SelenideElement buttonDeleteProduct = $("#deleteProductButton");

    public SelenideElement inputFile = $("input[aria-label*='for']");

    public SelenideElement buttonUpload = $("button[class$='btn-primary']");

    public SelenideElement divSuccess = $("div[class^='alert']");

    public SelenideElement divFileError = $("#fileError");
    
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
    public void SuccessfulJPGUpload() throws URISyntaxException {

        //Logs in as admin
        inputEmail.sendKeys("officialbkalathil@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        
        //navigate to products
        liProducts.click();

        buttonAdd.click();

        inputName.sendKeys("Test Product");

        inputQuantity.sendKeys("2");
        inputPrice.sendKeys("11.50");
        inputType.sendKeys("Dinnerware");
        textareaDescription.sendKeys("lol");
        buttonDeleteProduct.click();

        // Locate the file input element
        SelenideElement fileInput = $("input[type='file']");

        // Load the file as a classpath resource
        URL resource = getClass().getClassLoader().getResource("cart.jpg");
        File fileToUpload = new File(resource.toURI());

        // Upload the file
        fileInput.uploadFile(fileToUpload);

        inputFile.uploadFile(fileToUpload);

        buttonUpload.click();

        divSuccess.shouldBe(visible);
        
    }

    @Test
    public void PNGErrorUpload() throws URISyntaxException {

        //Logs in as admin
        inputEmail.sendKeys("officialbkalathil@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();

        //navigate to products
        liProducts.click();

        buttonAdd.click();

        inputName.sendKeys("Test Product");

        inputQuantity.sendKeys("2");
        inputPrice.sendKeys("11.50");
        inputType.sendKeys("Dinnerware");
        textareaDescription.sendKeys("lol");
        buttonDeleteProduct.click();

        // Locate the file input element (change the selector to match your input element)
        SelenideElement fileInput = $("input[type='file']");

        // Load the file as a classpath resource
        URL resource = getClass().getClassLoader().getResource("cart.png");
        File fileToUpload = new File(resource.toURI());

        // Upload the file
        fileInput.uploadFile(fileToUpload);

        inputFile.uploadFile(fileToUpload);

        buttonUpload.click();

        divFileError.shouldBe(visible);

    }
}
