package com.example.SeniorProject.Controller;


import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderRepository;
import com.example.SeniorProject.Model.OrderStatus;
import com.example.SeniorProject.Model.PaymentRequest;
import com.example.SeniorProject.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        public ResponseEntity<?> createPaymentIntent(@RequestBody String orderId) throws Exception {
                try
                {
                        System.out.println("order id" + orderId);
                        int id;
                        try {
                                id = Integer.parseInt(orderId);
                        } catch (NumberFormatException e) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid orderId format");
                        }

                        Map<String, Object> response = paymentService.payAll(id);
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                }
                catch (ResponseStatusException exception)
                {
                        return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
                }
        }

        @PostMapping("/create-refund-intent")
        public ResponseEntity<?> createRefund(@RequestBody PaymentRequest paymentRequest, @RequestBody int orderId, @RequestBody double amount) throws Exception {
                try
                {
                        Map<String, Object> response = paymentService.refund(orderId, amount);
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                }
                catch (ResponseStatusException exception)
                {
                        return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
                }

        }

        @GetMapping("/paymentInfo")
        public ResponseEntity<?> getPaymentInfo( @RequestBody int orderId) throws Exception {
                try
                {
                        Map<String, Object> response = paymentService.getCharge(orderId);
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                }
                catch (ResponseStatusException exception)
                {
                        return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
                }

        }

        @PostMapping("/paymentSuccess")
        public ResponseEntity<?> getPaymentSuccess(@RequestBody String orderId) throws Exception {

                try
                {
                        System.out.println("order id" + orderId);
                        int id;
                        try {
                                id = Integer.parseInt(orderId);
                        } catch (NumberFormatException e) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid orderId format");
                        }

                        String response = paymentService.paymentSucceeded(id);
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                }
                catch (ResponseStatusException exception)
                {
                        return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
                }
        }
}

