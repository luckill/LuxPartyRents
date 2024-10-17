package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderRepository;
import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Model.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

        @InjectMocks
        private PaymentService paymentService;

        @Mock
        private OrderRepository orderRepository;

        @BeforeEach
        public void setup() {
                MockitoAnnotations.openMocks(this);
        }

        @Test
        void payAllTest() throws Exception {

        }

}