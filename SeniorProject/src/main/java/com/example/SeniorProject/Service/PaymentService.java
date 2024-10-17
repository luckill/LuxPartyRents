package com.example.SeniorProject.Service;


import com.example.SeniorProject.Controller.OrderController;
import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import com.stripe.Stripe;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

@Service
public class PaymentService {

        @Value("${stripe.api.key}")
        private String stripeApiKey;

        OrderRepository orderRepository;

        static class CreatePaymentResponse {
                private String clientSecret;
                private String dpmCheckerLink;

                public CreatePaymentResponse(String clientSecret, String transactionId) {
                        this.clientSecret = clientSecret;
                        this.dpmCheckerLink = "https://dashboard.stripe.com/settings/payment_methods/review?transaction_id=" + transactionId;
                }
        }

        public Map<String, Object> payALl(double price) throws Exception {
                Stripe.apiKey = stripeApiKey;
                //Order order = orderRepository.getOrderById(orderId);
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(200L)
                        .setCurrency("usd")
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build())
                        .build();

                try {
                        PaymentIntent paymentIntent = PaymentIntent.create(params);
                        CreatePaymentResponse paymentResponse = new CreatePaymentResponse(paymentIntent.getClientSecret(), paymentIntent.getId());

                        Map<String, Object> response = new HashMap<>();
                        response.put("clientSecret", paymentResponse.clientSecret);
                        response.put("transactionId", paymentResponse.dpmCheckerLink); // Ensure this field exists
                        response.put("amount", paymentIntent.getAmount());
                        response.put("currency", paymentIntent.getCurrency());
                        response.put("status", paymentIntent.getStatus());
                        response.put("id", paymentIntent.getId());
                        // Optionally update order status if applicable
                        // order.setPaid(true);
                        // orderRepository.save(order);
                        System.out.println(response.toString());
                        return response;
                } catch (StripeException e) {
                        // Log the error
                        // logger.error("Error creating payment intent: ", e);
                        throw new Exception("Payment processing failed: " + e.getMessage());
                }
        }

}
