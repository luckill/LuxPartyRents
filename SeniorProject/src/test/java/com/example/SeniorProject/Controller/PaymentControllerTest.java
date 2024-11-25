package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePaymentIntent_Success() throws Exception {
        String orderId = "1";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("clientSecret", "mockSecret");
        mockResponse.put("transactionId", "mockTransactionId");

        when(paymentService.payAll(1)).thenReturn(mockResponse);

        ResponseEntity<?> response = paymentController.createPaymentIntent(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(paymentService, times(1)).payAll(1);
    }

    @Test
    void testCreatePaymentIntent_InvalidOrderIdFormat() throws Exception {
        String orderId = "invalid";

        ResponseEntity<?> response = paymentController.createPaymentIntent(orderId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid orderId format", response.getBody());
        verifyNoInteractions(paymentService);
    }

    @Test
    void testCreatePaymentIntent_ServiceException() throws Exception {
        String orderId = "1";
        when(paymentService.payAll(1)).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Service error"));

        ResponseEntity<?> response = paymentController.createPaymentIntent(orderId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Service error", response.getBody());
        verify(paymentService, times(1)).payAll(1);
    }

    @Test
    void testCreateRefund_Success() throws Exception {
        int orderId = 1;
        double amount = 100.0;
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");

        when(paymentService.refund(orderId, amount)).thenReturn(mockResponse);

        ResponseEntity<?> response = paymentController.createRefund(orderId, amount);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(paymentService, times(1)).refund(orderId, amount);
    }

    @Test
    void testCreateRefund_ServiceException() throws Exception {
        int orderId = 1;
        double amount = 100.0;
        when(paymentService.refund(orderId, amount)).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Refund error"));

        ResponseEntity<?> response = paymentController.createRefund(orderId, amount);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Refund error", response.getBody());
        verify(paymentService, times(1)).refund(orderId, amount);
    }

    @Test
    void testCreateRefundDepositOnly_Success() throws Exception {
        int orderId = 1;
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");

        when(paymentService.refundDeposit(orderId)).thenReturn(mockResponse);

        ResponseEntity<?> response = paymentController.createRefundDepositOnly(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(paymentService, times(1)).refundDeposit(orderId);
    }

    @Test
    void testGetPaymentInfo_Success() throws Exception {
        int orderId = 1;
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("id", "mockId");

        when(paymentService.getCharge(orderId)).thenReturn(mockResponse);

        ResponseEntity<?> response = paymentController.getPaymentInfo(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(paymentService, times(1)).getCharge(orderId);
    }

    @Test
    void testGetPaymentInfo_ServiceException() throws Exception {
        int orderId = 1;
        when(paymentService.getCharge(orderId)).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment info error"));

        ResponseEntity<?> response = paymentController.getPaymentInfo(orderId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Payment info error", response.getBody());
        verify(paymentService, times(1)).getCharge(orderId);
    }

    @Test
    void testGetPaymentSuccess_Success() throws Exception {
        String orderId = "1";
        String mockResponse = "payment success";

        when(paymentService.paymentSucceeded(1)).thenReturn(mockResponse);

        ResponseEntity<?> response = paymentController.getPaymentSuccess(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(paymentService, times(1)).paymentSucceeded(1);
    }

    @Test
    void testGetPaymentSuccess_InvalidOrderIdFormat() throws Exception {
        String orderId = "invalid";

        ResponseEntity<?> response = paymentController.getPaymentSuccess(orderId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid orderId format", response.getBody());
        verifyNoInteractions(paymentService);
    }

    @Test
    void testGetPaymentSuccess_ServiceException() throws Exception {
        String orderId = "1";
        when(paymentService.paymentSucceeded(1)).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment success error"));

        ResponseEntity<?> response = paymentController.getPaymentSuccess(orderId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Payment success error", response.getBody());
        verify(paymentService, times(1)).paymentSucceeded(1);
    }
}
