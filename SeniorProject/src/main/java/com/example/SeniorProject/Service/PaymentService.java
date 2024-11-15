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
import org.springframework.beans.factory.annotation.Autowired;
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

        @Autowired
        OrderRepository orderRepository;


        static class CreatePaymentResponse {
                private String clientSecret;
                private String dpmCheckerLink;

                public CreatePaymentResponse(String clientSecret, String transactionId) {
                        this.clientSecret = clientSecret;
                        this.dpmCheckerLink = "https://dashboard.stripe.com/settings/payment_methods/review?transaction_id=" + transactionId;
                }
        }

        public Map<String, Object> payAll(int orderId) throws Exception {
                Stripe.apiKey = stripeApiKey;

                //gets order form db
                Order order = orderRepository.getOrderById(orderId);
                if (order == null) {
                        throw new Exception("Order not found with id: " + orderId);
                }

                //convert price into cents
                long amountInCents = (long) (order.getPrice() * 100);

                //creats paymentintent
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

                        //gets relevent info as response
                        Map<String, Object> response = new HashMap<>();
                        response.put("clientSecret", paymentResponse.clientSecret);
                        response.put("transactionId", paymentResponse.dpmCheckerLink); // Ensure this field exists
                        response.put("amount", paymentIntent.getAmount());
                        response.put("currency", paymentIntent.getCurrency());
                        response.put("status", paymentIntent.getStatus());
                        response.put("id", paymentIntent.getId());

                        //update the order
                        order.setPaid(false);
                        order.setPaymentReference(paymentIntent.getId());
                        order.setStatus(OrderStatus.RECEIVED);
                        orderRepository.save(order);

                        // Log and return the payment data
                        System.out.println("Payment Intent Created: " + response.toString());
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
                        .setPaymentIntent(order.getPaymentReference())
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
                        System.out.println("Caught exception: " + e.getMessage());
                        throw new Exception("Payment processing failed: " + e.getMessage());
                }
        }

        public Map<String, Object> refundAll(int orderId) throws Exception {
                Stripe.apiKey = stripeApiKey;
                Order order = orderRepository.getOrderById(orderId);
                long amountInCents = (long) (order.getPrice() * 100);
                RefundCreateParams params = RefundCreateParams.builder()
                        .setPaymentIntent(order.getPaymentReference())
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
                        order.setStatus(OrderStatus.REFUNDED);
                        orderRepository.save(order);
                        return refundMap;
                } catch (StripeException e) {
                        // Log the error
                        // logger.error("Error creating payment intent: ", e);
                        throw new Exception("Payment processing failed: " + e.getMessage());
                }
        }

        public Map<String, Object> refundDeposit(int orderId) throws Exception {
                Stripe.apiKey = stripeApiKey;
                Order order = orderRepository.getOrderById(orderId);
                long amountInCents = (long) (order.getPrice() * 100);// replace this with deposit
                RefundCreateParams params = RefundCreateParams.builder()
                        .setPaymentIntent(order.getPaymentReference())
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
                        order.setStatus(OrderStatus.REFUNDED);
                        orderRepository.save(order);
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

        public String paymentSucceeded(int orderId) throws Exception{
                Order order = orderRepository.getOrderById(orderId);
                if (order == null) {
                        throw new Exception("Order not found with id: " + orderId);
                }
                order.setPaid(true);
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                return "payment sucessfull";
        }

}
