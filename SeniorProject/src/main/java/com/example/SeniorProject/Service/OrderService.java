package com.example.SeniorProject.Service;

import com.example.SeniorProject.DTOs.*;
import com.example.SeniorProject.Email.*;
import com.example.SeniorProject.Model.*;
import jakarta.transaction.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.server.*;

import java.util.*;
import java.util.stream.*;

@Service
public class OrderService
{
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PdfService pdfService;

    @Transactional
    public OrderDTO createOrder(int id, OrderDTO orderDTO)
    {
        // Check if the order already exists
        int orderId = generateUniqueOrderId();

        // Find the customer by ID
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Customer not found");
        }

        // Create the new order
        Order order = new Order(orderId, orderDTO.getRentalTime(), orderDTO.isPaid());
        order.setCustomer(customer);
        // Save the order to the database
        order = orderRepository.save(order);

        double totalPrice = 0;

        // Process the products in the order
        for (OrderProductDTO orderProductDTO : orderDTO.getOrderProducts())
        {
            ProductDTO productDTO = orderProductDTO.getProduct();
            Product product = productRepository.findById(productDTO.getId()).orElse(null);
            if (product == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Product not found");
            }

            int quantity = orderProductDTO.getQuantity();
            if (quantity > product.getQuantity())
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - Insufficient product quantity.");
            }

            // Update product quantity and calculate total price
            totalPrice += product.getPrice() * quantity;
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            // Save the OrderProduct to link the order and product
            OrderProduct orderProduct = new OrderProduct(order, product, quantity);
            orderProductRepository.save(orderProduct);
        }

        // Set the total price for the order and save it again
        order.setPrice(totalPrice);
        orderRepository.save(order);

        // Send notification to admin for new order
        sendAdminNotification(
                "New Order Placed",
                "A new order has been placed by " + customer.getFirstName() + " " + customer.getLastName(),
                order
        );

        return mapToOrderDTO(order);
    }

    // Cancel an order
    public void cancelOrder(int orderId)
    {
        // Find the order by ID
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Order not found");
        }

        // Check if the order is already cancelled
        if (order.getStatus() == OrderStatus.CANCELLED)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - Order is already cancelled");
        }

        // Set the order status to 'cancelled'
        order.setStatus(OrderStatus.CANCELLED);

        // Save the order with the updated status
        orderRepository.save(order);

        // Send notification to admin for order cancellation
        sendAdminNotification(
                "Order Cancelled",
                "Order ID " + order.getId() + " has been cancelled by " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                order
        );
    }

    // Fetching the current orders for a customer (status = 'active')
    public List<OrderDTO> getCurrentOrders(int customerId)
    {
        List<Order> currentOrders = orderRepository.findCurrentOrdersByCustomerId(customerId);
        if (currentOrders.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No current orders found for the customer.");
        }

        return currentOrders.stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    // Fetch past orders for a customer (status = 'completed')
    public List<OrderDTO> getPastOrders(int customerId)
    {
        List<Order> pastOrders = orderRepository.findPastOrdersByCustomerId(customerId);
        if (pastOrders.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No past orders found for the customer.");
        }

        return pastOrders.stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getAllOrders()
    {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No orders found.");
        }
        return orders.stream().map(this::mapToOrderDTO).collect(Collectors.toList());
    }

    public OrderDTO getOrderById(int id)
    {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return mapToOrderDTO(order);
    }

    public void updateOrder(int orderId, OrderDTO orderDTO)
    {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Order not found");
        }

        if (orderDTO.getRentalTime() != order.getRentalTime() && orderDTO.getRentalTime() != 0)
        {
            order.setRentalTime(orderDTO.getRentalTime());
        }
        if (orderDTO.isPaid() != order.isPaid())
        {
            order.setPaid(orderDTO.isPaid());
        }
        if (orderDTO.getStatus() != null)
        {
            if (!orderDTO.getStatus().equals(order.getStatus().toString()))
            {
                if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.RETURNED)
                {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is already returned or cancelled");
                }
                order.setStatus(OrderStatus.valueOf(orderDTO.getStatus()));
            }
        }
        else
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - order status have null value");
        }

        orderRepository.save(order);
    }

    // Delete an order
    public void deleteOrder(int id)
    {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error!!! - Order associated with this id is not found in the database");
        }
        orderRepository.deleteById(id);
    }

    // Return an order
    public void returnOrder(int orderId)
    {
        //checking to see if order exists
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error!!! - Order not found");
        }

        if (order.getStatus() == OrderStatus.RETURNED)
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is already returned");
        }

        //returning the products within order

        for (OrderProduct orderProduct : order.getOrderProducts()){
            //checking to make sure product exists before updating it
            // and that quantity of the order is not 0
            Product product = orderProduct.getProduct();
            int quantity = orderProduct.getQuantity();
            if (product == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Product not found");
            }else if (quantity == 0)
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - Insufficient product quantity.");
            }else{}

            //update product quantity and save
            product.setQuantity(product.getQuantity() + quantity );
            productRepository.save(product);
        }
        

        // Update the order status to "Returned"
        order.setStatus(OrderStatus.RETURNED);

        // Save the updated order
        orderRepository.save(order);

        // Send notification to both admin and customer for successful return
        sendAdminNotification("Order Returned",
                "Order ID " + order.getId() + " has been successfully returned by "
                        + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                order);

        sendCustomerReturnNotification(order);
    }

    public List<OrderDTO> getOrderByCustomerId(int id)
    {
        Customer customer = customerRepository.findById(id).orElse(null);

        if (customer == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }

        List<Order> orders = orderRepository.findOrderByCustomerId(customer.getId());
        if (orders.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No orders associated with this customer found");
        }
        return orders.stream().map(this::mapToOrderDTO).toList();
    }

    public CustomerDTO getCustomerByOrderId(int orderId)
    {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        Customer customer = order.getCustomer();
        if (customer == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        return new CustomerDTO(customer.getFirstName(), customer.getLastName(), customer.getEmail(), customer.getPhone());
    }

    private OrderDTO mapToOrderDTO(Order order)
    {
        Set<OrderProductDTO> orderProductDTOs = order.getOrderProducts().stream()
                .map(orderProduct -> new OrderProductDTO(orderProduct.getQuantity(), new ProductDTO(orderProduct.getProduct().getId(), orderProduct.getProduct().getName(), orderProduct.getProduct().getPrice(), orderProduct.getProduct().getType())))
                .collect(Collectors.toSet());
        OrderDTO orderDTO = new OrderDTO(order.getCreationDate(), order.getRentalTime(), order.isPaid(), order.getStatus());
        orderDTO.setPrice(order.getPrice());
        orderDTO.setId(order.getId());
        orderDTO.setOrderProducts(orderProductDTOs);
        return orderDTO;
    }

    // Helper method to send email notifications to the admin
    private void sendAdminNotification(String subject, String messageBody, Order order)
    {
        EmailDetails adminEmailDetails = new EmailDetails();
        adminEmailDetails.setRecipient("zhijunli7799@gmail.com"); //email of admin
        adminEmailDetails.setSubject(subject);

        String emailBody = messageBody +
                "\nOrder ID: " + order.getId() +
                "\nCustomer: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() +
                "\nTotal Amount: $" + order.getPrice();

        adminEmailDetails.setMessageBody(emailBody);
        emailService.sendSimpleEmail(adminEmailDetails);
    }

    // Helper method to send email notifications to the customer when the order is returned
    private void sendCustomerReturnNotification(Order order)
    {
        EmailDetails customerEmailDetails = new EmailDetails();
        customerEmailDetails.setRecipient(order.getCustomer().getEmail());
        customerEmailDetails.setSubject("Order Returned Successfully");

        String emailBody = "Dear " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + "," +
                "\n\nYour order with ID: " + order.getId() + " has been successfully returned." +
                "\n\nThank you for shopping with us!";

        customerEmailDetails.setMessageBody(emailBody);
        emailService.sendSimpleEmail(customerEmailDetails);
    }

    // Method to generate a unique order ID
    private int generateUniqueOrderId()
    {
        Random random = new Random();
        int orderId;
        boolean exists;
        do
        {
            orderId = 1000000000 + random.nextInt(1147483648);
            exists = orderRepository.existsById(orderId);
        }
        while (exists);

        return orderId;
    }
}