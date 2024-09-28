package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Email.EmailDetails;
import com.example.SeniorProject.Service.EmailService;
import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderRepository;
import com.example.SeniorProject.Model.Customer;
import com.example.SeniorProject.Model.CustomerRepository;
import com.example.SeniorProject.Model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path="/order")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;

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
                if (customer.getId() != 0) {
                    Customer existingCustomer = customerRepository.findById(customer.getId()).orElse(null);
                    if (existingCustomer != null) {
                        order.setCustomer(existingCustomer);
                    } else {
                        customerRepository.save(customer);
                    }
                } else {
                    customerRepository.save(customer);
                }
            }

            // Save the order after handling the customer
            orderRepository.save(order);

            // Send notification to admin for new order
            sendAdminNotification("New Order Placed",
                    "A new order has been placed by "
                            + customer.getFirstName() + " " + customer.getLastName(),
                    order);

            return "Order created successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error creating order";
        }
    }

    // Read all orders
    @GetMapping(path="/getAll")
    public @ResponseBody List<Order> getAllOrders() {
        return orderRepository.findAll();
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

    // Get orders by customer ID
    @GetMapping(path="/getOrderByCustomerId")
    public @ResponseBody List<Order> getOrderByCustomerId(@RequestParam int id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        return customer != null ? customer.getOrders() : null;
    }

    // Return an order
    @PostMapping(path="/return")
    public @ResponseBody String returnOrder(@RequestBody String orderJson) {
        try {
            Order order = objectMapper.readValue(orderJson, Order.class);

            // Update the order status to "Returned"
            order.setStatus("Returned");

            // Save the updated order
            orderRepository.save(order);

            // Send notification to both admin and customer for successful return
            sendAdminNotification("Order Returned",
                    "Order ID " + order.getId() + " has been successfully returned by "
                            + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                    order);

            sendCustomerReturnNotification(order);

            return "Order returned successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error returning order";
        }
    }

    // Cancel an order
    @PostMapping(path="/cancel")
    public @ResponseBody String cancelOrder(@RequestBody String orderJson) {
        try {
            Order order = objectMapper.readValue(orderJson, Order.class);

            // Logic to handle order cancellation
            orderRepository.deleteById(order.getId());

            // Send notification to admin for order cancellation
            sendAdminNotification("Order Canceled",
                    "Order ID " + order.getId() + " has been canceled by "
                            + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                    order);

            return "Order canceled successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error canceling order";
        }
    }

    // Helper method to send email notifications to the admin
    private void sendAdminNotification(String subject, String messageBody, Order order) {
        EmailDetails adminEmailDetails = new EmailDetails();
        adminEmailDetails.setRecipient("190project2024@gmail.com"); //email of admin
        adminEmailDetails.setSubject(subject);

        String emailBody = messageBody +
                "\nOrder ID: " + order.getId() +
                "\nCustomer: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() +
                "\nTotal Amount: $" + order.getPrice();

        adminEmailDetails.setMessageBody(emailBody);
        emailService.sendSimpleEmail(adminEmailDetails);
    }

    // Helper method to send email notifications to the customer when the order is returned
    private void sendCustomerReturnNotification(Order order) {
        EmailDetails customerEmailDetails = new EmailDetails();
        customerEmailDetails.setRecipient(order.getCustomer().getEmail());
        customerEmailDetails.setSubject("Order Returned Successfully");

        String emailBody = "Dear " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + "," +
                "\n\nYour order with ID: " + order.getId() + " has been successfully returned." +
                "\n\nThank you for shopping with us!";

        customerEmailDetails.setMessageBody(emailBody);
        emailService.sendSimpleEmail(customerEmailDetails);
    }
}
