package com.example.SeniorProject.Service;


import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderRepository;
import com.example.SeniorProject.Model.OrderStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.stripe.Stripe;

@Service
public class PaymentService {

        @Value("${stripe.api.key}")
        private String stripeApiKey;

        @Autowired
        OrderRepository orderRepository;
    @Autowired
    private EmailService emailService;


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
                long amountInCents = (long) (order.getSubtotal() * 100);

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
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
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
        long amountInCents = (long) (order.getSubtotal() * 100);
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

    public Map<String, Object> refundAllExceptDeposit(int orderId) throws Exception {
        Stripe.apiKey = stripeApiKey;
        Order order = orderRepository.getOrderById(orderId);
        long amountInCents = (long) ((order.getSubtotal() - order.getDeposit()) * 100);
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
        long amountInCents = (long) (order.getDeposit() * 100);// replace this with deposit
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
            order.setStatus(OrderStatus.COMPLETED);
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
        PaymentIntent paymentIntent = PaymentIntent.retrieve(stripeId);

        // You can alternatively use Charge if you have a charge ID
        // Charge charge = Charge.retrieve(referenceId);

        // Retrieve the PaymentMethod associated with the PaymentIntent
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentIntent.getPaymentMethod());

        // Extracting information from the PaymentMethod and PaymentIntent
        String cardLast4 = paymentMethod.getCard().getLast4();
        String cardBrand = paymentMethod.getCard().getBrand();
        long amountReceived = paymentIntent.getAmountReceived(); // Amount in cents
        String status = paymentIntent.getStatus();
        long createdTimestamp = paymentIntent.getCreated(); // Created timestamp (UNIX)

        // Convert timestamp to a human-readable format (optional)
        String created = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(createdTimestamp * 1000));

        // Return payment details as a Map
        return Map.of(
            "cardLast4", cardLast4,
            "cardBrand", cardBrand,
            "amount", amountReceived / 100.0, // Convert from cents to dollars
            "status", status,
            "created", created
        );
    }

    public String paymentSucceeded(int orderId) throws Exception{
        Order order = orderRepository.getOrderById(orderId);
        if (order == null) {
            throw new Exception("Order not found with id: " + orderId);
        }
        order.setPaid(true);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        emailService.sendAdminNotification(
                "New Order Placed",
                "A new order has been placed by " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                order
        );

        //Send notification to Customer about creation of new order, and pick up
        emailService.sendCxPickupNotification(order);
        emailService.sendOrderConfirmation(order);
        return "payment sucessfull";
    }

}
