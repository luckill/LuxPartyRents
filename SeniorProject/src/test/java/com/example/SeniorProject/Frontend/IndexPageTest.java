package com.example.SeniorProject.Frontend;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.chrome.ChromeOptions;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static org.junit.jupiter.api.Assertions.*;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.url;

// Index Page Testing
/*
 *   Not Logged In:
 *   - Edit button should not be visible
 *   - Test if featured item is selected, you get rerouted to rentals page
 *
 *   Logged In (Not Admin):
 *   - Edit button should not be visible
 *   - Test if featured item is selected, you get rerouted to rentals page
 *
 *   Logged In (Admin):
 *   - Edit button should be visible
 *   - If Edit mode isn't clicked
 *       - Test if featured item is selected, you get rerouted to rentals page
 *
 *   Featured Items (Admin):
 *   - Pressing Edit button / Edit Mode
 *       - Edit button now says "Done"
 *       - Edit Mode Interface shows
 *           - Open spots will have an empty frame
 *       - Pressing on a frame will show a modal with a list of items
 *       - Pressing on a white frame will add to the featured items locally
 *       - Pressing on a gray frame will remove the selected featured item locally
 *       - Pressing "Done" button will save any changes and exit Edit Mode (Button now says "Edit")
 *   - Pressing Done button / Exiting Edit Mode
 *       - Edit button now says "Edit"
 *       - Edit Mode Interface disappears
 *           - Empty Frames will disappear (if any)
 */

public class IndexPageTest {

    // Login Variables
    private SelenideElement inputEmail = $("#email");
    private SelenideElement inputPassword = $("#password");
    private SelenideElement loginButton = $("#loginButton");
    private SelenideElement loginConfirmButton = $("button[class$='btn-primary']");
    private SelenideElement welcomeUser = $("#user-greeting");
    // Featured Items Variables
    private SelenideElement editButton = $("#edit-button");

    private MainPage mainPage;

    @BeforeAll
    public static void setUpAll() {
        Configuration.browserSize = "1280x800";
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        Configuration.browserCapabilities = options;
        // Opens a new page
        Selenide.open("http://localhost:8080");
        mainPage = new MainPage();
    }

    @AfterEach
    public void tearDown()
    {
        Selenide.closeWebDriver();
    }

    // Reusable Functions
    public void loginUser(String email, String password) {
        // Click login button
        loginButton.click();
        webdriver().shouldHave(url("http://localhost:8080/login"));

        // Login
        $("#email").setValue(email);
        $("#password").setValue(password);
        loginConfirmButton.scrollTo().click();

        // Check login was successful
        webdriver().shouldHave(url("http://localhost:8080/"));
        welcomeUser.shouldBe(visible);
    }

    // Make sure all featured items (Not in Edit Mode) direct you to the rental page
    public void testAllFeaturedItemsNoEditMode() {
        ElementsCollection featuredItems = $$("[id^='item-card-clone']");

        for (int i = 0; i < featuredItems.size(); i++) {
            // Re-fetch element to ensure working with current version since DOM always changes
            featuredItems = $$("[id^='item-card-clone']");
            SelenideElement item = featuredItems.get(i);

            // Make sure item is visible
            item.shouldBe(visible);

            // Click item
            item.click();

            // Verify the URL redirects correctly
            webdriver().shouldHave(url("http://localhost:8080/rental"));

            // Navigate back to the index page
            Selenide.open("http://localhost:8080");
        }
    }

    // Test that each card opens up list of items to change
    public void testAllFeaturedItemsInEditMode() {
        ElementsCollection featuredItems = $$("#featured-items-container > div").filter(visible);
        System.out.println(featuredItems.size());

        for (int i = 0; i < featuredItems.size(); i++) {
            // Re-fetch element to ensure working with current version since DOM always changes
            featuredItems = $$("#featured-items-container > div").filter(visible);
            SelenideElement item = featuredItems.get(i);

            // Make sure item is visible
            item.shouldBe(visible);

            // Click item
            try {
                item.scrollTo().click();
            } catch (Exception e) {
                System.out.println("Standard click failed, using JavaScript for item: " + item.getAttribute("id"));
                executeJavaScript("arguments[0].click();", item);
            }

            // Assert that the modal opened up
            SelenideElement itemListModal = $("div[class$='modal-dialog-scrollable']");
            SelenideElement itemListModalCloseButton = $("button[aria-label='Close']");
            itemListModal.shouldBe(visible);
            itemListModalCloseButton.shouldBe(visible).click();
            // Gives some time between clicking buttons
            itemListModal.shouldNotBe(visible, Duration.ofSeconds(10));
            // Sometimes modals come with a modal backdrop. Check for this too
            $(".modal-backdrop").shouldNot(exist);
        }
        System.out.println("finished");
    }

    // Checks for visibility of edit button
    public void checkEditButtonVisible(Boolean isAdmin) {
        // If not an Admin then edit button shouldn't be visible
        if (!isAdmin) {
            editButton.shouldNotBe(visible);
            return;
        }

        // Is an Admin
        editButton.shouldBe(visible);
        editButton.shouldHave(text("Edit"));
    }

    public void enterEditMode() {
        loginUser("brandonkue12@gmail.com", "Password123!");
        editButton.click();
        editButton.shouldHave(text("Done"));
    }

    // (Guest) On page load, welcome message and Edit Button is invisible. All featured items should send to rentals
    @Test
    public void testInitialLoad() {
       // This will assume that there was no previous login
       // Edit button and welcome shouldn't be invisible
       checkEditButtonVisible(false);
       welcomeUser.shouldNotBe(visible);

       testAllFeaturedItemsNoEditMode();
    }

    // (User) Test login for a user who is not an Admin. No Edit button and all featured items direct to rentals
    @Test
    public void testLoginUser() {
        // Login
        loginUser("brandonkue12@gmail.com", "Password123!");

        // Check for edit button invisible
        checkEditButtonVisible(false);
        welcomeUser.shouldBe(visible);

        testAllFeaturedItemsNoEditMode();
    }

    // (Admin) Test login for an Admin. Only tests for Edit button being visible.
    @Test
    public void testLoginAdmin() {
        // Login
        loginUser("brandonkue12@gmail.com", "Password123!");

        // Check for edit button visible
        checkEditButtonVisible(true);
        welcomeUser.shouldBe(visible);
    }

    // Test Featured Items Functions (Admin only, assume admin login)

    // Check when toggle Edit Mode button
    @Test
    public void testEditModeButtonWorks() {
        loginUser("brandonkue12@gmail.com", "Password123!");
        checkEditButtonVisible(true);

        // Check that edit button now says done
        editButton.click();
        editButton.shouldHave(text("Done"));

        // Click again and check it says "Edit"
        editButton.click();
        editButton.shouldHave(text("Edit"));
    }

    // When toggle Edit Mode on and less than 6 items, there should be empty cards that fill to 6
    @Test
    public void testEmptyCardsInstantiate() {
        // Enter Edit Mode
        enterEditMode();

        // Get Items
        ElementsCollection featuredItems = $$("[id^='item-card-clone']");
        ElementsCollection emptyCards = $$("#empty-card").filter(visible);

        assertEquals(6 - featuredItems.size(), emptyCards.size(), "Mismatch in the number of visible empty cards.");
        System.out.println("ran");
        testAllFeaturedItemsInEditMode();
    }

    // When toggle Edit Mode on and there is 6 items, no empty cards
    @Test
    public void testNoEmptyCards() {
        enterEditMode();

        // Get Items
        ElementsCollection featuredItems = $$("[id^='item-card-clone']");
        ElementsCollection emptyCards = $$("#empty-card").filter(visible);

        // Assert that there are 6 featured items and no empty cards
        assertEquals(6, featuredItems.size(), "There aren't 6 featured items.");
        assertEquals(0, emptyCards.size(), "There are empty cards.");

        testAllFeaturedItemsInEditMode();
    }

    // Test when trying to add an item
    @Test
    public void addFeaturedItem() {
        enterEditMode();

        // Select only visible elements
        SelenideElement featuredItem = $$("#featured-items-container > div").filter(visible).first();

        // Ensure the element is visible
        featuredItem.shouldBe(visible);

        try {
            featuredItem.scrollTo().click();
        } catch (Exception e) {
            System.out.println("Standard click failed, using JavaScript for item: " + featuredItem.getAttribute("id"));
            executeJavaScript("arguments[0].click();", featuredItem);
        }

        // Assert that the modal opened up
        SelenideElement itemListModal = $("div[class$='modal-dialog-scrollable']");
        itemListModal.shouldBe(visible);

        // Add an item
        SelenideElement itemToAdd = $("html > body > div:nth-of-type(3) > div > div:nth-of-type(3) > div > div > div:nth-of-type(2) > div:nth-of-type(2) > div:nth-of-type(3)");
        itemToAdd.shouldBe(visible); // Ensure the item is loaded and visible
        String itemText = itemToAdd.getText().trim();
        System.out.println("Item to add: " + itemText);

        // Click the item to add it
        itemToAdd.click();

        // Make sure modal closes
        itemListModal.shouldNotBe(visible, Duration.ofSeconds(10));
        $(".modal-backdrop").shouldNot(exist);

        // Verify the item is reflected in the featured items
        ElementsCollection featuredItems = $$("#featured-items-container > div").filter(visible);
        System.out.println("Second featured item: " + featuredItems.get(1).getAttribute("outerHTML"));
        featuredItems.get(1).shouldHave(text(itemText));
    }

    // Test when trying to remove an item
    @Test
    public void removeFeaturedItem() {
        enterEditMode();

        // Select only visible elements
        SelenideElement featuredItem = $$("#featured-items-container > div").filter(visible).first();

        // Ensure the item is visible
        featuredItem.shouldBe(visible);

        // Capture the item's text for validation later
        String itemText = featuredItem.getText().trim();
        System.out.println("Item to remove: " + itemText);

        try {
            // Click the item to remove it
            featuredItem.scrollTo().click();
        } catch (Exception e) {
            System.out.println("Standard click failed, using JavaScript for item: " + featuredItem.getAttribute("id"));
            executeJavaScript("arguments[0].click();", featuredItem);
        }

        // Validate that the item has been removed
        ElementsCollection updatedFeaturedItems = $$("#featured-items-container > div").filter(visible);
        System.out.println("Updated featured items: ");
        updatedFeaturedItems.forEach(item -> System.out.println(item.getText()));

        // Assert the removed item is not present in the list
        updatedFeaturedItems.forEach(item -> {
            assertNotEquals(itemText, item.getText(), "Removed item is still present in the featured items!");
        });
    }

    // Test when trying to add more than 6 items
    @Test
    public void testFeaturedItemMax() {
        enterEditMode();

        // Get featured items
        ElementsCollection featuredItems = $$("#featured-items-container > div").filter(visible);
        assertEquals(6, featuredItems.size(), "There should be exactly 6 featured items before testing max.");

        try {
            featuredItems.first().scrollTo().click();
        } catch (Exception e) {
            System.out.println("Standard click failed, using JavaScript for item: " + featuredItems.first().getAttribute("id"));
            executeJavaScript("arguments[0].click();", featuredItems.first());
        }

        // Assert that the modal opened up
        SelenideElement itemListModal = $("div[class$='modal-dialog-scrollable']");
        itemListModal.shouldBe(visible);

        // Add an item
        SelenideElement itemToAdd = $("html > body > div:nth-of-type(3) > div > div:nth-of-type(3) > div > div > div:nth-of-type(2) > div:nth-of-type(2) > div:nth-of-type(7)");
        itemToAdd.shouldBe(visible); // Ensure the item is loaded and visible

        // Click the item to add it
        itemToAdd.click();

        // Validate the alert
        try {
            String alertText = Selenide.switchTo().alert().getText();
            System.out.println("Alert appeared with message: " + alertText);
            assertEquals("You have 6 items already!", alertText, "Alert message did not match!");

            // Accept the alert
            Selenide.switchTo().alert().accept();
        } catch (NoAlertPresentException e) {
            fail("Expected an alert to appear, but none was present.");
        }

        itemListModal.shouldBe(visible, Duration.ofSeconds(10));

    }

    // Test when trying to have less than 1 featured item
    @Test
    public void testFeaturedItemMin() {
        enterEditMode();

        // Get featured items
        ElementsCollection featuredItems = $$("#featured-items-container > div").filter(visible);

        try {
            featuredItems.first().scrollTo().click();
        } catch (Exception e) {
            System.out.println("Standard click failed, using JavaScript for item: " + featuredItems.first().getAttribute("id"));
            executeJavaScript("arguments[0].click();", featuredItems.first());
        }

        // Assert that the modal opened up
        SelenideElement itemListModal = $("div[class$='modal-dialog-scrollable']");
        itemListModal.shouldBe(visible);

        // Remove an item
        SelenideElement itemToRemove = $("div[class='card mb-3 text-white']");
        itemToRemove.shouldBe(visible); // Ensure the item is loaded and visible

        // Click the item to add it
        itemToRemove.click();

        // Validate the alert
        try {
            String alertText = Selenide.switchTo().alert().getText();
            System.out.println("Alert appeared with message: " + alertText);
            assertEquals("You must have at least 1 item!", alertText, "Alert message did not match!");

            // Accept the alert
            Selenide.switchTo().alert().accept();
        } catch (NoAlertPresentException e) {
            fail("Expected an alert to appear, but none was present.");
        }

        itemListModal.shouldBe(visible, Duration.ofSeconds(10));
    }

    // Test save only when done button is pressed
    @Test
    public void testSaveChanges() {
        enterEditMode();

        // Select only visible elements
        SelenideElement featuredItem = $$("#featured-items-container > div").filter(visible).first();

        // Ensure the element is visible
        featuredItem.shouldBe(visible);

        try {
            featuredItem.scrollTo().click();
        } catch (Exception e) {
            System.out.println("Standard click failed, using JavaScript for item: " + featuredItem.getAttribute("id"));
            executeJavaScript("arguments[0].click();", featuredItem);
        }

        // Assert that the modal opened up
        SelenideElement itemListModal = $("div[class$='modal-dialog-scrollable']");
        itemListModal.shouldBe(visible);

        // Add an item
        SelenideElement itemToAdd = $("        html > body > div:nth-of-type(3) > div > div:nth-of-type(3) > div > div > div:nth-of-type(2) > div:nth-of-type(2) > div:nth-of-type(2)\n");
        itemToAdd.shouldBe(visible); // Ensure the item is loaded and visible
        String itemText = itemToAdd.getText().trim();
        System.out.println("Item to add: " + itemText);

        // Click the item to add it
        itemToAdd.click();

        // Make sure modal closes
        itemListModal.shouldNotBe(visible, Duration.ofSeconds(10));
        $(".modal-backdrop").shouldNot(exist);

        // Click Done button
        editButton.shouldHave(text("Done"));
        editButton.click();

        // Verify the item is reflected in the featured items
        ElementsCollection featuredItems = $$("#featured-items-container > div").filter(visible);
        System.out.println("Second featured item: " + featuredItems.get(1).getAttribute("outerHTML"));
        featuredItems.get(1).shouldHave(text(itemText));
    }

}

