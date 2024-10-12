package com.example.SeniorProject.Controller;


import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import com.stripe.Stripe;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

@RestController
@RequestMapping("/api/payment/secure")
public class PaymentController {

        @Value("${stripe.api.key}")
        private String stripeApiKey;

        // Define the inner classes to match your previous implementation
        static class CreatePaymentItem {
                @SerializedName("id")
                String id;

                @SerializedName("amount")
                Long amount;

                public String getId() {
                        return id;
                }

                public Long getAmount() {
                        return amount;
                }
        }

        static class CreatePayment {
                @SerializedName("items")
                CreatePaymentItem[] items;

                public CreatePaymentItem[] getItems() {
                        return items;
                }
        }

        static class CreatePaymentResponse {
                private String clientSecret;
                private String dpmCheckerLink;

                public CreatePaymentResponse(String clientSecret, String transactionId) {
                        this.clientSecret = clientSecret;
                        this.dpmCheckerLink = "https://dashboard.stripe.com/settings/payment_methods/review?transaction_id=" + transactionId;
                }
        }

        static int calculateOrderAmount(CreatePaymentItem[] items) {
                int total = 0;
                for (CreatePaymentItem item : items) {
                        total += item.getAmount();
                }
                return total;
        }

        @PostMapping("/create-payment-intent")
        public Map<String, Object> createPaymentIntent(@RequestBody CreatePayment createPayment) throws Exception {
                Stripe.apiKey = stripeApiKey;

                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount((long) calculateOrderAmount(createPayment.getItems()))
                        .setCurrency("usd")
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build())
                        .build();

                PaymentIntent paymentIntent = PaymentIntent.create(params);
                CreatePaymentResponse paymentResponse = new CreatePaymentResponse(paymentIntent.getClientSecret(), paymentIntent.getId());

                Map<String, Object> response = new HashMap<>();
                response.put("clientSecret", paymentResponse.clientSecret);
                response.put("transactionId", paymentResponse.dpmCheckerLink);
                return response;
        }
}