/*package com.example.SeniorProject;

import com.example.SeniorProject.Configuration.*;
import com.example.SeniorProject.Controller.OrderController;
import com.example.SeniorProject.DTOs.OrderDTO;
import com.example.SeniorProject.DTOs.OrderProductDTO;
import com.example.SeniorProject.DTOs.ProductDTO;
import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.*;
import org.springframework.test.web.servlet.MockMvc;

import java.time.*;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
Unit Test Plan for OrderController
1. Test Framework and Tools
    Framework: JUnit 5
    Mocking Library: Mockito
    Spring Test: Spring's test context for integration testing
2. Test Objectives
    1. Validate the behavior of each endpoint in the OrderController.
    2. Ensure that proper responses are returned for various scenarios, including success and error conditions.
    3. Verify that the dependencies (repositories) are called correctly.
3. Key Functionalities to Test
    1.Order Creation: POST /order/create
    2.Retrieve All Orders: GET /order/getAll
    3.Retrieve Order by ID: GET /order/getById
    4.Retrieve Orders by Customer ID: GET /order/getOrderByCustomerId
4. Test Cases

A. Order Creation (createOrder)
Test Case: Successful order creation
Input: Valid OrderDTO and a valid customer ID.
Expected Output: HTTP 200 OK with a success message.
Mock Behavior: Mock customerRepository.getCustomerById(id) to return a valid customer, and mock productRepository to return valid products.
Test Case: Order ID already exists
Input: An OrderDTO with an existing ID.
Expected Output: HTTP 400 BAD REQUEST with an error message.
Mock Behavior: Mock orderRepository.existsById(orderDTO.getId()) to return true.
Test Case: Customer not found
Input: Valid OrderDTO with an invalid customer ID.
Expected Output: HTTP 404 NOT FOUND with an error message.
Mock Behavior: Mock customerRepository.getCustomerById(id) to return null.
Test Case: Product not found
Input: OrderDTO with a product that doesn’t exist.
Expected Output: HTTP 404 NOT FOUND with an error message.
Mock Behavior: Mock productRepository.findById(productDTO.getId()) to return Optional.empty().
Test Case: Insufficient product quantity
Input: OrderDTO with a quantity greater than the available stock.
Expected Output: HTTP 400 BAD REQUEST with an error message.
Mock Behavior: Mock the product's quantity to be less than the requested quantity.

B. Retrieve All Orders (getAllOrders)
Test Case: Retrieve all orders successfully
Input: None
Expected Output: HTTP 200 OK with a list of OrderDTOs.
Mock Behavior: Mock orderRepository.findAll() to return a list of orders.

C. Retrieve Order by ID (getOrderById)
Test Case: Retrieve order successfully
Input: Valid order ID.
Expected Output: HTTP 200 OK with the corresponding OrderDTO.
Mock Behavior: Mock orderRepository.findById(id) to return an Optional containing an order.
Test Case: Order not found
Input: Invalid order ID.
Expected Output: HTTP 404 NOT FOUND with an error message.
Mock Behavior: Mock orderRepository.findById(id) to return Optional.empty().

D. Retrieve Orders by Customer ID (getOrderByCustomerId)
Test Case: Retrieve orders successfully for a customer
Input: Valid customer ID with associated orders.
Expected Output: HTTP 200 OK with a list of OrderDTOs.
Mock Behavior: Mock customerRepository.findById(id) to return a valid customer and orderRepository.findOrderByCustomerId(id) to return a list of orders.
Test Case: Customer not found
Input: Invalid customer ID.
Expected Output: HTTP 404 NOT FOUND with an error message.
Mock Behavior: Mock customerRepository.findById(id) to return null.
Test Case: No orders associated with the customer
Input: Valid customer ID with no orders.
Expected Output: HTTP 404 NOT FOUND with an error message.
Mock Behavior: Mock orderRepository.findOrderByCustomerId(id) to return an empty list.
 */
/*
@WebMvcTest(OrderController.class)  // Use WebMvcTest for controller testing
//@SpringBootTest
@Import(TestSecurityConfig.class)
public class OrderControllerTest
{

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private OrderProductRepository orderProductRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PdfService pdfService;

    @MockBean
    private JwtTokenBlacklistService jwtTokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON strings
    @Autowired
    private OrderController orderController;

    @MockBean
    private EmailService emailService;

    @MockBean
    private UserDetailsService userDetailsService;


    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testOrderAlreadyExisted() throws Exception
    {
        // Arrange
        //    public OrderDTO(LocalDate date, int rentalTime, boolean paid, OrderStatus status)
        OrderDTO orderDTO = new OrderDTO(LocalDate.now(), 3,false, OrderStatus.RECEIVED);

        orderDTO.setId(100);  // Setting a specific order ID to check for existence

        // Mocking the behavior of orderRepository to simulate that the order already exists
        when(orderRepository.existsById(orderDTO.getId())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/order/create")
                        .param("id", "1")  // Sending customer ID as request parameter
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))  // Sending orderDTO as request body
                .andExpect(status().isBadRequest())  // Expect 400 Bad Request
                .andExpect(content().string("ERROR!!! - Order with this ID already exists."));  // Expect specific error message

        // Verify that the existsById method was called with the correct ID
        verify(orderRepository).existsById(orderDTO.getId());
    }

    @Test
    public void testCustomerIdNotFound() throws Exception
    {
        // Arrange
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1212323);
        int customerId = 999;

        when(orderRepository.existsById(orderDTO.getId())).thenReturn(false);
        when(customerRepository.getCustomerById(customerId)).thenReturn(null);  // Simulate customer not found

        // Act & Assert
        mockMvc.perform(post("/order/create")
                        .param("id", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isNotFound())  // Expect 404 Not Found
                .andExpect(content().string("ERROR!!! - Customer not found"));  // Expect specific error message
    }

    @Test
    public void testProductNotFound() throws Exception
    {
        // Arrange
        int productId = 456;
        int customerId = 123;

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(productId);
        OrderProductDTO orderProductDTO = new OrderProductDTO(1, productDTO);
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderProducts(Set.of(orderProductDTO));

        when(productRepository.findById(productId)).thenReturn(Optional.empty());  // Simulate product not found
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(new Customer()));

        // Act & Assert
        mockMvc.perform(post("/order/create")
                        .param("id", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isNotFound())  // Expect 500 Internal Server Error
                .andExpect(content().string("ERROR!!! - Product not found"));
    }

    @Test
    public void testInsufficientProductQuantity() throws Exception
    {
        // Arrange
        int requestedQuantity = 10; // Example requested quantity
        int availableQuantity = 5; // Example available quantity
        int customerId = 123;
        int productId = 456;

        Customer customer = new Customer();
        customer.setId(customerId);

        Product product = new Product();
        product.setId(productId);
        product.setQuantity(availableQuantity);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(productId);

        OrderProductDTO orderProductDTO = new OrderProductDTO(requestedQuantity, productDTO);
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1);
        orderDTO.setOrderProducts(new HashSet<>(Set.of(orderProductDTO)));

        when(orderRepository.existsById(orderDTO.getId())).thenReturn(false);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act & Assert
        mockMvc.perform(post("/order/create")
                        .param("id", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isBadRequest())  // Expect 400 Bad Request
                .andExpect(content().string("ERROR!!! - Insufficient product quantity."));
    }

    @Test
    public void testOrderCreatedSuccessfully() throws Exception
    {

        // Arrange
        int requestedQuantity = 2;
        int customerId = 123;
        int productId = 456;

        Customer customer = new Customer();
        customer.setId(customerId);

        Product product = new Product();
        product.setId(productId);
        product.setQuantity(10);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(productId);

        OrderProductDTO orderProductDTO = new OrderProductDTO(requestedQuantity, productDTO);
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(123123123); // Ensure this ID is unique
        orderDTO.setOrderProducts(new HashSet<>(Set.of(orderProductDTO)));

        when(orderRepository.existsById(orderDTO.getId())).thenReturn(false);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));


        // Act & Assert
        mockMvc.perform(post("/order/create")
                        .param("id", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isOk())  // Expect 200 OK
                .andExpect(content().string("Order created successfully."));
    }

    @Test
    public void testGetAllOrders() throws Exception {
        // Create sample order data
        //    public Order(int id, int rentalTime, boolean paid)
        List<Order> orders = Arrays.asList(new Order(12234, 3, true), new Order(44456, 5,false), new Order(002202, 4, false)); // Replace with actual order instances

        // Mock the repository to return the list of orders
        when(orderRepository.findAll()).thenReturn(orders);

        // Perform the GET request and validate response
        mockMvc.perform(get("/order/getAll"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void testGetOrderByIdNotFound() throws Exception {
        // Arrange
        int invalidOrderId = 999; // An ID that does not exist

        // Mock behavior
        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/order/getById?id=" + invalidOrderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("text/plain;charset=UTF-8")) // Expect text/plain response
                .andExpect(content().string("Order not found"));
    }

    @Test
    public void testGetOrderByIdSuccess()
    {
        //public Order(int id, int rentalTime, boolean paid)
        int orderId = 12222343;
        Order order = new Order(orderId, 3, true);
        order.setId(orderId);
        order.setPrice(100.0);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        //    public OrderDTO(LocalDate date, int rentalTime, boolean paid, OrderStatus status)
        OrderDTO orderDTO = new OrderDTO(LocalDate.of(2024,9,27),5, true, OrderStatus.RECEIVED);
        orderDTO.setId(orderId);
        orderDTO.setPrice(100.0);
        orderDTO.setOrderProducts(Collections.emptySet());

        try
        {
            mockMvc.perform(get("/order/getById")
                            .param("id", String.valueOf(orderId))) // Passing the valid ID
                    .andExpect(status().isOk()) // Expect HTTP 200 OK
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.id").value(orderId)) // Check if ID matches
                    .andExpect(jsonPath("$.status").value("RECEIVED")) // Check if status matches
                    .andExpect(jsonPath("$.price").value(100.0)) // Check if price matches
                    .andExpect(jsonPath("$.orderProducts").isEmpty());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetOrderByCustomerIdWithInvalidCustomerId()
    {
        int customerId = 999;

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());
        try
        {
            mockMvc.perform(get("/order/getOrderByCustomerId?id=" + customerId).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType("text/plain;charset=UTF-8")) // Expecting plain text
                    .andExpect(content().string("Customer not found"));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetOrderByCustomerIdWithCustomerWithNoOrder()
    {
        int customerId = 1;
        Customer customer = new Customer();
        customer.setId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.findOrderByCustomerId(customerId)).thenReturn(Collections.emptyList());

        try
        {
            mockMvc.perform(get("/order/getOrderByCustomerId?id=" + customerId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andExpect(content().string("No orders associate with this customer found"));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetOrderByCustomerIdSuccess() throws Exception
    {
        int customerId = 1;
        Customer customer = new Customer(); // Initialize with relevant data
        customer.setId(customerId);
        Order order1 = new Order(234555, 3, false); // Initialize with relevant data
        order1.setId(3453434);
        Order order2 = new Order(34345, 4, false); // Initialize with relevant data
        order2.setId(3243424);
        List<Order> orders = List.of(order1, order2);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.findOrderByCustomerId(customerId)).thenReturn(orders);

        mockMvc.perform(get("/order/getOrderByCustomerId?id=" + customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2))); // Expecting two orders
    }

    @Test
    public void testCancelOrder_Success() throws Exception {
        // Arrange
        int orderId = 1;

        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.RECEIVED);  // The order status is not CANCELLED
        order.setCustomer(customer);       // Set a non-null customer to avoid NullPointerException

        // Mock the findById method to return a valid order
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mock the save method to save the updated order
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act and Assert
        mockMvc.perform(put("/order/cancel")
                .param("orderId", String.valueOf(orderId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Order cancelled successfully"));

        // Verify the order status was updated and the notification was sent
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void testCancelOrderFailure() throws Exception
    {
        int orderId = 12234;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(put("/order/cancel")
                .param("orderId", String.valueOf(orderId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));
    }

    @Test
    public void testCancelOrderThatHasAlreadyBeenCancelled() throws Exception
    {
        int orderId = 1;

        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CANCELLED);  // The order status is not CANCELLED
        order.setCustomer(customer);       // Set a non-null customer to avoid NullPointerException

        // Mock the findById method to return a valid order
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        mockMvc.perform(put("/order/cancel")
                .param("orderId", String.valueOf(orderId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order is already cancelled"));
    }

    @Test
    public void testGetCustomerByOrderIdWithValidOrderAndCustomer()
    {
        Customer customer = new Customer();
        customer.setId(1);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhone("123456789");

        Order order = new Order(123445, 3,true);
        order.setCustomer(customer);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        try
        {
            mockMvc.perform(get("/order/getCustomerByOrderId")
                    .param("orderId", "123445"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.phone").value("123456789"));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetCustomerByOrderIdWithNonExistentOrder()
    {
        when(orderRepository.findById(999)).thenReturn(Optional.empty());
        try
        {
            mockMvc.perform(get("/order/getCustomerByOrderId")
                    .param("orderId", "999"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Order not found"));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetCustomerByOrderId_CustomerNotFound() throws Exception
    {
        // Step 1: Create a mock Order with a customer ID, but the customer does not exist in the database
        Customer mockCustomer = new Customer();
        mockCustomer.setId(1); // Set customer ID

        Order mockOrder = new Order();
        mockOrder.setId(1);
        mockOrder.setCustomer(mockCustomer); // Associate customer with order

        // Step 2: Mock repository responses
        when(orderRepository.findById(1)).thenReturn(Optional.of(mockOrder));
        when(customerRepository.findById(1)).thenReturn(Optional.empty()); // Customer not found

        // Step 3: Perform GET request and validate response
        mockMvc.perform(get("/order/getCustomerByOrderId")
                .param("orderId", "1"))
                .andExpect(status().isNotFound()) // Assert 404 NOT FOUND status
                .andExpect(content().string("Customer not found")); // Assert error message
    }

    @Test
    public void tetGetCustomerByOrderIdSuccess() throws Exception
    {

    }

    @Test
    public void testGetCurrentOrdersWithNoOrdersFound() throws Exception
    {
        int customerId = 1;
        when(orderRepository.findCurrentOrdersByCustomerId(customerId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/order/currentOrders").param("customerId", String.valueOf(customerId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No current orders found for the customer.")); // Expected structured error response
    }
}*/