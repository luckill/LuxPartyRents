package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderRepository;
import com.example.SeniorProject.Model.Customer;
import com.example.SeniorProject.Model.CustomerRepository; // Import for CustomerRepository
import com.example.SeniorProject.Model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

@RestController
@RequestMapping(path="/order")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository; // Add CustomerRepository to handle Customer persistence

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Create a new order
    @PostMapping(path="/create")
    public @ResponseBody String createOrder(@RequestBody String orderJson) {
        try {
            // Deserialize the order JSON into an Order object
            Order order = objectMapper.readValue(orderJson, Order.class);

            // Check if the customer already exists
            Customer customer = order.getCustomer();
            if (customer != null) {
                // If the customer has an ID, fetch it from the database
                if (customer.getId() != 0) {
                    Customer existingCustomer = customerRepository.findById(customer.getId()).orElse(null);
                    if (existingCustomer != null) {
                        order.setCustomer(existingCustomer); // Attach existing customer to the order
                    } else {
                        // If the customer ID is provided but not found in DB, save the new customer
                        customerRepository.save(customer);
                    }
                } else {
                    // If the customer doesn't have an ID, it's a new customer, so save it first
                    customerRepository.save(customer);
                }
            }

            // Save the order after handling the customer
            orderRepository.save(order);
            return "Order created successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error creating order";
        }
    }

    // Read all orders with pagination
    @GetMapping(path="/getAll")
    public @ResponseBody Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    // Read a single order by ID
    @GetMapping(path="/getById")
    public @ResponseBody Order getOrderById(@RequestParam int id) {
        return orderRepository.findById(id).orElse(null);
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
        return order != null ? order.getProducts() : null;
    }
}
