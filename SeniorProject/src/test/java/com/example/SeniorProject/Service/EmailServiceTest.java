package com.example.SeniorProject.Service;

import static org.junit.jupiter.api.Assertions.*;
import com.example.SeniorProject.Email.EmailDetails;
import com.example.SeniorProject.Model.Account;
import com.example.SeniorProject.Model.Customer;
import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.AccountRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.mock;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EmailServiceTest {

        @Mock
        private JavaMailSender javaMailSender;

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private HttpServletRequest httpServletRequest;

        @Mock
        private FileSystemResource fileSystemResource;
        @Mock
        private S3Service s3Service;

        @Value("${spring.mail.username}")
        private String sender;

        @InjectMocks
        private EmailService emailService;
        @Mock
        private Order order; // Mock the Order object

        @Mock
        private Customer customer; // Mock the Customer object

        @Mock
        private EmailDetails emailDetails;
        @Mock
        private MimeMessage mimeMessage;  // Mock MimeMessage
        @Mock
        private MimeMessageHelper mimeMessageHelper;  // Mock MimeMessageHelper

        @Mock
        private File pdfFile;  // Mock PDF file
        @Mock
        private FileSystemResource fileResource;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                emailDetails = new EmailDetails();
                emailDetails.setRecipient("test@example.com");
                emailDetails.setSubject("Test Subject");
                emailDetails.setMessageBody("Test body message");
        }

        @Test
        void testSendSimpleEmail_Success() {
                // Arrange
                // Mock the behavior of the send method for SimpleMailMessage
                doNothing().when(javaMailSender).send(ArgumentMatchers.any(SimpleMailMessage.class));  // Correct way to mock with matchers

                // Act
                String result = emailService.sendSimpleEmail(emailDetails);

                // Assert
                assertEquals("Message sent successfully", result);
                // Verify that the send method was called with any SimpleMailMessage
                verify(javaMailSender, times(1)).send(ArgumentMatchers.any(SimpleMailMessage.class));  // Correct usage in verification
        }

        @Test
        void testSendSimpleEmail_Failure() {
                // Arrange
                doThrow(MailSendException.class).when(javaMailSender).send(ArgumentMatchers.any(SimpleMailMessage.class));

                // Act
                String result = emailService.sendSimpleEmail(emailDetails);

                // Assert
                assertEquals("Error occur while sending the message!!!", result);
                verify(javaMailSender, times(1)).send(ArgumentMatchers.any(SimpleMailMessage.class));
        }

        @Test
        void testSendSimpleEmail_AuthenticationFailed() {
                // Arrange: Mock the JavaMailSender to throw MailAuthenticationException
                doThrow(new MailAuthenticationException("Authentication failed")).when(javaMailSender).send(any(SimpleMailMessage.class));

                // Act: Call the method
                String result = emailService.sendSimpleEmail(emailDetails);

                // Assert: Check that the proper error message is returned
                assertEquals("authentication failed!!!", result);
        }

        @Test
        void testSendSimpleEmail_MailException() {
                // Arrange: Mock the JavaMailSender to throw a generic MailException
                doThrow(new MailException("Generic mail error") {}).when(javaMailSender).send(any(SimpleMailMessage.class));

                // Act: Call the method
                String result = emailService.sendSimpleEmail(emailDetails);

                // Assert: Check that an empty string is returned for MailException
                assertEquals("", result);
        }

        @Test
        void testSendEmailWithAttachment_Success() throws MessagingException {
                // Arrange
                String filePath = "path/to/attachment.txt";
                emailDetails.setAttachment(filePath);  // Set the file path for attachment

                // Mock MimeMessage and MimeMessageHelper
                MimeMessage mimeMessage = mock(MimeMessage.class);  // Mock MimeMessage
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);  // Mock helper for MimeMessage

                // Mock createMimeMessage to return the MimeMessage object
                when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

                emailService.setSender("test@example.com");
                String result = emailService.sendEmailWithAttachment(emailDetails);

                // Assert
                assertEquals("Message sent successfully", result);
                // Verify that the send method of javaMailSender was called with a single MimeMessage
                verify(javaMailSender, times(1)).send(ArgumentMatchers.any(MimeMessage.class));  // Use MimeMessage.class to target the single object method
        }

        @Test
        void testSendOrderInvoice_Successful() throws Exception {
                // Arrange
                String recipient = "customer@example.com";
                String subject = "Order Invoice";
                String messageBody = "Attached is your order invoice.";
                int orderId = 123;

                // Create the EmailDetails object
                EmailDetails emailDetails = new EmailDetails();
                emailDetails.setRecipient(recipient);
                emailDetails.setSubject(subject);
                emailDetails.setMessageBody(messageBody);

                when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
                MimeMessageHelper mimeMessageHelper = spy(new MimeMessageHelper(mimeMessage, true));

                File pdfFile = mock(File.class);
                FileSystemResource fileResource = mock(FileSystemResource.class);

                when(s3Service.downloadPdfFileFromS3Bucket("invoice_123.pdf")).thenReturn(pdfFile);
                when(pdfFile.getName()).thenReturn("invoice_123.pdf");
                when(fileResource.getFilename()).thenReturn("invoice_123.pdf");

                emailService.setSender("test@example.com");
                emailService.sendOrderInvoice(emailDetails, orderId);

                // Assert: Verify interactions with javaMailSender and the S3 service
                verify(javaMailSender, times(1)).send(mimeMessage); // Verifying email was sent
                verify(s3Service, times(1)).downloadPdfFileFromS3Bucket("invoice_123.pdf"); // Verify PDF download
                // Verify that addAttachment was called with the correct arguments on the spy
                //verify(mimeMessageHelper, times(1)).addAttachment(eq("invoice_123.pdf"), eq(fileResource));
        }

        @Test
        void testSendOrderInvoice_RuntimeException() throws Exception {
                // Arrange
                int orderId = 123;
                String fileName = "invoice_" + orderId + ".pdf";

                // Simulate an IOException when downloading the PDF
                when(s3Service.downloadPdfFileFromS3Bucket(fileName)).thenThrow(new IOException("Failed to download PDF"));

                // Act & Assert
                assertThrows(RuntimeException.class, () -> emailService.sendOrderInvoice(emailDetails, orderId),
                        "Expected RuntimeException to be thrown when IOException occurs");
        }

        @Test
        void testVerifyEmail_Success() {
                // Arrange
                String token = "someToken";
                String email = "test@example.com";
                emailService.getEmailMap().put(token, email);

                Account account = new Account();
                account.setEmail(email);
                account.setVerified(false);
                when(accountRepository.findAccountByEmail(email)).thenReturn(account);

                // Act
                boolean result = emailService.verifyEmail(token);

                // Assert
                assertTrue(result);
                assertTrue(account.isVerified());
                verify(accountRepository, times(1)).save(account);
        }

        @Test
        void testVerifyEmail_TokenNotFound() {
                // Arrange
                String token = "invalidToken";

                // Act
                boolean result = emailService.verifyEmail(token);

                // Assert
                assertFalse(result);
        }


        @Test
        void testSendVerificationEmail() {
                // Arrange
                String email = "test@example.com";
                String token = "generatedToken";
                when(httpServletRequest.getServerName()).thenReturn("localhost");
                when(httpServletRequest.getServerPort()).thenReturn(8080);

                // Mock send(SimpleMailMessage) instead of send(MimeMessage)
                doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

                // Act
                emailService.sendVerificationEmail(email, httpServletRequest);

                // Assert
                verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));  // Specify SimpleMailMessage.class
        }


        @Test
        void testSendCxPickupNotification() {
                // Arrange
                Order order = mock(Order.class);
                Customer customer = mock(Customer.class);

                // Mock the behavior of Order and Customer objects
                when(order.getCustomer()).thenReturn(customer);
                when(order.getId()).thenReturn(1);
                when(order.getCreationDate()).thenReturn(LocalDate.now());
                when(customer.getEmail()).thenReturn("test@example.com");
                when(customer.getFirstName()).thenReturn("John");
                when(customer.getLastName()).thenReturn("Doe");

                // Create the EmailDetails mock or mock the real behavior if needed
                EmailDetails emailDetails = new EmailDetails();
                emailDetails.setRecipient("test@example.com");
                emailDetails.setSubject("Wedding Rental Pickup Reminder");

                String emailBody = "Thank you for coming to us for your rental needs!\n"
                        + "Here is an important reminder for your rental pickup.\n"
                        + "John Doe your Order, 1 pickup is on, " + LocalDate.now();

                emailDetails.setMessageBody(emailBody);

                // Mock the sendSimpleEmail method to simulate sending an email
                doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));  // Mock send() to do nothing

                // Act
                emailService.sendCxPickupNotification(order);

                // Assert
                // Verify that the email is sent exactly once
                verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));  // Assuming you're using SimpleMailMessage
        }

        @Test
        void testSendCxCanceledNotification() {
                // Arrange
                String canceledReason = "Customer request";
                Order order = mock(Order.class);
                Customer customer = mock(Customer.class);

                // Mock the behavior of Order and Customer objects
                when(order.getCustomer()).thenReturn(customer);
                when(order.getId()).thenReturn(1);
                when(customer.getEmail()).thenReturn("test@example.com");
                when(customer.getFirstName()).thenReturn("John");
                when(customer.getLastName()).thenReturn("Doe");

                // Create the EmailDetails mock or mock the real behavior if needed
                EmailDetails emailDetails = new EmailDetails();
                emailDetails.setRecipient("test@example.com");
                emailDetails.setSubject("Important Wedding Rental Order Update");

                String emailBody = "John Doe, your Order 1 has been canceled for: " + canceledReason;
                emailDetails.setMessageBody(emailBody);

                // Mock the sendSimpleEmail method to simulate sending an email
                doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));  // Mock send() to do nothing

                // Act
                emailService.sendCxCanceledNotification(canceledReason, order);

                // Assert
                verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));  // Verifying that SimpleMailMessage is sent
        }

        @Test
        void testSendOrderConfirmation_Successful() {
                // Arrange: Simulate order with a customer
                when(order.getCustomer()).thenReturn(customer);
                when(customer.getEmail()).thenReturn("customer@example.com");

                // Spy on the emailService to mock only the sendOrderInvoice method
                EmailService spyEmailService = spy(emailService);

                // Mock the sendOrderInvoice method to ensure it's called
                doNothing().when(spyEmailService).sendOrderInvoice(any(EmailDetails.class), anyInt());  // Mock void method

                // Act: Call sendOrderConfirmation on the spy
                spyEmailService.sendOrderConfirmation(order);

                // Assert: Verify that sendOrderInvoice was called once
                verify(spyEmailService, times(1)).sendOrderInvoice(any(EmailDetails.class), anyInt());  // Verify call
        }

        @Test
        void testSendOrderConfirmation_WhenOrderHasNoCustomer() {
                // Arrange: Simulate order with no customer
                when(order.getCustomer()).thenReturn(null);

                // Act & Assert: Ensure IllegalArgumentException is thrown when the order has no customer
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                        emailService.sendOrderConfirmation(order);
                });

                // Assert: Verify the exception message
                assertEquals("Order has no associated customer. Cannot send order confirmation.", exception.getMessage());
        }



        @Test
        void testSendAdminNotification() {
                // Arrange
                String subject = "Order Notification";
                String messageBody = "New order received!";
                int orderId = 123;
                double orderPrice = 99.99;

                // Set up the mocked order and customer
                when(order.getId()).thenReturn(orderId);
                when(order.getPrice()).thenReturn(orderPrice);
                when(order.getCustomer()).thenReturn(customer);
                when(customer.getFirstName()).thenReturn("John");
                when(customer.getLastName()).thenReturn("Doe");

                // Spy on the emailService to mock only sendSimpleEmail method
                EmailService spyEmailService = spy(emailService);



                // Act: Call sendAdminNotification method
                spyEmailService.sendAdminNotification(subject, messageBody, order);

                // Assert: Verify sendSimpleEmail was called with correct EmailDetails
                verify(spyEmailService, times(1)).sendSimpleEmail(argThat(emailDetails ->
                        "zhijunli7799@gmail.com".equals(emailDetails.getRecipient()) &&
                                subject.equals(emailDetails.getSubject()) &&
                                emailDetails.getMessageBody().contains("Order ID: " + orderId) &&
                                emailDetails.getMessageBody().contains("Customer: John Doe") &&
                                emailDetails.getMessageBody().contains("Total Amount: $99.99")
                ));
        }

        @Test
        void testSendCustomerReturnNotification() {
                // Arrange
                String expectedSubject = "Order Returned Successfully";
                String expectedFirstName = "John";
                String expectedLastName = "Doe";
                String expectedEmail = "john.doe@example.com";
                int expectedOrderId = 123;
                String expectedEmailBody = "Dear John Doe,\n\nYour order with ID: 123 has been successfully returned.\n\nThank you for shopping with us!";

                // Set up the mocked order and customer
                when(order.getId()).thenReturn(expectedOrderId);
                when(order.getCustomer()).thenReturn(customer);
                when(customer.getFirstName()).thenReturn(expectedFirstName);
                when(customer.getLastName()).thenReturn(expectedLastName);
                when(customer.getEmail()).thenReturn(expectedEmail);

                // Spy on the emailService to mock only sendSimpleEmail method
                EmailService spyEmailService = spy(emailService);


                // Act: Call sendCustomerReturnNotification method
                spyEmailService.sendCustomerReturnNotification(order);

                // Assert: Verify sendSimpleEmail was called with correct EmailDetails
                verify(spyEmailService, times(1)).sendSimpleEmail(argThat(emailDetails ->
                        expectedEmail.equals(emailDetails.getRecipient()) &&
                                expectedSubject.equals(emailDetails.getSubject()) &&
                                expectedEmailBody.equals(emailDetails.getMessageBody())
                ));
        }


}