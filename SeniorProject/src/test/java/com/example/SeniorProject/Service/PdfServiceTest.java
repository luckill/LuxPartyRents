package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.springframework.core.io.ByteArrayResource;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PdfServiceTest {
        @InjectMocks
        private PdfService pdfService;

        @Mock
        private S3Service s3Service;

        @Mock
        private Order order;

        @Mock
        private Customer customer;

        @Mock
        private OrderProduct orderProduct;

        private Set<OrderProduct> orderProducts;
        private java.util.Map<String, Object> model;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);

                // Initialize order products set
                orderProducts = new HashSet<>();
                orderProducts.add(orderProduct);

                // Setup model with mocked order and customer
                model = new java.util.HashMap<>();
                model.put("order", order);
                model.put("customer", customer);

                // Set up mock data for the order and customer
                when(order.getId()).thenReturn(123);
                when(order.getCreationDate()).thenReturn(LocalDate.now());
                when(order.getPrice()).thenReturn(100.00);
                when(order.getTax()).thenReturn(7.25);
                when(order.getDeposit()).thenReturn(50.00);
                when(order.getDeliveryFee()).thenReturn(20.00);
                when(order.getSubtotal()).thenReturn(100.00);

                when(customer.getFirstName()).thenReturn("John");
                when(customer.getLastName()).thenReturn("Doe");
                when(customer.getPhone()).thenReturn("123-456-7890");
                when(customer.getEmail()).thenReturn("john.doe@example.com");
        }

        @Test
        void testGenerateInvoicePDF_Successfully() {
                // Arrange: Create mock Product
                Product product = mock(Product.class); // Mock product
                when(product.getName()).thenReturn("Laptop");  // Mock method to return product name
                when(product.getPrice()).thenReturn(50.0);    // Mock price for product

                // Arrange: Create mock OrderProduct linked to the order and product
                OrderProduct orderProduct = mock(OrderProduct.class);  // Mock OrderProduct
                when(orderProduct.getProduct()).thenReturn(product);  // Mock getProduct() to return mocked product
                when(orderProduct.getQuantity()).thenReturn(2);       // Mock quantity for the orderProduct

                // Create a set to hold OrderProducts
                Set<OrderProduct> orderProducts = new HashSet<>();
                orderProducts.add(orderProduct);  // Add the mocked orderProduct

                // Mock the behavior of order to return the mocked orderProducts
                when(order.getOrderProducts()).thenReturn(orderProducts);

                // Arrange: Mock other properties of order
                when(order.getId()).thenReturn(123);           // Mock order ID
                when(order.getCreationDate()).thenReturn(LocalDate.now()); // Mock order creation date
                when(order.getPrice()).thenReturn(100.00);     // Mock order price
                when(order.getTax()).thenReturn(7.25);         // Mock order tax
                when(order.getDeposit()).thenReturn(50.00);    // Mock order deposit
                when(order.getDeliveryFee()).thenReturn(20.00); // Mock order delivery fee
                when(order.getSubtotal()).thenReturn(100.00);  // Mock order subtotal

                // Mock the customer details
                Customer customer = mock(Customer.class); // Mock customer
                when(customer.getFirstName()).thenReturn("John");
                when(customer.getLastName()).thenReturn("Doe");
                when(customer.getPhone()).thenReturn("123-456-7890");
                when(customer.getEmail()).thenReturn("john.doe@example.com");

                // Add the customer to the model
                Map<String, Object> model = new HashMap<>();
                model.put("order", order);
                model.put("customer", customer);

                // Act: Call the generateInvoicePDF method
                File result = pdfService.generateInvoicePDF(model);

                // Assert: Verify the result and that the expected methods were called
                assertNotNull(result);  // Verify that the result is not null (i.e., the PDF was generated)
                verify(s3Service, times(1)).uploadOrderInvoice(any(File.class), eq(123));
        }

        @Test
        void testGenerateInvoicePDF_ShouldThrowRuntimeException() throws IOException {
                // Arrange
                Map<String, Object> model = new HashMap<>();
                model.put("order", order);
                model.put("customer", customer);

                // Mock the behavior of order and customer
                when(order.getId()).thenReturn(123);
                when(order.getCreationDate()).thenReturn(java.time.LocalDate.now());
                when(order.getPrice()).thenReturn(100.00);
                when(order.getTax()).thenReturn(7.25);
                when(order.getDeposit()).thenReturn(50.00);
                when(order.getDeliveryFee()).thenReturn(20.00);
                when(order.getSubtotal()).thenReturn(100.00);

                // Simulate an IOException by throwing a RuntimeException with the message "Failed to save PDF"
                doThrow(new RuntimeException("Error generating PDF")).when(s3Service).uploadOrderInvoice(any(File.class), eq(123));

                // Act & Assert
                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        pdfService.generateInvoicePDF(model);  // This should throw an exception
                });

                // Assert that the exception message is the wrapped one ("Error generating PDF")
                assertEquals("Error generating PDF", exception.getMessage());
        }

}