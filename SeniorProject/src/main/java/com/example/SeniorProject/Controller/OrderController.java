package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.DTOs.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path="/order")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository; // Add CustomerRepository to handle Customer persistence

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<?> createOrder(@RequestParam(name = "id") int id, @RequestBody OrderDTO orderDTO)
    {
        if (orderRepository.existsById(orderDTO.getId()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR!!! - Order with this ID already exists.");
        }
        Customer customer = customerRepository.getCustomerById(id);
        if (customer == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR!!! - Customer not found");
        }
        Order order = new Order(orderDTO.getDate(), orderDTO.getRentalTime(),  orderDTO.isPaid(), orderDTO.getStatus());
        order.setId(orderDTO.getId());  // Manually assigning the ID
        order.setCustomer(customer);
        order = orderRepository.save(order);
        double totalPrice = 0;
        for (OrderProductDTO orderProductDTO : orderDTO.getOrderProducts())
        {
            ProductDTO productDTO = orderProductDTO.getProduct();
            Product product = productRepository.findById(productDTO.getId()).orElseThrow(() -> new RuntimeException("Product not found"));
            int quantity = orderProductDTO.getQuantity();
            if (quantity > product.getQuantity())
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("hahahah");
            }
            totalPrice += product.getPrice() * quantity;
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            OrderProduct orderProduct = new OrderProduct(order, product, quantity);
            orderProductRepository.save(orderProduct);
        }
        order.setPrice(totalPrice);
        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.OK).body("Order created successfully.");
    }

    // Read all orders with pagination
    @GetMapping(path="/getAll")
    public @ResponseBody
    List<OrderDTO> getAllOrders()
    {
       List<Order> orders = orderRepository.findAll();
       List<OrderDTO> orderDTOs = orders.stream().map(this::mapToOrderDTO).collect(Collectors.toList());

    return ResponseEntity.ok(orderDTOs).getBody();
    }

    // Read a single order by ID
    @GetMapping(path="/getById")
    public ResponseEntity<?> getOrderById(@RequestParam int id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
        OrderDTO orderDTO = mapToOrderDTO(order);
        return ResponseEntity.status(HttpStatus.OK).body(orderDTO);

    }

    // Update an existing order
    @PostMapping(path="/update")
    public @ResponseBody String updateOrder(@RequestBody String orderJson) {
        try {
            Order order = objectMapper.readValue(orderJson, Order.class);
            orderRepository.save(order);
            return "Order updated successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error updating order";
        }
    }

    // Delete an order
    @PostMapping(path="/delete")
    public @ResponseBody String deleteOrder(@RequestBody String orderJson) {
        try {
            Order order = objectMapper.readValue(orderJson, Order.class);
            orderRepository.deleteById(order.getId());
            return "Order deleted successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error deleting order";
        }
    }

    // Get products associated with an order
    @GetMapping(path="/getProductsByOrderId")
    public @ResponseBody Set<Product> getProductsByOrderId(@RequestParam int id) {
        Order order = orderRepository.findById(id).orElse(null);
        //return order != null ? order.getProducts() : null;
        return null;
    }

    @GetMapping(path="/getOrderByCustomerId")
    public ResponseEntity<?> getOrderByCustomreId(@RequestParam int id)
    {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }

        List<Order> orders = orderRepository.findOrderByCustomerId(customer.getId());
        if (orders.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No orders associate with this customer found");
        }
        
        List<OrderDTO> orderDTOs = orders.stream().map(this::mapToOrderDTO).toList();
        return ResponseEntity.status(HttpStatus.OK).body(orderDTOs);
    }

    private OrderDTO mapToOrderDTO(Order order) {
        Set<OrderProductDTO> orderProductDTOs = order.getOrderProducts().stream()
            .map(orderProduct -> new OrderProductDTO(orderProduct.getQuantity(), new ProductDTO(orderProduct.getProduct().getId(), orderProduct.getProduct().getName(),orderProduct.getProduct().getPrice())))
            .collect(Collectors.toSet());
        OrderDTO orderDTO = new OrderDTO(order.getDate(), order.getRentalTime(), order.isPaid(), order.getStatus());
        orderDTO.setPrice(order.getPrice());
        orderDTO.setId(order.getId());
        orderDTO.setOrderProducts(orderProductDTOs);
        return orderDTO;
    }


}
