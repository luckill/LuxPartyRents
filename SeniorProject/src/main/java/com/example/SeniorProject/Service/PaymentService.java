package com.example.SeniorProject.Service;


import com.example.SeniorProject.Controller.OrderController;
import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderRepository;
import com.example.SeniorProject.Model.OrderStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
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

        public Map<String, Object> payALl(int orderId) throws Exception {
                Stripe.apiKey = stripeApiKey;
                Order order = orderRepository.getOrderById(orderId);
                long amountInCents = (long) (order.getPrice() * 100);
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
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
                        order.setPaid(true);
                        order.setStatus(OrderStatus.CONFIRMED);
                        orderRepository.save(order);
                        System.out.println(response.toString());
                        return response;
                } catch (StripeException e) {
                        throw new Exception("Payment processing failed: " + e.getMessage());
                }
        }

        public Map<String, Object> refund(int orderId, double price) throws Exception {
                Stripe.apiKey = stripeApiKey;
                Order order = orderRepository.getOrderById(orderId);
                long amountInCents = (long) (price * 100);
                RefundCreateParams params = RefundCreateParams.builder()
                        .setCharge("ch_1NirD82eZvKYlo2CIvbtLWuY")
                        .setAmount(amountInCents) // Set the amount to refund
                        .build();
                try {
                        Refund refund = Refund.create(params);
                        Map<String, Object> refundMap = new HashMap<>();
                        refundMap.put("id", refund.getId());
                        refundMap.put("amount", refund.getAmount());
                        refundMap.put("currency", refund.getCurrency());
                        refundMap.put("status", refund.getStatus());
                        refundMap.put("created", refund.getCreated());
                        return refundMap;
                } catch (StripeException e) {
                        // Log the error
                        // logger.error("Error creating payment intent: ", e);
                        throw new Exception("Payment processing failed: " + e.getMessage());
                }
        }

        public Map<String, Object> getCharge(int orderId) throws Exception {
                Stripe.apiKey = stripeApiKey;
                Order order = orderRepository.getOrderById(orderId);
                String stripeId = order.getPaymentReference();
                try {
                        Charge charge = Charge.retrieve(stripeId);
                        var card = charge.getPaymentMethodDetails().getCard();
                        Map<String, Object> refundMap = new HashMap<>();
                        refundMap.put("id", charge.getId());
                        refundMap.put("amount", charge.getAmount());
                        refundMap.put("currency", charge.getCurrency());
                        refundMap.put("status", charge.getStatus());
                        refundMap.put("created", charge.getCreated());
                        refundMap.put("Last-4", card.getLast4());
                        return refundMap;
                } catch (StripeException e) {
                        // Log the error
                        // logger.error("Error creating payment intent: ", e);
                        throw new Exception("Payment processing failed: " + e.getMessage());
                }
        }

}
