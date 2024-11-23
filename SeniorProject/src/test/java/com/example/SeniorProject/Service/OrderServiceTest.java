package com.example.SeniorProject.Service;

import com.example.SeniorProject.DTOs.*;
import com.example.SeniorProject.Email.*;
import com.example.SeniorProject.Model.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.*;
import java.util.*;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.SeniorProject.Model.Order;
import org.springframework.dao.*;
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

    @Mock
    private GoogleMapService googleMapService;
    @Mock
    private PaymentService paymentService;


    @InjectMocks
    private OrderService orderService;

    private static final int CUSTOMER_ID = 1;
    private static final int ORDER_ID = 100;
    private static final double DELIVERY_FEE = 15.00;

    @BeforeEach
    public void setup()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_SuccessfulWithoutDelivery()
    {
        // Arrange
        Customer customer = createCustomer();
        Product product = createProduct(10);
        OrderDTO orderDTO = createOrderDTO();


        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        OrderProductDTO orderProductDTO = new OrderProductDTO(2, productDTO);
        Set<OrderProductDTO> orderProductDTOSet = new HashSet<>();
        orderProductDTOSet.add(orderProductDTO);
        orderDTO.setOrderProducts(orderProductDTOSet);

        Set<OrderProduct> orderProducts = new HashSet<>();
        orderProducts.add(createOrderProduct(product, 2));
        Order mockOrder = createOrder(OrderStatus.RECEIVED, customer, orderProducts);

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(orderProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderDTO result = orderService.createOrder(CUSTOMER_ID, orderDTO);

        // Assert
        assertNotNull(result);
        verify(customerRepository).findById(CUSTOMER_ID);
        verify(productRepository).findById(product.getId());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderProductRepository).saveAll(anySet());

        assertEquals(200.00, result.getPrice());
        assertEquals(40.00, result.getDeposit());
        assertEquals(14.50, result.getTax());
        assertEquals(0.00, result.getDeliveryFee());
        assertEquals(254.50, result.getSubtotal());
    }

    @Test
    void createOrder_SuccessfulWithDelivery()
    {
        // Arrange
        Customer customer = createCustomer();
        Product deliveryProduct = createDeliveryProduct(5);
        OrderDTO orderDTO = createOrderDTO();
        orderDTO.setAddress("123 Test St"); // Set a valid address

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(deliveryProduct.getId());
        OrderProductDTO orderProductDTO = new OrderProductDTO(1, productDTO);
        Set<OrderProductDTO> orderProductsDTO = new HashSet<>();
        orderProductsDTO.add(orderProductDTO);
        orderDTO.setOrderProducts(orderProductsDTO);

        Set<OrderProduct> orderProducts = new HashSet<>();
        orderProducts.add(createOrderProduct(deliveryProduct, 1));
        Order mockOrder = createOrder(OrderStatus.RECEIVED, customer, orderProducts);

        String testPlaceId = "TEST_PLACE_ID";
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(productRepository.findById(deliveryProduct.getId())).thenReturn(Optional.of(deliveryProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(orderProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(googleMapService.getPlaceId("123 Test St")).thenReturn(testPlaceId);
        when(googleMapService.calculateDeliveryFee(testPlaceId)).thenReturn(DELIVERY_FEE);

        // Act
        OrderDTO result = orderService.createOrder(CUSTOMER_ID, orderDTO);

        // Assert
        assertNotNull(result);
        verify(customerRepository).findById(CUSTOMER_ID);
        verify(productRepository).findById(deliveryProduct.getId());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderProductRepository).saveAll(anySet());
        assertEquals(150.0, result.getPrice());
        assertEquals(30.00, result.getDeposit());
        assertEquals(Math.round(result.getPrice() * 0.0725 * 100.0) / 100.0, result.getTax());
        assertEquals(result.getPrice() + result.getDeposit() + result.getTax() + result.getDeliveryFee(), result.getSubtotal());
    }

    @Test
    void createOrder_CustomerNotFound()
    {
        // Arrange
        OrderDTO orderDTO = createOrderDTO();
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.createOrder(CUSTOMER_ID, orderDTO));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Customer not found"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ProductNotFound()
    {
        // Arrange
        Customer customer = createCustomer();
        OrderDTO orderDTO = createOrderDTO();

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(999);
        OrderProductDTO orderProductDTO = new OrderProductDTO(1, productDTO);
        Set<OrderProductDTO> orderProductsDTO = new HashSet<>();
        orderProductsDTO.add(orderProductDTO);
        orderDTO.setOrderProducts(orderProductsDTO);

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrder(CUSTOMER_ID, orderDTO)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Product not found"));
    }

    @Test
    public void testCreateOrderWithInsufficientProductQuantity()
    {
        ProductDTO productDTO = new ProductDTO(1, "Laptop", 1500.0, 4, "Electronics", "A high-end laptop", false);
        OrderProductDTO orderProductDTO = new OrderProductDTO(10, productDTO);
        OrderDTO orderDTO = new OrderDTO(LocalDate.now(), LocalDate.now(), LocalDate.now(), true, OrderStatus.RECEIVED, "1234 test Ave, Sacramento CA 99999", 0, 0, 0, 0, 0);
        orderDTO.setOrderProducts(Set.of(orderProductDTO));
        Product product = new Product(productDTO.getQuantity(), productDTO.getPrice(), productDTO.getType(), productDTO.getName(), productDTO.getDescription());

        when(customerRepository.findById(1)).thenReturn(Optional.of(new Customer()));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.createOrder(1, orderDTO));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("ERROR!!! - Insufficient product quantity.", exception.getReason());
    }

    @Test
    void testOrderCancelledByCustomer_Success()
    {
        // Given
        int orderId = 1;
        Order order = new Order();
        Customer customer = new Customer();
        order.setCustomer(customer);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mocking payment service call
        Map<String, Object> dummyRefundMap = new HashMap<>();
        dummyRefundMap.put("id", "dummy_id");
        dummyRefundMap.put("amount", 1000L);
        dummyRefundMap.put("currency", "usd");
        dummyRefundMap.put("status", "succeeded");
        dummyRefundMap.put("created", 123456789L);

        try
        {
            when(paymentService.refundAllExceptDeposit(orderId)).thenReturn(dummyRefundMap);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        // When
        orderService.orderCancelledByCustomer(orderId);

        // Then
        verify(orderRepository).findById(orderId);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(order.getCustomer(), customer);
        try
        {
            verify(paymentService).refundAllExceptDeposit(orderId);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        verify(orderRepository).save(order); // Assuming cancelOrder calls save()
    }

    @Test
    void testOrderCancelledByAdmin_Success() throws Exception
    {
        // Given
        int orderId = 1;
        Order order = new Order();
        Customer customer = new Customer();
        order.setCustomer(customer);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mocking payment service call
        Map<String, Object> dummyRefundMap = new HashMap<>();
        dummyRefundMap.put("id", "dummy_id");
        dummyRefundMap.put("amount", 1000L);
        dummyRefundMap.put("currency", "usd");
        dummyRefundMap.put("status", "succeeded");
        dummyRefundMap.put("created", 123456789L);

        when(paymentService.refundAll(orderId)).thenReturn(dummyRefundMap);

        // When
        orderService.orderCancelledByAdmin(orderId);

        // Then
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(order.getCustomer(), customer);
        verify(orderRepository).findById(orderId);
        verify(paymentService).refundAll(orderId);
        verify(orderRepository).save(order); // Assuming cancelOrder calls save()
    }

    @Test
    void testOrderCancelledByCustomer_Failure()
    {
        // Given
        int orderId = 1;
        Order order = new Order();
        Customer customer = new Customer();
        order.setCustomer(customer);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mocking payment service to throw an exception
        try
        {
            doThrow(new RuntimeException("Payment failed")).when(paymentService).refundAllExceptDeposit(orderId);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        // When / Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.orderCancelledByCustomer(orderId));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("ERROR!!! - something went wrong when processing return payment", exception.getReason());
    }

    @Test
    void testOrderCancelledByAdmin_Failure()
    {
        // Given
        int orderId = 1;
        Order order = new Order();
        Customer customer = new Customer();
        order.setCustomer(customer);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        // Mocking payment service to throw an exception
        try
        {
            doThrow(new RuntimeException("Payment failed")).when(paymentService).refundAll(orderId);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        // When / Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.orderCancelledByAdmin(orderId));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("ERROR!!! - something went wrong when processing return payment", exception.getReason());
    }

    @Test
    void testValidStatusUpdate()
    {
        // Arrange
        int orderId = 1;
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("CONFIRMED");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.RECEIVED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        orderService.updateOrderStatus(orderId, orderDTO);

        // Assert
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testOrderNotFound()
    {
        // Arrange
        int orderId = 999;
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("CONFIRMED");

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.updateOrderStatus(orderId, orderDTO);
        });

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
        assert exception.getStatusCode() == HttpStatus.NOT_FOUND;
        assert exception.getReason().equals("ERROR!!! - Order not found");
    }

    @Test
    void testNullStatusInOrderDTO()
    {
        // Arrange
        int orderId = 1;
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus(null);

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.RECEIVED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.updateOrderStatus(orderId, orderDTO);
        });

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("ERROR!!! - trying to updated order status to null status", exception.getReason());
    }

    @Test
    void testInvalidOrderStatusInOrderDTO()
    {
        // Arrange
        int orderId = 1;
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("INVALID_STATUS");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.RECEIVED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.updateOrderStatus(orderId, orderDTO);
        });

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
        assertEquals(exception.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals("ERROR!!! - Invalid order status", exception.getReason());
    }

    @Test
    void testOrderAlreadyInTargetStatus()
    {
        // Arrange
        int orderId = 1;
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("RECEIVED");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.RECEIVED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.updateOrderStatus(orderId, orderDTO);
        });

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
        assertEquals(exception.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals("ERROR!!! - Order already has the status you want to updated to", exception.getReason());
    }

    @Test
    void testStatusUpdateNotAllowed()
    {
        // Arrange
        int orderId = 1;
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("CONFIRMED");

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.updateOrderStatus(orderId, orderDTO);
        });

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
        assert exception.getStatusCode() == HttpStatus.CONFLICT;
        assert exception.getReason().equals("Order has already been completed");
    }

    @Test
    public void cancelExistingOrder()
    {
        Customer customer = new Customer();
        //    public Order(int id, LocalDate pickupDate, LocalDate returnDate, boolean paid, String address)
        Order order = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
        order.setCustomer(customer);
        when(orderRepository.findById(122345)).thenReturn(Optional.of(order));
        orderService.cancelOrder(122345);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
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
        Order order = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
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
        Order order1 = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
        order1.setStatus(OrderStatus.READY_FOR_PICK_UP);
        Order order2 = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
        Order order3 = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");

        List<Order> orders = Arrays.asList(order1, order2, order3);

        // Mock the repository call to return the mock orders
        when(orderRepository.findCurrentOrdersByCustomerId(customerId)).thenReturn(orders);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(new Customer()));

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
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(new Customer()));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.getCurrentOrders(customerId);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("No current orders found for the customer.");
    }

    @Test
    public void testGetCurrentOrderInvalidCustomer()
    {
        int customerId = 3;
        when(orderRepository.findCurrentOrdersByCustomerId(customerId)).thenReturn(Collections.emptyList());
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.getCurrentOrders(customerId);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("ERROR!!! - Customer not found");
    }

    @Test
    public void testGetPastOrdersSuccess()
    {
        // Arrange
        int customerId = 1;

        // Create mock Order objects
        Order order1 = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
        order1.setStatus(OrderStatus.CANCELLED);
        Order order2 = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
        order2.setStatus(OrderStatus.CANCELLED);
        Order order3 = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
        order3.setStatus(OrderStatus.RETURNED);

        List<Order> orders = Arrays.asList(order1, order2, order3);

        // Mock the repository call to return the mock orders
        when(orderRepository.findPastOrdersByCustomerId(customerId)).thenReturn(orders);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(new Customer()));

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
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(new Customer()));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.getPastOrders(customerId);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("No past orders found for the customer.");
    }

    @Test
    public void testGetPastOrderInvalidCustomer()
    {
        int customerId = 3;
        when(orderRepository.findCurrentOrdersByCustomerId(customerId)).thenReturn(Collections.emptyList());
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.getCurrentOrders(customerId);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("ERROR!!! - Customer not found");
    }

    @Test
    void testCustomerNotFound()
    {
        int customerId = 1;
        when(customerRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> orderService.getPastOrders(1));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {orderService.getCurrentOrders(customerId);});
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("ERROR!!! - Customer not found");
    }

    @Test
    void testNoPastOrdersFound()
    {
        when(customerRepository.findById(anyInt())).thenReturn(Optional.of(new Customer()));
        when(orderRepository.findPastOrdersByCustomerId(anyInt())).thenReturn(Collections.emptyList());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.getPastOrders(1));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("No past orders found for the customer.", exception.getReason());
    }

    @Test
    void testValidCustomerWithPastOrders()
    {
        Customer customer = new Customer(); // Add details if necessary
        Order order = new Order(); // Add details if necessary
        when(customerRepository.findById(anyInt())).thenReturn(Optional.of(customer));
        when(orderRepository.findPastOrdersByCustomerId(anyInt())).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getPastOrders(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        // Add further assertions for mapped DTOs if required
    }

    @Test
    void testRepositoryExceptions()
    {
        when(customerRepository.findById(anyInt())).thenThrow(RuntimeException.class);
        assertThrows(RuntimeException.class, () -> orderService.getPastOrders(1));
    }

    @Test
    public void testGetAllOrdersWithOrders()
    {
        // Arrange
        List<Order> orders = Arrays.asList(
                new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999"),// Initialize first Order with necessary values
                new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999"),
                new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999"));

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
        Order order = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        OrderDTO result = orderService.getOrderById(order.getId());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(order.getId());
        assertThat(result.getCreationDate()).isEqualTo(order.getCreationDate());
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
    public void testGetOrderByCustomerIdWithValidCustomerWithOrder()
    {
        Customer customer = new Customer("John", "Doe", "1234@test.com", "");
        customer.setId(4);
        List<Order> orders = Arrays.asList(new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999"), new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999"));
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(orderRepository.findOrderByCustomerId(4)).thenReturn(orders);

        List<OrderDTO> result = orderService.getOrderByCustomerId(4);
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(orders.get(0).getId());
        assertThat(result.get(0).getCreationDate()).isEqualTo(orders.get(0).getCreationDate());
        assertThat(result.get(0).getStatus()).isEqualTo(orders.get(0).getStatus().toString());

        assertThat(result.get(1).getId()).isEqualTo(orders.get(1).getId());
        assertThat(result.get(1).getCreationDate()).isEqualTo(orders.get(1).getCreationDate());
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
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
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
        Order order = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
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
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
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
        Order order = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
        order.setCustomer(customer);
        when(orderRepository.findById(anyInt())).thenReturn(Optional.of(new Order()));
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.getCustomerByOrderId(123445);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Customer not found");
    }

    @Test
    public void testReturnOrderSuccess()
    {
        Customer customer = new Customer("John", "Doe", "1234@test.com", "");
        Order order = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
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
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.returnOrder(invalidOrderId);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Error!!! - Order not found");
    }

    @Test
    public void testReturnOrderWithAlreadyReturnedOrder()
    {
        Customer customer = new Customer("John", "Doe", "1234@test.com", "");
        Order order = new Order(122345, LocalDate.now(), LocalDate.now(), false, "1234 test Ave, Sacramento CA 99999");
        order.setStatus(OrderStatus.RETURNED);
        order.setCustomer(customer);
        when(customerRepository.findById(order.getId())).thenReturn(Optional.of(customer));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.returnOrder(order.getId());
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo("Order has already been returned");
    }

    @Test
    public void testUpdateOrder_ValidStatusUpdateSuccessfully()
    {
        // Arrange
        Order existingOrder = new Order();

        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("RETURNED");

        // Act
        orderService.updateOrderStatus(existingOrder.getId(), orderDTO);

        // Assert
        assertEquals(OrderStatus.RETURNED, existingOrder.getStatus());
        verify(orderRepository).save(existingOrder);
    }

    @Test
    public void testUpdateOrderUpdateToReturnedStatusConflict()
    {
        // Arrange
        Order existingOrder = new Order();
        existingOrder.setStatus(OrderStatus.RETURNED);

        when(orderRepository.findById(1)).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("RECEIVED");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.updateOrderStatus(1, orderDTO);
        });
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Order has already been returned", exception.getReason());
    }

    @Test
    public void testUpdateOrder_OrderNotFoundNotFound()
    {
        // Arrange
        when(orderRepository.findById(99)).thenReturn(Optional.empty());
        OrderDTO orderDTO = new OrderDTO();
        // Act & Assert
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.updateOrderStatus(99, orderDTO);
        });
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertEquals("ERROR!!! - Order not found", thrown.getReason());
    }

    @Test
    public void testUpdateOrder_InvalidStatusBadRequest()
    {
        // Arrange
        Order existingOrder = new Order();

        when(orderRepository.findById(1)).thenReturn(Optional.of(existingOrder));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("INVALID");

        // Act & Assert
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.updateOrderStatus(1, orderDTO);
        });
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
        assertEquals("ERROR!!! - Invalid order status", thrown.getReason());
    }

    @Test
    void testOrderAlreadyCancelled()
    {
        // Given
        Order existingOrder = new Order();
        existingOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(anyInt())).thenReturn(Optional.of(existingOrder));

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("CONFIRMED");

        // When / Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.updateOrderStatus(1, orderDTO));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Order has already been cancelled", exception.getReason());
    }

    @Test
    void testOrderAlreadyReturned()
    {
        // Given
        Order existingOrder = new Order();
        existingOrder.setStatus(OrderStatus.RETURNED);
        when(orderRepository.findById(anyInt())).thenReturn(Optional.of(existingOrder));

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("CONFIRMED");

        // When / Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.updateOrderStatus(1, orderDTO));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Order has already been returned", exception.getReason());
    }

    @Test
    void testOrderAlreadyRefunded()
    {
        // Given
        Order existingOrder = new Order();
        existingOrder.setStatus(OrderStatus.REFUNDED);
        when(orderRepository.findById(anyInt())).thenReturn(Optional.of(existingOrder));

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("CONFIRMED");

        // When / Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.updateOrderStatus(1, orderDTO));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Order has already been refunded", exception.getReason());
    }

    @Test
    void testOrderAlreadyCompleted()
    {
        // Given
        Order existingOrder = new Order();
        existingOrder.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(anyInt())).thenReturn(Optional.of(existingOrder));

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStatus("CONFIRMED");

        // When / Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.updateOrderStatus(1, orderDTO));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Order has already been completed", exception.getReason());
    }

    @Test
    void testOrderDueCheckWithOrders()
    {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Customer customer = new Customer();
        customer.setEmail("test@example.com");
        customer.setFirstName("John");
        customer.setLastName("Doe");

        Order order = new Order();
        order.setId(1);
        order.setCustomer(customer);

        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findReturnOrders(tomorrow)).thenReturn(orders);

        // Act
        orderService.orderDueCheck();

        // Assert
        verify(orderRepository, times(1)).findReturnOrders(tomorrow);
        verify(emailService, times(1)).sendSimpleEmail(argThat(emailDetails ->
                emailDetails.getRecipient().equals("test@example.com") &&
                        emailDetails.getSubject().equals("Order Return") &&
                        emailDetails.getMessageBody().contains("John Doe") &&
                        emailDetails.getMessageBody().contains("1")
        ));
    }

    @Test
    void testOrderDueCheckWithNoOrders()
    {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(orderRepository.findReturnOrders(tomorrow)).thenReturn(Collections.emptyList());

        // Act
        orderService.orderDueCheck();

        // Assert
        verify(orderRepository, times(1)).findReturnOrders(tomorrow);
        verify(emailService, never()).sendSimpleEmail(any(EmailDetails.class));
    }

    @Test
    void testOrderDueCheckWithNullCustomer()
    {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Order order = new Order();
        order.setId(2);
        order.setCustomer(null);

        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findReturnOrders(tomorrow)).thenReturn(orders);

        // Act
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.orderDueCheck();
        });
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertEquals("Customer not found", thrown.getReason());

        // Assert
        verify(orderRepository, times(1)).findReturnOrders(tomorrow);
        verify(emailService, never()).sendSimpleEmail(any(EmailDetails.class));
    }

    @Test
    void testOrderDueCheckWithNullCustomerEmail()
    {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Customer customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setEmail(null);

        Order order = new Order();
        order.setId(3);
        order.setCustomer(customer);

        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findReturnOrders(tomorrow)).thenReturn(orders);

        // Act
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.orderDueCheck();
        });
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertEquals("Customer email not found", thrown.getReason());

        // Assert
        verify(orderRepository, times(1)).findReturnOrders(tomorrow);
        verify(emailService, never()).sendSimpleEmail(any(EmailDetails.class));
    }

    @Test
    void deleteOrder_Success()
    {
        // Create test data
        Product product = createProduct(10);
        Customer customer = createCustomer();
        OrderProduct orderProduct = createOrderProduct(product, 2);
        Order order = createOrder(
                OrderStatus.COMPLETED,
                customer,
                new HashSet<>(Collections.singleton(orderProduct))
        );

        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        // Act
        orderService.deleteOrder(1);

        // Assert
        verify(productRepository).save(argThat(savedProduct ->
                savedProduct.getQuantity() == 12
        ));
        verify(orderRepository).save(argThat(savedOrder ->
                savedOrder.getCustomer() == null
        ));
        verify(orderProductRepository).deleteAll(order.getOrderProducts());
        verify(orderRepository).delete(order);
    }

    @Test
    void testDeleteOrder_MultipleProducts()
    {
        // Create test data
        Product product1 = createProduct(10);
        Product product2 = createProduct(5);

        OrderProduct orderProduct1 = createOrderProduct(product1, 2);
        OrderProduct orderProduct2 = createOrderProduct(product2, 3);

        Set<OrderProduct> orderProducts = new HashSet<>();
        orderProducts.add(orderProduct1);
        orderProducts.add(orderProduct2);

        Order order = createOrder(OrderStatus.COMPLETED, null, orderProducts);

        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        // Act
        orderService.deleteOrder(1);

        // Assert
        // Capture all saved products to verify their quantities
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(2)).save(productCaptor.capture());

        List<Product> savedProducts = productCaptor.getAllValues();
        assertThat(savedProducts)
                .extracting(Product::getQuantity)
                .containsExactlyInAnyOrder(12, 8);

        verify(orderProductRepository).deleteAll(order.getOrderProducts());
        verify(orderRepository).delete(order);
    }

    @Test
    void testDeleteOrderEmptyOrderProducts()
    {
        // Create test data
        Order order = createOrder(
                OrderStatus.COMPLETED,
                null,
                new HashSet<>()
        );

        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        // Act
        orderService.deleteOrder(1);

        // Assert
        verify(productRepository, never()).save(any());
        verify(orderProductRepository).deleteAll(emptySet());
        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrder_OrderNotFound()
    {
        // Arrange
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.deleteOrder(999)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testDeleteOrderProductNotFound()
    {
        // Create test data
        OrderProduct orderProduct = createOrderProduct(null, 2);
        Order order = createOrder(
                OrderStatus.COMPLETED,
                null,
                new HashSet<>(Collections.singleton(orderProduct))
        );

        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.deleteOrder(1)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("ERROR!!! - Product not found", exception.getReason());
    }

    @Test
    void testDeleteOrder_DataIntegrityViolation()
    {
        Product product = createProduct(10);
        OrderProduct orderProduct = createOrderProduct(product, 2);
        Order order = createOrder(OrderStatus.COMPLETED, null, new HashSet<>(Collections.singleton(orderProduct)));

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        doThrow(new DataIntegrityViolationException("FK constraint")).when(orderRepository).delete(any());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.deleteOrder(1));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Cannot delete order due to foreign key constraints. Ensure all related entities are properly handled.", exception.getReason());
    }

    @Test
    void testDeleteOrderZeroQuantity()
    {
        Product product = createProduct(10);
        OrderProduct orderProduct = createOrderProduct(product, 0);
        Order order = createOrder(OrderStatus.COMPLETED, null, new HashSet<>(Collections.singleton(orderProduct)));

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.deleteOrder(1));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("ERROR!!! - Insufficient product quantity in order, check to see if it hasn't been processed already.", exception.getReason());
    }

    @Test
    void testGenerateOrderInvoice_OrderIsNull()
    {
        // Expect exception
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            orderService.generateOrderInvoice(null);
        });

        // Assert exception details
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("ERROR!!! - Order not found", exception.getReason());
    }

    @Test
    void testGenerateOrderInvoice_ValidOrder()
    {
        // Given
        Order order = new Order();
        Customer customer = new Customer();
        order.setCustomer(customer);

        // Mock PDF generation
        doNothing().when(pdfService).generateInvoicePDF(any());

        // When
        orderService.generateOrderInvoice(order);

        // Then
        Map<String, Object> expectedModel = new HashMap<>();
        expectedModel.put("order", order);
        expectedModel.put("customer", customer);

        verify(pdfService).generateInvoicePDF(expectedModel);
    }

    @Test
    void testGenerateOrderInvoice_OrderHasNoCustomer()
    {
        // Given
        Order order = new Order();
        order.setCustomer(null);

        // Mock PDF generation
        doNothing().when(pdfService).generateInvoicePDF(any());

        // When
        orderService.generateOrderInvoice(order);

        // Then
        Map<String, Object> expectedModel = new HashMap<>();
        expectedModel.put("order", order);
        expectedModel.put("customer", null);

        verify(pdfService).generateInvoicePDF(expectedModel);
    }

    private Order createOrder(OrderStatus status, Customer customer, Set<OrderProduct> orderProducts)
    {
        Order order = new Order();
        order.setId(1);
        order.setStatus(status);
        order.setCustomer(customer);
        order.setOrderProducts(orderProducts);
        return order;
    }

    private Customer createCustomer()
    {
        return new Customer();
    }

    private Product createProduct(int initialQuantity)
    {
        Product product = new Product();
        product.setId(UUID.randomUUID().hashCode() & Integer.MAX_VALUE);
        product.setQuantity(initialQuantity);
        product.setPrice(100.00);
        product.setDeposit(20.00);
        return product;
    }

    private Product createDeliveryProduct(int initialQuantity)
    {
        Product product = createProduct(initialQuantity);
        product.setDeliverOnly(true);
        product.setPrice(150.00);
        product.setDeposit(30.00);
        return product;
    }

    private OrderProduct createOrderProduct(Product product, int quantity)
    {
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setProduct(product);
        orderProduct.setQuantity(quantity);
        return orderProduct;
    }

    private OrderDTO createOrderDTO()
    {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setPickupDate(LocalDate.now());
        orderDTO.setReturnDate(LocalDate.now().plusDays(7));
        orderDTO.setPaid(true);
        orderDTO.setAddress("123 Test St");
        return orderDTO;
    }
}