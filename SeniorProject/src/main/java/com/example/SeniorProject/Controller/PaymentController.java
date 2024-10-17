package com.example.SeniorProject.Controller;


import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderRepository;
import com.example.SeniorProject.Model.OrderStatus;
import com.example.SeniorProject.Model.PaymentRequest;
import com.example.SeniorProject.Service.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
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

        @Autowired
        PaymentService paymentService;
        @Autowired
        OrderRepository orderRepository;
        @PostMapping("/create-payment-intent")
        public Map<String, Object> createPaymentIntent(@RequestBody PaymentRequest paymentRequest) throws Exception {
                Order order1 = new Order();
                order1.setPrice(1009);
                order1.setId(4);
                order1.setStatus(OrderStatus.RECEIVED);
                orderRepository.save(order1);

                int orderID = paymentRequest.getOrderID();

                // Retrieve the order and its price based on orderID
                // Assume you have a method to get order details
                Order order = orderRepository.getOrderById(orderID);
                if (order == null) {
                        throw new Exception("Order not found");
                }

                double price = order.getPrice();

                Map<String, Object> response = paymentService.payALl(10.10);
                return response;
        }
}

