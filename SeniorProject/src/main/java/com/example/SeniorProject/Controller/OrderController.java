package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.DTOs.*;
import com.example.SeniorProject.Service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.*;

import java.util.*;


@RestController
@RequestMapping(path = "/order")
public class OrderController
{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrder(@RequestParam(name = "id") int id, @RequestBody OrderDTO orderDTO) {
        try {
            // Assuming createOrder returns the created order object, including the orderId
            OrderDTO createdOrder = orderService.createOrder(id, orderDTO);
            // Return the order ID with a success message
            return new ResponseEntity<>(
                    Map.of(
                            "message", "An order has been successfully created, we will send a confirmation email to your email address soon.",
                            "orderId", createdOrder.getId()  // Assuming createdOrder has a method getId()
                    ),
                    HttpStatus.OK
            );
        }
        catch (ResponseStatusException exception)
        {
            System.err.println("Error creating order: " + exception.getMessage());
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
        catch (Exception e)
        {
            // Log unexpected errors
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred");
        }
    }

    // Cancel an order
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelOrder(@RequestParam int orderId, @RequestParam String role)
    {
        try
        {
            if (role.equalsIgnoreCase("admin"))
            {
                orderService.orderCancelledByAdmin(orderId);
            }
            else if (role.equalsIgnoreCase("user"))
            {
                orderService.orderCancelledByCustomer(orderId);
            }
            else
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or unknown role");
            }
            return new ResponseEntity<>("An order has been successfully cancelled", HttpStatus.OK);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    // Fetching the current orders for a customer (status = 'active')
    @GetMapping(path = "/currentOrders")
    public ResponseEntity<?> getCurrentOrders(@RequestParam int customerId)
    {
        try
        {
            List<OrderDTO> currentOrders = orderService.getCurrentOrders(customerId);
            return ResponseEntity.status(HttpStatus.OK).body(currentOrders);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    // Fetch past orders for a customer (status = 'completed')
    @GetMapping(path = "/pastOrders")
    public ResponseEntity<?> getPastOrders(@RequestParam int customerId)
    {
        try
        {
            List<OrderDTO> pastOrders = orderService.getPastOrders(customerId);
            return ResponseEntity.status(HttpStatus.OK).body(pastOrders);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }


    // Read all orders with pagination
    @GetMapping(path = "/getAll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllOrders()
    {
        try
        {
            List<OrderDTO> orders = orderService.getAllOrders();
            return ResponseEntity.status(HttpStatus.OK).body(orders);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    // Read a single order by ID
    @GetMapping(path = "/getById")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrderById(@RequestParam int id)
    {
        try
        {
            OrderDTO order = orderService.getOrderById(id);
            return ResponseEntity.status(HttpStatus.OK).body(order);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @PutMapping(path = "/update")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateOrder(@RequestParam int orderId, @RequestBody OrderDTO orderDTO)
    {
        try
        {
            orderService.updateOrderStatus(orderId, orderDTO);
            return new ResponseEntity<>("The order has been successfully updated", HttpStatus.OK);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    // Delete an order
    @DeleteMapping(path = "/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteOrder(@RequestParam(value = "orderId", required = false, defaultValue = "0")  int id)
    {
        System.out.println("calling delete");
        System.out.println(id);
        try
        {
            orderService.deleteOrder(id);
            return ResponseEntity.ok("Order deleted successfully");
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    // Return an order
    @PostMapping(path = "/return")
    public ResponseEntity<?> returnOrder(@RequestParam int orderId)
    {
        try
        {
            orderService.returnOrder(orderId);
            return ResponseEntity.ok("Order returned successfully");
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @GetMapping(path = "/getOrderByCustomerId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrderByCustomerId(@RequestParam int id)
    {
        try
        {
            List<OrderDTO> orders = orderService.getOrderByCustomerId(id);
            return ResponseEntity.status(HttpStatus.OK).body(orders);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @GetMapping("/getCustomerByOrderId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCustomerByOrderId(@RequestParam int orderId)
    {
        try
        {
            CustomerDTO customer = orderService.getCustomerByOrderId(orderId);
            return ResponseEntity.status(HttpStatus.OK).body(customer);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @GetMapping("/sendOrderDueForReturnNotification")
    public void sendOrderDueForReturnNotification()
    {
        try
        {
            orderService.orderDueCheck();
        }
        catch (ResponseStatusException exception)
        {
            ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }
}