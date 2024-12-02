package com.example.demo;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.demo.MainPage;
import org.openqa.selenium.chrome.ChromeOptions;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;


import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.url;

public class MainPageTest {
    MainPage mainPage = new MainPage();

    SelenideElement userGreeting = $("#user-greeting");
    private SelenideElement inputEmail = $("#email");
    private SelenideElement inputPassword = $("#password");
    private SelenideElement buttonSign = $("button[class$='btn-primary']");
    private SelenideElement divError = $("#error");
    private SelenideElement successMessage = $("#successMessage");

    public SelenideElement inputPassword2 = $("#password");

    public SelenideElement buttonLogin = $("button[class$='btn-primary']");


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
    public void TestSuccessfulLogin() {

        //inputEmail2.sendKeys("hello");

        $("input[id='email']").sendKeys("officialbkalathil@gmail.com");
        $("input[id='password']").sendKeys("MinerRaj77&");
        $("button[class$='btn-primary']").click();

        webdriver().shouldHave(url("http://localhost:8080/"));
        $("span[id='firstNameDisplay']").shouldBe(visible).shouldHave(text("lol"));
        //serGreeting.shouldBe(visible).shouldHave(text("Welcome, lol!"));

    }
}
/*
    @Test
    public void search() {
        mainPage.searchButton.click();

        $("[data-test='search-input']").sendKeys("Selenium");
        $("button[data-test='full-search-button']").click();

        $("input[data-test='search-input']").shouldHave(attribute("value", "Selenium"));
    }

    @Test
    public void toolsMenu() {
        mainPage.toolsMenu.click();

        $("div[data-test='main-submenu']").shouldBe(visible);
    }

    @Test
    public void navigationToAllTools() {
        mainPage.seeDeveloperToolsButton.click();
        mainPage.findYourToolsButton.click();

        $("#products-page").shouldBe(visible);

        assertEquals("All Developer Tools and Products by JetBrains", Selenide.title());
    }

    private void assertEquals(String s, String title) {
    }
}
*/