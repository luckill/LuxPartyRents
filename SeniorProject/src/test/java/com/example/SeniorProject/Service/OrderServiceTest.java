package com.example.SeniorProject.Service;

import com.example.SeniorProject.DTOs.*;
import com.example.SeniorProject.Email.*;
import com.example.SeniorProject.Model.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.example.SeniorProject.Model.Order;
import org.springframework.http.*;
import org.springframework.web.server.*;

public class OrderServiceTest
{
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    public void setup()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateOrderSuccess()
    {
        ProductDTO productDTO = new ProductDTO(1, "Laptop", 1500.0, 100,  "Electronics", "A high-end laptop", false);
        OrderProductDTO orderProductDTO = new OrderProductDTO(2, productDTO);
        OrderDTO orderDTO = new OrderDTO(LocalDate.now(), 5, true, OrderStatus.RECEIVED);
        orderDTO.setOrderProducts(Set.of(orderProductDTO));
        Product product = new Product(productDTO.getQuantity(), productDTO.getPrice(), productDTO.getType(), productDTO.getName(), productDTO.getDescription());

        when(customerRepository.findById(1)).thenReturn(Optional.of(new Customer()));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        OrderDTO createdOrder = orderService.createOrder(1, orderDTO);

        // Verify
        assertNotNull(createdOrder);
        assertEquals(createdOrder.getCreationDate(), orderDTO.getCreationDate());
        assertEquals(createdOrder.getRentalTime(), orderDTO.getRentalTime());
        assertEquals(createdOrder.getStatus(), orderDTO.getStatus());
        assertEquals(createdOrder.isPaid(), orderDTO.isPaid());
        //assertEquals();
        //assertEquals(p, createdOrder.getPrice());
        verify(emailService, times(1)).sendSimpleEmail(any(EmailDetails.class));
    }

    @Test
    public void testCreateOrderWithCustomerNotFound()
    {
        OrderDTO orderDTO = new OrderDTO();
        int customerId = 2;

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.createOrder(customerId, orderDTO));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("ERROR!!! - Customer not found", exception.getReason());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    public void testCreateOrderWithProductNotFound()
    {
        int validCustomerId = 1;
        OrderProductDTO orderProductDTO = new OrderProductDTO(2, new ProductDTO());
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderProducts(Set.of(orderProductDTO));

        when(customerRepository.findById(validCustomerId)).thenReturn(Optional.of(new Customer()));
        when(productRepository.findById(1)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.createOrder(validCustomerId, orderDTO));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("ERROR!!! - Product not found", exception.getReason());
    }

    @Test
    public void testCreateOrderWithInsufficientProductQuantity()
    {
        ProductDTO productDTO = new ProductDTO(1, "Laptop", 1500.0, 4,  "Electronics", "A high-end laptop", false);
        OrderProductDTO orderProductDTO = new OrderProductDTO(10, productDTO);
        OrderDTO orderDTO = new OrderDTO(LocalDate.now(), 5, true, OrderStatus.RECEIVED);
        orderDTO.setOrderProducts(Set.of(orderProductDTO));
        Product product = new Product(productDTO.getQuantity(), productDTO.getPrice(), productDTO.getType(), productDTO.getName(), productDTO.getDescription());

        when(customerRepository.findById(1)).thenReturn(Optional.of(new Customer()));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.createOrder(1, orderDTO));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("ERROR!!! - Insufficient product quantity.", exception.getReason());
    }

    @Test
    public void cancelExistingOrder()
    {
        Customer customer = new Customer();
        Order order = new Order(122345, 3,false);
        order.setCustomer(customer);
        when(orderRepository.findById(122345)).thenReturn(Optional.of(order));
        orderService.cancelOrder(122345);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
        verify(emailService, times(1)).sendSimpleEmail(any(EmailDetails.class));
    }

    @Test
    public void cancelNonExistingOrder()
    {
        int orderId = 233223213;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.cancelOrder(orderId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("ERROR!!! - Order not found", exception.getReason());
    }

    @Test
    public void cancelAlreadyCanceledOrder()
    {
        Customer customer = new Customer();
        Order order = new Order(122345, 3,false);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(122345)).thenReturn(Optional.of(order));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.cancelOrder(122345));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("ERROR!!! - Order is already cancelled", exception.getReason());
    }

    @Test
    public void testGetCurrentOrdersSuccess()
    {
        // Arrange
        int customerId = 1;

        // Create mock Order objects
        Order order1 = new Order(122345, 3, false);
        order1.setStatus(OrderStatus.READY_FOR_PICK_UP);
        Order order2 = new Order(242466, 10, false);
        Order order3 = new Order(1232345, 3, false);

        List<Order> orders = Arrays.asList(order1, order2, order3);

        // Mock the repository call to return the mock orders
        when(orderRepository.findCurrentOrdersByCustomerId(customerId)).thenReturn(orders);

        // Act
        List<OrderDTO> result = orderService.getCurrentOrders(customerId);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3); // Ensure two orders are returned

        for (int i = 0; i < 3; i++)
        {
            OrderDTO order = result.get(i);
            assertEquals(order.getId(), orders.get(i).getId());
            assertEquals(order.getCreationDate(), orders.get(i).getCreationDate());
            assertEquals(order.getRentalTime(), orders.get(i).getRentalTime());
            assertEquals(order.getRentalTime(), orders.get(i).getRentalTime());
            assertEquals(order.getStatus(), orders.get(i).getStatus().toString());
            assertNotEquals(order.getStatus(), OrderStatus.CANCELLED.toString());
            assertNotEquals(order.getStatus(), OrderStatus.RETURNED.toString());
            //assertNotEquals(order.getStatus(), OrderStatus.COMPLETED.toString());
        }
        verify(orderRepository).findCurrentOrdersByCustomerId(customerId);
    }

    @Test
    void testGetCurrentOrdersWithNoCurrentOrders()
    {
        // Arrange
        int customerId = 2;
        when(orderRepository.findCurrentOrdersByCustomerId(customerId)).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.getCurrentOrders(customerId);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("No current orders found for the customer.");
    }

    @Test
    public void testGetPastOrdersSuccess() {
        // Arrange
        int customerId = 1;

        // Create mock Order objects
        Order order1 = new Order(122345, 3, false);
        order1.setStatus(OrderStatus.CANCELLED);
        Order order2 = new Order(242466, 10, false);
        order2.setStatus(OrderStatus.CANCELLED);
        Order order3 = new Order(1232345, 3, false);
        order3.setStatus(OrderStatus.RETURNED);

        List<Order> orders = Arrays.asList(order1, order2, order3);

        // Mock the repository call to return the mock orders
        when(orderRepository.findPastOrdersByCustomerId(customerId)).thenReturn(orders);

        // Act
        List<OrderDTO> result = orderService.getPastOrders(customerId);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3); // Ensure two orders are returned

        for (int i = 0; i < 3; i++)
        {
            OrderDTO order = result.get(i);
            assertEquals(order.getId(), orders.get(i).getId());
            assertEquals(order.getCreationDate(), orders.get(i).getCreationDate());
            assertEquals(order.getRentalTime(), orders.get(i).getRentalTime());
            assertEquals(order.getRentalTime(), orders.get(i).getRentalTime());
            assertEquals(order.getStatus(), orders.get(i).getStatus().toString());
            assertNotEquals(order.getStatus(), OrderStatus.RECEIVED.toString());
            assertNotEquals(order.getStatus(), OrderStatus.READY_FOR_PICK_UP.toString());
        }
        verify(orderRepository).findPastOrdersByCustomerId(customerId);
    }

    @Test
    void testGetPastOrdersWithNoActiveOrders()
    {
        // Arrange
        int customerId = 2;
        when(orderRepository.findCurrentOrdersByCustomerId(customerId)).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.getPastOrders(customerId);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("No past orders found for the customer.");
    }

    @Test
    public void testGetAllOrdersWithOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(
                new Order(134355,3,false), // Initialize first Order with necessary values
                new Order(2454445,5, true),
                new Order(344567,10,true)// Initialize second Order with necessary values
        );

        // Mock the repository to return the list of orders
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<OrderDTO> result = orderService.getAllOrders();

        // Assert
        assertThat(result).isNotEmpty(); // Verify that the result is not empty
        assertThat(result).hasSize(3); // Verify the size of the result

        for (int i = 0; i < 3; i++)
        {
            OrderDTO order = result.get(i);
            assertEquals(order.getId(), orders.get(i).getId());
            assertEquals(order.getCreationDate(), orders.get(i).getCreationDate());
            assertEquals(order.getRentalTime(), orders.get(i).getRentalTime());
            assertEquals(order.getStatus(), orders.get(i).getStatus().toString());
        }

        // Verify that the repository method was called
        verify(orderRepository).findAll();
    }

    @Test
    void testGetAllOrdersWithNoeOrders()
    {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.getAllOrders();
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("No orders found.");
    }

    @Test
    public void testGetOrderByIdSuccess()
    {
        Order order = new Order(134355,3,false);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        OrderDTO result = orderService.getOrderById(order.getId());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(order.getId());
        assertThat(result.getCreationDate()).isEqualTo(order.getCreationDate());
        assertThat(result.getRentalTime()).isEqualTo(order.getRentalTime());
        assertThat(result.getStatus()).isEqualTo(order.getStatus().toString());
    }

    @Test
    void testGetOrderById_InvalidId()
    {
        // Arrange
        int invalidOrderId = 99;
        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.getOrderById(invalidOrderId);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Order not found");

        // Verify the repository method was called
        verify(orderRepository).findById(invalidOrderId);
    }

    @Test
    public void testDeleteOrderSuccess()
    {
        Order order = new Order(134355,3,false);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        orderService.deleteOrder(order.getId());
        verify(orderRepository).deleteById(order.getId());
    }

    @Test
    public void testDeleteNonExistingOrderThrowsException()
    {
        int invalidOrderId = 99;
        // Arrange
        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.deleteOrder(invalidOrderId);
        });
        assertEquals(exception.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(exception.getReason(), "Error!!! - Order associated with this id is not found in the database");
    }

    @Test
    public void testGetOrderByCustomerIdWithValidCustomerWithOrder()
    {
        Customer customer = new Customer("John", "Doe", "1234@test.com","");
        customer.setId(4);
        List<Order> orders = Arrays.asList(new Order(123345, 3,false), new Order(222456, 10, true)); // Initialize list of Orders
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(orderRepository.findOrderByCustomerId(4)).thenReturn(orders);

        List<OrderDTO> result = orderService.getOrderByCustomerId(4);
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(orders.get(0).getId());
        assertThat(result.get(0).getCreationDate()).isEqualTo(orders.get(0).getCreationDate());
        assertThat(result.get(0).getRentalTime()).isEqualTo(orders.get(0).getRentalTime());
        assertThat(result.get(0).getStatus()).isEqualTo(orders.get(0).getStatus().toString());

        assertThat(result.get(1).getId()).isEqualTo(orders.get(1).getId());
        assertThat(result.get(1).getCreationDate()).isEqualTo(orders.get(1).getCreationDate());
        assertThat(result.get(1).getRentalTime()).isEqualTo(orders.get(1).getRentalTime());
        assertThat(result.get(1).getStatus()).isEqualTo(orders.get(1).getStatus().toString());
    }

    @Test
    public void testGetOrderByCustomerIdWithValidCustomerWithoutOrder()
    {
        Customer customer = new Customer("John", "Doe", "1234@test.com", "");
        List<Order> orders = new ArrayList<>();
        customer.setId(4);
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(orderRepository.findOrderByCustomerId(customer.getId())).thenReturn(orders);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.getOrderByCustomerId(customer.getId());
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("No orders associated with this customer found");

        // Verify the repository method was called
        verify(orderRepository).findOrderByCustomerId(customer.getId());
    }

    @Test
    public void testGetOrderByCustomerId()
    {
        // Arrange
        int invalidCustomerId = 99;
        when(customerRepository.findById(invalidCustomerId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.getOrderByCustomerId(invalidCustomerId);
        });

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Customer not found");

        verify(customerRepository).findById(invalidCustomerId);
        verify(orderRepository, never()).findOrderByCustomerId(anyInt()); // Verify the order repository was not called
    }

    @Test
    public void testGetCustomerByOrderIdSuccess()
    {
        Customer customer = new Customer("John", "Doe", "1234@test.com", "");
        customer.setId(4);
        Order order = new Order(123345, 3, false);
        order.setCustomer(customer);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        CustomerDTO result = orderService.getCustomerByOrderId(order.getId());
        assertThat(result).isNotNull();
        assertEquals(result.getFirstName(), customer.getFirstName());
        assertEquals(result.getLastName(), customer.getLastName());
        assertEquals(result.getEmail(), customer.getEmail());
        assertEquals(result.getPhone(), customer.getPhone());
        verify(orderRepository).findById(order.getId());
    }

    @Test
    public void testGetCustomerByOrderIdWithNonExistingOrder()
    {
        int invalidOrderId = 99;
        when(customerRepository.findById(invalidOrderId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.getCustomerByOrderId(invalidOrderId);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Order not found");
        verify(orderRepository).findById(invalidOrderId);
    }

    @Test
    public void testGetCustomerByOrderIdWithInvalidCustomer()
    {
        Customer customer = new Customer("John", "Doe", "1234@test.com", "");
        customer.setId(4);
        Order order = new Order(123345, 3, false);
        order.setCustomer(customer);
        when(orderRepository.findById(anyInt())).thenReturn(Optional.of(new Order()));
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.getCustomerByOrderId(123445);
        });
       assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
       assertThat(exception.getReason()).isEqualTo("Customer not found");
    }

    @Test
    public void testReturnOrderSuccess()
    {
        Customer customer = new Customer("John", "Doe", "1234@test.com", "");
        Order order = new Order(123345, 3, false);
        order.setCustomer(customer);
        when(customerRepository.findById(order.getId())).thenReturn(Optional.of(customer));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        orderService.returnOrder(order.getId());
        assertEquals(order.getStatus(), OrderStatus.RETURNED);
        verify(orderRepository).save(order);
    }

    @Test
    public void testReturnOrderWithNonExistingOrder()
    {
        int invalidOrderId = 123445;
        when(customerRepository.findById(invalidOrderId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.returnOrder(invalidOrderId);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Error!!! - Order not found");
    }

    @Test
    public void testReturnOrderWithAlreadyReturnedOrder()
    {
        Customer customer = new Customer("John", "Doe", "1234@test.com", "");
        Order order = new Order(123345, 3, false);
        order.setStatus(OrderStatus.RETURNED);
        order.setCustomer(customer);
        when(customerRepository.findById(order.getId())).thenReturn(Optional.of(customer));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.returnOrder(order.getId());
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo("Order is already returned");
    }

    @Test
    public void testUpdateOrderValidRentalTimeUpdatesSuccessfully()
    {

        Order existingOrder = new Order(123345, 3, false);

        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setRentalTime(10);
        orderDTO.setStatus(existingOrder.getStatus().toString());

        // Act
        orderService.updateOrder(existingOrder.getId(), orderDTO);

        // Assert
        assertEquals(10, existingOrder.getRentalTime());
        verify(orderRepository).save(existingOrder);
    }

    @Test
    public void testUpdateOrderSameRentalTimeNoUpdate()
    {
        // Arrange
        Order existingOrder = new Order(232345,5,false);
        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setRentalTime(5);
        orderDTO.setStatus(existingOrder.getStatus().toString());

        // Act
        orderService.updateOrder(existingOrder.getId(), orderDTO);

        // Assert
        assertEquals(5, existingOrder.getRentalTime());
        verify(orderRepository, never()).save(existingOrder);
    }

    @Test
    public void testUpdateOrderZeroRentalTimeNoUpdate()
    {
        // Arrange
        Order existingOrder = new Order(122342, 3,false);
        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setRentalTime(0);

        // Act
        orderService.updateOrder(existingOrder.getId(), orderDTO);

        // Assert
        assertEquals(5, existingOrder.getRentalTime());
        verify(orderRepository, never()).save(existingOrder);
    }

    @Test
    public void testUpdateOrderValidPaidStatusUpdatesSuccessfully()
    {
        // Arrange
        Order existingOrder = new Order(122345, 3, false);

        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setPaid(true);

        // Act
        orderService.updateOrder(existingOrder.getId(), orderDTO);

        // Assert
        assertTrue(existingOrder.isPaid());
        verify(orderRepository).save(existingOrder);
    }

    @Test
    public void testUpdateOrder_SamePaidStatus_NoUpdate()
    {
        // Arrange
        Order existingOrder = new Order(121212,10,true);

        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setPaid(existingOrder.isPaid());

        // Act
        orderService.updateOrder(existingOrder.getId(), orderDTO);

        // Assert
        assertFalse(existingOrder.isPaid());
        verify(orderRepository, never()).save(existingOrder);
    }

    @Test
    public void testUpdateOrder_ValidStatus_UpdateSuccessfully()
    {
        // Arrange
        Order existingOrder = new Order();

        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("RETURNED");

        // Act
        orderService.updateOrder(existingOrder.getId(), orderDTO);

        // Assert
        assertEquals(OrderStatus.RETURNED, existingOrder.getStatus());
        verify(orderRepository).save(existingOrder);
    }

    @Test
    public void testUpdateOrder_UpdateToCancelledStatus_Conflict()
    {
        // Arrange
        Order existingOrder = new Order();
        existingOrder.setStatus(OrderStatus.RETURNED);

        when(orderRepository.findById(1)).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("RECEIVED");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.updateOrder(1, orderDTO);
        });
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Order is already returned or cancelled", exception.getReason());
    }

    @Test
    public void testUpdateOrder_OrderNotFound_NotFound()
    {
        // Arrange
        when(orderRepository.findById(99)).thenReturn(Optional.empty());
        OrderDTO orderDTO = new OrderDTO();
        // Act & Assert
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> {
            orderService.updateOrder(99, orderDTO);
        });
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertEquals("ERROR!!! - Order not found", thrown.getReason());
    }

    @Test
    public void testUpdateOrder_InvalidStatus_BadRequest()
    {
        // Arrange
        Order existingOrder = new Order();

        when(orderRepository.findById(1)).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("INVALID");

        // Act & Assert
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> {
            orderService.updateOrder(1, orderDTO);
        });
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
        assertEquals("Invalid order status", thrown.getReason());
    }

    @Test
    public void testUpdateOrder_NullStatus_NoUpdate()
    {
        // Arrange
        Order existingOrder = new Order();

        when(orderRepository.findById(1)).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus(null);

        // Act
        orderService.updateOrder(1, orderDTO);

        // Assert
        assertEquals(OrderStatus.RECEIVED, existingOrder.getStatus());
        verify(orderRepository, never()).save(existingOrder);
    }
}
