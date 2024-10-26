package com.example.SeniorProject.Controller;


import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderRepository;
import com.example.SeniorProject.Model.OrderStatus;
import com.example.SeniorProject.Model.PaymentRequest;
import com.example.SeniorProject.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/secure")
public class PaymentController {

        @Autowired
        PaymentService paymentService;
        @Autowired
        OrderRepository orderRepository;
        @PostMapping("/create-payment-intent")
        public Map<String, Object> createPaymentIntent(@RequestBody PaymentRequest paymentRequest, @RequestBody int orderId) throws Exception {
                Map<String, Object> response = paymentService.payALl(orderId);
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
}

