package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.Customer;
import com.example.SeniorProject.Model.OrderRepository;
import com.example.SeniorProject.Model.OrderStatus;
import com.example.SeniorProject.Model.Account;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.RefundCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.MockedStatic;
import com.stripe.exception.StripeException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

        @Mock
        private OrderRepository orderRepository;

        @InjectMocks
        private PaymentService paymentService;

        @Mock
        private EmailService emailService;

        private Order order;
        private Customer customer;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                // Create a sample order for testing
                Stripe.apiKey = "test_api_key";
                order = new Order();
                order.setId(1);
                order.setPrice(100.0);
                order.setPaid(false);
                order.setStatus(OrderStatus.RECEIVED);
                order.setPaymentReference("pi_1234567890");

                Account account = new Account("john.doe@example.com", "password123", false);


                // Create Customer
                customer = new Customer("John", "Doe", "john.doe@example.com", "1234567890");
                customer.setAccount(account); // Set account for the customer

                // Create an Order for the Customer
                order = new Order();
                order.setId(1);
                order.setPaid(false);
                order.setStatus(OrderStatus.RECEIVED);
                order.setCustomer(customer);

                // Set up the mock behavior for orderRepository
                when(orderRepository.getOrderById(1)).thenReturn(order);
        }

        @Test
        void testPayAll_success() throws Exception {
                // Arrange
                Order order = new Order();
                order.setId(1);
                order.setPrice(100.0);
                order.setPaymentReference("");  // Initially empty reference

                when(orderRepository.getOrderById(1)).thenReturn(order);  // Mock the order repository

                // Create a mocked PaymentIntent
                PaymentIntent paymentIntentMock = mock(PaymentIntent.class);
                when(paymentIntentMock.getClientSecret()).thenReturn("test_client_secret");
                when(paymentIntentMock.getId()).thenReturn("test_payment_intent_id");
                when(paymentIntentMock.getAmount()).thenReturn(10000L);  // Amount in cents
                when(paymentIntentMock.getCurrency()).thenReturn("usd");
                when(paymentIntentMock.getStatus()).thenReturn("requires_payment_method");

                // Mock static method PaymentIntent.create
                try (MockedStatic<PaymentIntent> mockedStatic = Mockito.mockStatic(PaymentIntent.class)) {
                        mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                                .thenReturn(paymentIntentMock);  // Return the mocked PaymentIntent

                        // Act
                        Map<String, Object> response = paymentService.payAll(1);

                        // Assert
                        assertNotNull(response);
                        assertEquals("test_client_secret", response.get("clientSecret"));
                        assertEquals("https://dashboard.stripe.com/settings/payment_methods/review?transaction_id=test_payment_intent_id", response.get("transactionId"));
                        assertEquals(10000L, response.get("amount"));
                        assertEquals("usd", response.get("currency"));
                        assertEquals("requires_payment_method", response.get("status"));

                        // Verify the order status is updated
                        verify(orderRepository, times(1)).save(order);
                        assertEquals(OrderStatus.RECEIVED, order.getStatus());
                }
        }

        @Test
        void testPayAll_StripeException() throws Exception {
                // Arrange
                int orderId = 1;
                Order order = new Order();
                order.setId(orderId);
                order.setSubtotal(100.0);  // Set the subtotal for the order
                order.setPaymentReference("payment_reference_123");  // Set mock payment reference

                // Mock the order repository to return the mocked order when getOrderById is called
                when(orderRepository.getOrderById(orderId)).thenReturn(order);

                // Simulate StripeException
                StripeException stripeException = mock(StripeException.class);
                when(stripeException.getMessage()).thenReturn("Payment processing failed");

                // Mock PaymentIntent.create() to throw the StripeException
                try (MockedStatic<PaymentIntent> paymentIntentMockedStatic = Mockito.mockStatic(PaymentIntent.class)) {
                        paymentIntentMockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                                .thenThrow(stripeException);

                        // Act & Assert: Call the method and expect an exception
                        Exception exception = assertThrows(Exception.class, () -> {
                                paymentService.payAll(orderId);
                        });

                        // Get the actual message and compare
                        String actualMessage = exception.getMessage();
                        String expectedMessage = "Payment processing failed: Payment processing failed";
                        assertTrue(actualMessage.contains(expectedMessage), "Expected message to contain: " + expectedMessage + ", but got: " + actualMessage);
                }
        }


        @Test
        void testPayAll_orderNotFound() throws Exception {
                // Arrange
                when(orderRepository.getOrderById(1)).thenReturn(null);

                // Act & Assert
                Exception exception = assertThrows(Exception.class, () -> {
                        paymentService.payAll(1);
                });
                assertEquals("Order not found with id: 1", exception.getMessage());
        }

        @Test
        void testRefund_success() throws Exception {
                // Arrange
                // Set up the order mock with correct values
                Order order = new Order();
                order.setId(1);
                order.setPrice(100.0);
                order.setPaymentReference("payment_reference_123");  // Set a mock payment reference

                // Mock the order repository to return the order when getOrderById is called
                when(orderRepository.getOrderById(1)).thenReturn(order);

                long amountInCents = 10000L; // 100.0 USD

                // Mock Refund object
                Refund refundMock = mock(Refund.class);
                when(refundMock.getId()).thenReturn("refund_123456");
                when(refundMock.getAmount()).thenReturn(amountInCents);
                when(refundMock.getCurrency()).thenReturn("usd");
                when(refundMock.getStatus()).thenReturn("succeeded");
                when(refundMock.getCreated()).thenReturn(1627543254L); // Example timestamp

                // Create the refundParams
                RefundCreateParams refundParams = RefundCreateParams.builder()
                        .setPaymentIntent(order.getPaymentReference()) // Set the payment reference from the mocked order
                        .setAmount(amountInCents)
                        .build();

                // Mock the Refund.create() static call to return the mock Refund
                try (MockedStatic<Refund> refundMockedStatic = Mockito.mockStatic(Refund.class);
                     MockedStatic<Stripe> stripeMockedStatic = Mockito.mockStatic(Stripe.class)) {
                        refundMockedStatic.when(() -> Refund.create(any(RefundCreateParams.class)))
                                .thenReturn(refundMock);

                        // Act: Call the refund method
                        Map<String, Object> response = paymentService.refund(1, 100.0);

                        // Assert the refund response is as expected
                        assertNotNull(response);
                        assertEquals("refund_123456", response.get("id"));
                        assertEquals(amountInCents, response.get("amount"));
                        assertEquals("usd", response.get("currency"));
                        assertEquals("succeeded", response.get("status"));
                }
        }

        @Test
        void testRefund_failed() throws Exception {
                // Arrange
                Order order = new Order();
                order.setId(1);
                order.setPrice(100.0);
                order.setPaymentReference("payment_reference_123");  // Set mock payment reference

                when(orderRepository.getOrderById(1)).thenReturn(order);
                long amountInCents = 10000L; // 100.0 USD

                RefundCreateParams refundParams = RefundCreateParams.builder()
                        .setPaymentIntent(order.getPaymentReference())
                        .setAmount(amountInCents)
                        .build();

                // Simulate a Stripe exception
                StripeException stripeException = mock(StripeException.class);
                when(stripeException.getMessage()).thenReturn("Refund failed");


                try (MockedStatic<Refund> refundMockedStatic = Mockito.mockStatic(Refund.class);
                     MockedStatic<Stripe> stripeMockedStatic = Mockito.mockStatic(Stripe.class)) {
                        refundMockedStatic.when(() -> Refund.create(any(RefundCreateParams.class)))
                                .thenThrow(stripeException);

                        // Act & Assert
                        Exception exception = assertThrows(Exception.class, () -> {
                                paymentService.refund(1, 100.0);
                        });

                        // Get the actual message and compare
                        String actualMessage = exception.getMessage();
                        String expectedMessage = "Payment processing failed";
                        assertTrue(actualMessage.contains(expectedMessage), "Expected message to contain: " + expectedMessage + ", but got: " + actualMessage);
                }
        }

        @Test
        void testRefundAll_Success() throws Exception {
                // Arrange: Set up the order mock with correct values
                Order order = new Order();
                order.setId(1);
                order.setPrice(100.0);
                order.setPaymentReference("payment_reference_123"); // Set a mock payment reference

                // Mock the order repository to return the order when getOrderById is called
                when(orderRepository.getOrderById(1)).thenReturn(order);

                long amountInCents = 10000L; // 100.0 USD

                // Mock Refund object
                Refund refundMock = mock(Refund.class);
                when(refundMock.getId()).thenReturn("refund_123456");
                when(refundMock.getAmount()).thenReturn(amountInCents);
                when(refundMock.getCurrency()).thenReturn("usd");
                when(refundMock.getStatus()).thenReturn("succeeded");
                when(refundMock.getCreated()).thenReturn(1627543254L); // Example timestamp

                // Create the refundParams
                RefundCreateParams refundParams = RefundCreateParams.builder()
                        .setPaymentIntent(order.getPaymentReference()) // Set the payment reference from the mocked order
                        .setAmount(amountInCents)
                        .build();

                // Mock the Refund.create() static call to return the mock Refund
                try (MockedStatic<Refund> refundMockedStatic = Mockito.mockStatic(Refund.class)) {
                        refundMockedStatic.when(() -> Refund.create(any(RefundCreateParams.class)))
                                .thenReturn(refundMock);

                        // Act: Call the refundAll method
                        Map<String, Object> response = paymentService.refundAll(1);

                        // Assert: Verify the refund response is as expected
                        assertNotNull(response);
                        assertEquals("refund_123456", response.get("id"));
                        assertEquals(amountInCents, response.get("amount"));
                        assertEquals("usd", response.get("currency"));
                        assertEquals("succeeded", response.get("status"));
                        assertEquals(1627543254L, response.get("created"));
                }
        }

        @Test
        void testRefundAll_StripeException() throws Exception {
                // Arrange: Set up the order mock with correct values
                int orderId = 1;
                Order order = mock(Order.class);
                order.setId(orderId);
                order.setPrice(100.0);
                order.setPaymentReference("payment_intent_id"); // Set a mock payment reference

                // Mock the order repository to return the mocked order when getOrderById is called
                when(orderRepository.getOrderById(orderId)).thenReturn(order);

                StripeException stripeException = mock(StripeException.class);
                when(stripeException.getMessage()).thenReturn("Stripe error");

                // Mock Stripe to throw a StripeAPIException (a concrete subclass of StripeException)
                try (MockedStatic<Refund> refundMockedStatic = Mockito.mockStatic(Refund.class)) {
                        refundMockedStatic.when(() -> Refund.create(any(RefundCreateParams.class)))
                                .thenThrow(stripeException);

                        // Act & Assert: Call the method and expect an exception
                        Exception exception = assertThrows(Exception.class, () -> {
                                paymentService.refundAll(orderId);
                        });

                        // Assert: Verify that the exception message matches the expected output
                        assertEquals("Payment processing failed: Stripe error", exception.getMessage());
                }
        }

        @Test
        void testRefundAllExceptDeposit_Success() throws Exception {
                // Arrange: Set up the order mock with correct values
                Order order = new Order();
                order.setId(1);
                order.setSubtotal(150.0);  // Set the subtotal
                order.setDeposit(50.0);    // Set the deposit
                order.setPaymentReference("payment_reference_123"); // Set a mock payment reference

                // Mock the order repository to return the order when getOrderById is called
                when(orderRepository.getOrderById(1)).thenReturn(order);

                long amountInCents = 10000L; // 100.0 USD (refund amount: subtotal - deposit)

                // Mock Refund object
                Refund refundMock = mock(Refund.class);
                when(refundMock.getId()).thenReturn("refund_123456");
                when(refundMock.getAmount()).thenReturn(amountInCents);
                when(refundMock.getCurrency()).thenReturn("usd");
                when(refundMock.getStatus()).thenReturn("succeeded");
                when(refundMock.getCreated()).thenReturn(1627543254L); // Example timestamp

                // Create the refundParams
                RefundCreateParams refundParams = RefundCreateParams.builder()
                        .setPaymentIntent(order.getPaymentReference()) // Set the payment reference from the mocked order
                        .setAmount(amountInCents) // Refund amount
                        .build();

                // Mock the Refund.create() static call to return the mock Refund
                try (MockedStatic<Refund> refundMockedStatic = Mockito.mockStatic(Refund.class)) {
                        refundMockedStatic.when(() -> Refund.create(any(RefundCreateParams.class)))
                                .thenReturn(refundMock);

                        // Act: Call the refundAllExceptDeposit method
                        Map<String, Object> response = paymentService.refundAllExceptDeposit(1);

                        // Assert: Verify the refund response is as expected
                        assertNotNull(response);
                        assertEquals("refund_123456", response.get("id"));
                        assertEquals(amountInCents, response.get("amount"));
                        assertEquals("usd", response.get("currency"));
                        assertEquals("succeeded", response.get("status"));
                        assertEquals(1627543254L, response.get("created"));

                        // Also, verify that the order's status was updated to REFUNDED
                        assertEquals(OrderStatus.REFUNDED, order.getStatus());
                        verify(orderRepository).save(order); // Ensure that save was called to update the order status
                }
        }

        @Test
        void testRefundAllExceptDeposit_StripeException() throws Exception {
                // Arrange: Set up the order mock with correct values
                int orderId = 1;
                Order order = mock(Order.class);
                order.setId(orderId);
                order.setSubtotal(150.0);  // Set the subtotal
                order.setDeposit(50.0);    // Set the deposit
                order.setPaymentReference("payment_reference_123"); // Set a mock payment reference

                // Mock the order repository to return the mocked order when getOrderById is called
                when(orderRepository.getOrderById(orderId)).thenReturn(order);

                // Mock StripeException to simulate the error
                StripeException stripeException = mock(StripeException.class);
                when(stripeException.getMessage()).thenReturn("Stripe error");

                // Mock Refund.create() to throw the StripeException
                try (MockedStatic<Refund> refundMockedStatic = Mockito.mockStatic(Refund.class)) {
                        refundMockedStatic.when(() -> Refund.create(any(RefundCreateParams.class)))
                                .thenThrow(stripeException);

                        // Act & Assert: Call the method and expect an exception
                        Exception exception = assertThrows(Exception.class, () -> {
                                paymentService.refundAllExceptDeposit(orderId);
                        });

                        // Assert: Verify that the exception message matches the expected output
                        assertEquals("Payment processing failed: Stripe error", exception.getMessage());
                }
        }

        @Test
        void testRefundDeposit_Success() throws Exception {
                // Arrange: Set up the order mock with correct values
                Order order = new Order();
                order.setId(2);
                order.setPrice(50.0);
                order.setPaymentReference("payment_reference_456"); // Set a mock payment reference

                // Mock the order repository to return the order when getOrderById is called
                when(orderRepository.getOrderById(2)).thenReturn(order);

                long amountInCents = 5000L; // 50.0 USD deposit

                // Mock Refund object
                Refund refundMock = mock(Refund.class);
                when(refundMock.getId()).thenReturn("refund_789012");
                when(refundMock.getAmount()).thenReturn(amountInCents);
                when(refundMock.getCurrency()).thenReturn("usd");
                when(refundMock.getStatus()).thenReturn("succeeded");
                when(refundMock.getCreated()).thenReturn(1627543260L); // Example timestamp

                // Create the refundParams
                RefundCreateParams refundParams = RefundCreateParams.builder()
                        .setPaymentIntent(order.getPaymentReference()) // Set the payment reference from the mocked order
                        .setAmount(amountInCents)
                        .build();

                // Mock the Refund.create() static call to return the mock Refund
                try (MockedStatic<Refund> refundMockedStatic = Mockito.mockStatic(Refund.class)) {
                        refundMockedStatic.when(() -> Refund.create(any(RefundCreateParams.class)))
                                .thenReturn(refundMock);

                        // Act: Call the refundDeposit method
                        Map<String, Object> response = paymentService.refundDeposit(2);

                        // Assert: Verify the refund response is as expected
                        assertNotNull(response);
                        assertEquals("refund_789012", response.get("id"));
                        assertEquals(amountInCents, response.get("amount"));
                        assertEquals("usd", response.get("currency"));
                        assertEquals("succeeded", response.get("status"));
                        assertEquals(1627543260L, response.get("created"));
                }
        }

        @Test
        void testRefundDeposit_StripeException() throws Exception {
                // Arrange: Set up the order mock with correct values
                int orderId = 2;
                Order order = mock(Order.class);
                order.setId(orderId);
                order.setPrice(50.0);
                order.setPaymentReference("payment_intent_id"); // Set a mock payment reference

                // Mock the order repository to return the mocked order when getOrderById is called
                when(orderRepository.getOrderById(orderId)).thenReturn(order);

                // Mock StripeException to simulate an error
                StripeException stripeException = mock(StripeException.class);
                when(stripeException.getMessage()).thenReturn("Stripe error");

                // Mock Stripe to throw the StripeException
                try (MockedStatic<Refund> refundMockedStatic = Mockito.mockStatic(Refund.class)) {
                        refundMockedStatic.when(() -> Refund.create(any(RefundCreateParams.class)))
                                .thenThrow(stripeException);

                        // Act & Assert: Call the refundDeposit method and expect an exception
                        Exception exception = assertThrows(Exception.class, () -> {
                                paymentService.refundDeposit(orderId);
                        });

                        // Assert: Verify that the exception message matches the expected output
                        assertEquals("Payment processing failed: Stripe error", exception.getMessage());
                }
        }

        @Test
        void testGetCharge_Success() throws Exception {
                // Arrange
                int orderId = 1;
                Order order = new Order();
                order.setId(orderId);
                order.setPaymentReference("payment_intent_123");  // Mock the Stripe payment reference

                // Mock the order repository to return the mocked order when getOrderById is called
                when(orderRepository.getOrderById(orderId)).thenReturn(order);

                // Mock PaymentIntent and PaymentMethod
                PaymentIntent paymentIntentMock = mock(PaymentIntent.class);
                PaymentMethod paymentMethodMock = mock(PaymentMethod.class);

                // Mocking PaymentIntent fields
                when(paymentIntentMock.getAmountReceived()).thenReturn(10000L);  // 100.00 USD in cents
                when(paymentIntentMock.getStatus()).thenReturn("succeeded");
                when(paymentIntentMock.getCreated()).thenReturn(1627543254L); // Example timestamp
                when(paymentIntentMock.getPaymentMethod()).thenReturn("pm_123");

                // Mock PaymentMethod card details
                PaymentMethod.Card cardMock = mock(PaymentMethod.Card.class);
                when(cardMock.getLast4()).thenReturn("1234");
                when(cardMock.getBrand()).thenReturn("Visa");

                // Mock PaymentMethod to return the cardMock
                when(paymentMethodMock.getCard()).thenReturn(cardMock);

                // Mock Stripe to return the mocked objects
                try (MockedStatic<PaymentIntent> paymentIntentMockedStatic = Mockito.mockStatic(PaymentIntent.class);
                     MockedStatic<PaymentMethod> paymentMethodMockedStatic = Mockito.mockStatic(PaymentMethod.class)) {

                        paymentIntentMockedStatic.when(() -> PaymentIntent.retrieve("payment_intent_123"))
                                .thenReturn(paymentIntentMock);
                        paymentMethodMockedStatic.when(() -> PaymentMethod.retrieve("pm_123"))
                                .thenReturn(paymentMethodMock);

                        // Act: Call the getCharge method
                        Map<String, Object> chargeDetails = paymentService.getCharge(orderId);

                        // Assert: Verify that the returned details are correct
                        assertNotNull(chargeDetails);
                        assertEquals("1234", chargeDetails.get("cardLast4"));
                        assertEquals("Visa", chargeDetails.get("cardBrand"));
                        assertEquals(100.0, chargeDetails.get("amount"));
                        assertEquals("succeeded", chargeDetails.get("status"));
                        assertEquals("2021-07-29 00:20:54", chargeDetails.get("created")); // Expected date format
                }
        }
        @Test
        void testPaymentSucceeded_Success() throws Exception {
                // Arrange: Mock the orderRepository to return the mock order
                when(orderRepository.getOrderById(1)).thenReturn(order);

                // Act: Call the method under test
                String result = paymentService.paymentSucceeded(1);

                // Assert: Verify the order status and payment state
                assertTrue(order.isPaid());
                assertEquals(OrderStatus.CONFIRMED, order.getStatus());

                // Assert: Verify that the emailService methods are called
                verify(emailService).sendAdminNotification(anyString(), anyString(), eq(order));
                verify(emailService).sendCxPickupNotification(eq(order));
                verify(emailService).sendOrderConfirmation(eq(order));

                // Assert: Verify the returned message
                assertEquals("payment sucessfull", result);
        }

        @Test
        void testPaymentSucceeded_orderNotFound() throws Exception {
                // Arrange
                when(orderRepository.getOrderById(1)).thenReturn(null);

                // Act & Assert
                Exception exception = assertThrows(Exception.class, () -> {
                        paymentService.paymentSucceeded(1);
                });
                assertEquals("Order not found with id: 1", exception.getMessage());
        }


}