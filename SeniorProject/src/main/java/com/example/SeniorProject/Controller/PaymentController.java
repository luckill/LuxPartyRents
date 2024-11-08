package com.example.SeniorProject.Controller;


import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderRepository;
import com.example.SeniorProject.Model.OrderStatus;
import com.example.SeniorProject.Model.PaymentRequest;
import com.example.SeniorProject.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/payment/secure")
public class PaymentController {

        @Autowired
        PaymentService paymentService;
        @Autowired
        OrderRepository orderRepository;

        @PostMapping("/create-payment-intent")
        public Map<String, Object> createPaymentIntent(@RequestBody String orderId) throws Exception {
                System.out.println("order id" + orderId);
                int id;
                try {
                        id = Integer.parseInt(orderId);
                } catch (NumberFormatException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid orderId format");
                }

                Map<String, Object> response = paymentService.payAll(id);
                return response;
        }

        @PostMapping("/create-refund-intent")
        public Map<String, Object> createRefund(@RequestBody PaymentRequest paymentRequest, @RequestBody int orderId, @RequestBody double amount) throws Exception {
                Map<String, Object> response = paymentService.refund(orderId, amount);
                return response;
        }

        @GetMapping("/paymentInfo")
        public Map<String, Object> getPaymentInfo( @RequestBody int orderId) throws Exception {
                Map<String, Object> response = paymentService.getCharge(orderId);
                return response;
        }

        @PostMapping("/paymentSuccess")
        public String getPaymentSuccess(@RequestBody String orderId) throws Exception {
                System.out.println("order id" + orderId);
                int id;
                try {
                        id = Integer.parseInt(orderId);
                } catch (NumberFormatException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid orderId format");
                }
                String response = paymentService.paymentSucceeded(id);
                return response;
        }
}

