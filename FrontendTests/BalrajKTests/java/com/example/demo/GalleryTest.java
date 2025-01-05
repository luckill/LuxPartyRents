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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GalleryTest {

    private SelenideElement inputEmail = $("#email");
    private SelenideElement inputPassword = $("#password");
    private SelenideElement buttonSign = $("button[class$='btn-primary']");

    public SelenideElement liGallery = $("html > body > div:nth-of-type(2) > nav > div > div:nth-of-type(2) > ul > li:nth-of-type(3)");

    public SelenideElement imageGallery = $("html > body > section > img:nth-of-type(1)");

    public SelenideElement imageExpanded = $("#expandedImage");

    public SelenideElement imageGallery2 = $("html > body > section > img:nth-of-type(1)");


    
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
    public void SuccessfulImageEnlargement() {
        //Logs in
        inputEmail.sendKeys("bkalathil1337@gmail.com");
        inputPassword.sendKeys("Password1!");
        buttonSign.click();
        //navigate to gallery
        liGallery.click();
        
        //click on first image
        imageGallery.click();

        // Extract section number from imageGallery and imageExpanded
        String gallerySectionNumber = extractSectionNumber(imageGallery);
        String expandedSectionNumber = extractSectionNumber(imageExpanded);

        // Assert that both section numbers are the same
        assertEquals(gallerySectionNumber, expandedSectionNumber, "Section numbers do not match!");
        
        
        
        
    }

    private String extractSectionNumber(SelenideElement element) {
        // Example method to extract section number, customize as per your HTML structure
        String src = element.getAttribute("src");
        // Assuming section number is part of the src attribute, like: /images/section1/image.jpg
        return src.replaceAll(".*/section(\\d+).*", "$1");
    }
}
