package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Configuration.TestSecurityConfig;
import com.example.SeniorProject.DTOs.ProductDTO;
import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class ProductControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ProductService productService;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                // Mock authentication with 'ADMIN' role
                SecurityContextHolder.setContext(new org.springframework.security.core.context.SecurityContextImpl(
                        new UsernamePasswordAuthenticationToken("admin", "password",
                                AuthorityUtils.createAuthorityList("ADMIN"))
                ));
        }


        @Test
        @WithMockUser(roles = "ADMIN")
        void testAddProduct_Success() throws Exception {
                // Arrange
                ProductDTO productDTO = new ProductDTO();
                productDTO.setName("Test Product");
                productDTO.setPrice(100.0);

                Product savedProduct = new Product();
                savedProduct.setId(1);
                savedProduct.setName("Test Product");
                savedProduct.setPrice(100.0);

                when(productService.addProduct(any(ProductDTO.class))).thenReturn(savedProduct);

                // Act and Assert
                mockMvc.perform(post("/product/addProduct")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(productDTO)))
                        .andExpect(status().isOk());  // HTTP 200 OK
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testAddProduct_NoContent() throws Exception {
                // Arrange
                ProductDTO productDTO = new ProductDTO();
                productDTO.setName("Test Product");
                productDTO.setPrice(100.0);

                when(productService.addProduct(any(ProductDTO.class))).thenReturn(null);

                // Act and Assert
                mockMvc.perform(post("/product/addProduct")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(productDTO)))
                        .andExpect(status().isNoContent());  // HTTP 204 No Content
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testAddProduct_Exception() throws Exception {
                // Arrange
                ProductDTO productDTO = new ProductDTO();
                productDTO.setName("Test Product");
                productDTO.setPrice(100.0);

                when(productService.addProduct(any(ProductDTO.class)))
                        .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input"));

                // Act and Assert
                mockMvc.perform(post("/product/addProduct")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(productDTO)))
                        .andExpect(status().isBadRequest())  // HTTP 400 Bad Request
                        .andExpect(content().string("Invalid input"));
        }

        @Test
        void testGetAllProducts() throws Exception {
                // Prepare some sample products
                Product product1 = new Product(1, 100, "Type A", "Product 1", "Description 1");
                Product product2 = new Product(2, 200, "Type B", "Product 2", "Description 2");

                List<Product> products = Arrays.asList(product1, product2);  // List of products

                // Mock the service call to return the list of products
                when(productService.getAllProducts(null, null, null)).thenReturn(products);

                // Perform GET request to "/product/getAll" and check response
                mockMvc.perform(get("/product/getAll")
                                .contentType(MediaType.APPLICATION_JSON))  // No query parameters
                        .andExpect(status().isOk())  // Expect status 200 OK
                        .andExpect(jsonPath("$.length()").value(2))  // Verify the number of products
                        .andExpect(jsonPath("$[0].id").value(0))  // Verify the first product ID
                        .andExpect(jsonPath("$[0].name").value("Product 1"))  // Verify the first product name
                        .andExpect(jsonPath("$[1].id").value(0))  // Verify the second product ID
                        .andExpect(jsonPath("$[1].name").value("Product 2"));  // Verify the second product name
        }

        @Test
        void testGetAllProducts_WithSearchTerm() throws Exception {
                // Prepare sample products that match the search term
                Product product1 = new Product(1, 100, "Type A", "Product 1", "Description 1");
                List<Product> products = Arrays.asList(product1);

                // Mock the service call to return the filtered list of products
                when(productService.getAllProducts(null, null, "Product 1")).thenReturn(products);

                // Perform GET request to "/product/getAll?searchTerm=Product 1"
                mockMvc.perform(get("/product/getAll")
                                .param("searchTerm", "Product 1")  // Provide the searchTerm parameter
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())  // Expect status 200 OK
                        .andExpect(jsonPath("$.length()").value(1))  // Verify that only 1 product is returned
                        .andExpect(jsonPath("$[0].name").value("Product 1"));  // Verify that the returned product matches the search term
        }

        @Test
        void testGetAllProducts_WithSortBy() throws Exception {
                // Prepare some products with different prices to test sorting
                Product product1 = new Product(1, 100, "Type A", "Product 1", "Description 1");
                Product product2 = new Product(2, 200, "Type B", "Product 2", "Description 2");
                List<Product> products = Arrays.asList(product1, product2);

                // Mock the service call to return sorted products
                when(productService.getAllProducts("price", null, null)).thenReturn(products);  // Sort by price

                // Perform GET request to "/product/getAll?sortBy=price"
                mockMvc.perform(get("/product/getAll")
                                .param("sortBy", "price")  // Provide the sortBy parameter
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())  // Expect status 200 OK
                        .andExpect(jsonPath("$.length()").value(2))  // Verify number of products
                        .andExpect(jsonPath("$[0].price").value(100))  // Check the price of the first product
                        .andExpect(jsonPath("$[1].price").value(200));  // Check the price of the second product
        }

        @Test
        void testGetAllProducts_WithSearchType() throws Exception {
                // Prepare sample products of different types
                Product product1 = new Product(1, 100, "Type A", "Product 1", "Description 1");
                Product product2 = new Product(2, 200, "Type B", "Product 2", "Description 2");
                List<Product> products = Arrays.asList(product1);

                // Mock the service call to return filtered products based on the searchType
                when(productService.getAllProducts(null, "type", "Type A")).thenReturn(products);

                // Perform GET request to "/product/getAll?searchType=type&searchTerm=Type A"
                mockMvc.perform(get("/product/getAll")
                                .param("searchType", "type")  // Provide the searchType parameter
                                .param("searchTerm", "Type A")  // Provide the searchTerm parameter
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())  // Expect status 200 OK
                        .andExpect(jsonPath("$.length()").value(1))  // Verify that 1 product matches the search type and term
                        .andExpect(jsonPath("$[0].type").value("Type A"));  // Verify that the returned product matches the search type
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testUpdateProduct_Success() throws Exception {
                // Arrange
                ProductDTO productDTO = new ProductDTO();
                productDTO.setId(1);
                productDTO.setName("Updated Product");
                productDTO.setPrice(150.0);

                Product updatedProduct = new Product();
                updatedProduct.setId(1);
                updatedProduct.setName("Updated Product");
                updatedProduct.setPrice(150.0);

                when(productService.updateProduct(any(ProductDTO.class))).thenReturn(updatedProduct);

                // Act and Assert
                mockMvc.perform(post("/product/update")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(productDTO)))
                        .andExpect(status().isOk())  // HTTP 200 OK
                        .andExpect(content().string("Product updated successfully"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testUpdateProduct_NotFound() throws Exception {
                // Arrange
                ProductDTO productDTO = new ProductDTO();
                productDTO.setId(1);  // Non-existent product ID

                when(productService.updateProduct(any(ProductDTO.class))).thenReturn(null);

                // Act and Assert
                mockMvc.perform(post("/product/update")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(productDTO)))
                        .andExpect(status().isNotFound())  // HTTP 404 Not Found
                        .andExpect(content().string("ERROR!!! - product not found"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testUpdateProduct_ExceptionHandling() throws Exception {
                ProductDTO productDTO = new ProductDTO();
                productDTO.setId(1);  // Non-existent product ID

                when(productService.updateProduct(any(ProductDTO.class))).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));

                // Act and Assert
                mockMvc.perform(post("/product/update")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(productDTO)))
                        .andExpect(status().isInternalServerError());  // Expect status 500 Internal Server Error
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testDeleteProduct_Success() throws Exception {
                // Arrange
                when(productService.deleteProduct(1)).thenReturn(true);

                // Act and Assert
                mockMvc.perform(delete("/product/delete")
                                .param("id", "1"))
                        .andExpect(status().isOk())  // HTTP 200 OK
                        .andExpect(content().string("Product deleted successfully"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testDeleteProduct_NotFound() throws Exception {
                // Arrange
                when(productService.deleteProduct(999)).thenReturn(false);

                // Act and Assert
                mockMvc.perform(delete("/product/delete")
                                .param("id", "999"))
                        .andExpect(status().isNotFound())  // HTTP 404 Not Found
                        .andExpect(content().string("Error!!! - Product associated with this id is not found in the database"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testDeleteProduct_ExceptionHandling() throws Exception {
                ProductDTO productDTO = new ProductDTO();
                productDTO.setId(1);  // Non-existent product ID

                when(productService.deleteProduct(1)).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));

                // Act and Assert
                mockMvc.perform(delete("/product/delete")
                                .param("id", "1"))
                        .andExpect(status().isInternalServerError());  // Expect status 500 Internal Server Error
        }

        @Test
        void testGetProductByName_Success() throws Exception {
                // Prepare the product name
                String productName = "Test Product";

                // Prepare the product mock object
                Product product = new Product(1, 100, "Type A", productName, "Test description");
                product.setId(1);

                // Mock the service call to return the product
                when(productService.getProductByName(productName)).thenReturn(product);

                // Perform the GET request to get the product by name
                mockMvc.perform(get("/product/getByName")
                                .param("name", productName))  // Pass the product name as a request param
                        .andExpect(status().isOk())  // Expect HTTP 200 OK
                        .andExpect(jsonPath("$.id").value(1))  // Verify the returned product id
                        .andExpect(jsonPath("$.name").value(productName))  // Verify the returned product name
                        .andExpect(jsonPath("$.description").value("Test description"));  // Verify the product description
        }

        @Test
        void testGetProductByName_ProductNotFound() throws Exception {
                // Prepare the product name that doesn't exist
                String productName = "NonExistentProduct";

                // Mock the service call to throw an exception (product not found)
                when(productService.getProductByName(productName)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

                // Perform the GET request to get the product by name
                mockMvc.perform(get("/product/getByName")
                                .param("name", productName))  // Pass the product name as a request param
                        .andExpect(status().isNotFound())  // Expect HTTP 404 Not Found
                        .andExpect(content().string("Product not found"));  // Error message from the exception
        }

        @Test
        void testGetProductByName_ExceptionHandling() throws Exception {
                // Prepare the product name for testing
                String productName = "Test Product";

                // Mock the service to throw a runtime exception
                when(productService.getProductByName(productName)).thenThrow(new RuntimeException("Internal error"));

                // Perform the GET request to get the product by name
                mockMvc.perform(get("/product/getByName")
                                .param("name", productName))  // Pass the product name as a request param
                        .andExpect(status().isInternalServerError());  // Expect HTTP 500 Internal Server Error
        }

        @Test
        void testGetProductById_Success() throws Exception {
                // Arrange
                Product product = new Product();
                product.setId(1);
                product.setName("Test Product");
                product.setPrice(100.0);

                when(productService.getProductById(1)).thenReturn(product);

                // Act and Assert
                mockMvc.perform(get("/product/getById")
                                .param("id", "1"))
                        .andExpect(status().isOk())  // HTTP 200 OK
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.name").value("Test Product"));
        }

        @Test
        void testGetProductById_NotFound() throws Exception {
                // Arrange
                when(productService.getProductById(999)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

                // Act and Assert
                mockMvc.perform(get("/product/getById")
                                .param("id", "999"))
                        .andExpect(status().isNotFound())  // HTTP 404 Not Found
                        .andExpect(content().string("Product not found"));
        }

        @Test
        void testSearchProducts_Success() throws Exception {
                // Arrange
                Product product = new Product();
                product.setId(1);
                product.setName("Test Product");

                when(productService.searchProducts(anyString(), anyString())).thenReturn(Collections.singletonList(product));

                // Act and Assert
                mockMvc.perform(get("/product/search")
                                .param("type", "name")
                                .param("term", "Test"))
                        .andExpect(status().isOk())  // HTTP 200 OK
                        .andExpect(jsonPath("$[0].id").value(1))
                        .andExpect(jsonPath("$[0].name").value("Test Product"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testSearchProducts_NotFound() throws Exception {
                // Arrange
                when(productService.searchProducts(anyString(), anyString())).thenReturn(null);

                // Act and Assert
                mockMvc.perform(get("/product/search")
                                .param("type", "name")
                                .param("term", "Nonexistent"))
                        .andExpect(status().isNotFound())  // HTTP 404 Not Found
                        .andExpect(content().string("ERROR!!! - product not found"));
        }

        @Test
        void testSearchProducts_ExceptionHandling() throws Exception {
                // Prepare the search parameters
                String searchType = "category";
                String searchTerm = "Electronics";

                // Mock the service to throw a RuntimeException (internal server error)
                when(productService.searchProducts(searchType, searchTerm)).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));

                // Perform the GET request to search for products
                mockMvc.perform(get("/product/search")
                                .param("type", searchType)
                                .param("term", searchTerm))
                        .andExpect(status().isInternalServerError()); // Expect HTTP 500 Internal Server Error
        }

        @Test
        void testSearchProductsM_Success() throws Exception {
                // Prepare the search parameter
                String name = "Phone";

                // Prepare mock product data to return from the service
                Product product1 = new Product(1, 100, "Electronics", "Phone", "Smartphone");
                Product product2 = new Product(2, 150, "Electronics", "Smartphone", "Android smartphone");
                List<Product> productList = Arrays.asList(product1, product2);

                // Mock the service call to return a list of products
                when(productService.findAllByNameContaining(name)).thenReturn(productList);

                // Perform the GET request to search for products
                mockMvc.perform(get("/product/searchModal")
                                .param("name", name))
                        .andExpect(status().isOk())  // Expect HTTP 200 OK
                        .andExpect(jsonPath("$[0].id").value(0))  // Verify the first product ID
                        .andExpect(jsonPath("$[0].name").value("Phone"))  // Verify the first product name
                        .andExpect(jsonPath("$[1].id").value(0))  // Verify the second product ID
                        .andExpect(jsonPath("$[1].name").value("Smartphone"));  // Verify the second product name
        }

        @Test
        void testSearchProductsM_EmptyResults() throws Exception {
                // Prepare the search parameter
                String name = "NonExistentProduct";

                // Mock the service call to return an empty list (no products found)
                when(productService.findAllByNameContaining(name)).thenReturn(List.of());

                // Perform the GET request to search for products
                mockMvc.perform(get("/product/searchModal")
                                .param("name", name))
                        .andExpect(status().isOk())  // Expect HTTP 200 OK, even though no products are found
                        .andExpect(content().json("[]"));  // Expect an empty JSON array
        }

        @Test
        void testSearchProductsM_ExceptionHandling() throws Exception {
                // Prepare the search parameter
                String name = "Phone";

                // Mock the service to throw a ResponseStatusException
                when(productService.findAllByNameContaining(name)).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));

                // Perform the GET request to search for products
                mockMvc.perform(get("/product/searchModal")
                                .param("name", name))
                        .andExpect(status().isInternalServerError())  // Expect HTTP 500 Internal Server Error
                        .andExpect(content().string(""));  // Response should be empty body
        }


        @Test
        void testGetFeaturedProducts_Success() throws Exception {
                // Arrange
                Product product = new Product();
                product.setId(1);
                product.setName("Featured Product");

                when(productService.getFeaturedProducts()).thenReturn(Collections.singletonList(product));

                // Act and Assert
                mockMvc.perform(get("/product/getFeatured"))
                        .andExpect(status().isOk())  // HTTP 200 OK
                        .andExpect(jsonPath("$[0].id").value(1))
                        .andExpect(jsonPath("$[0].name").value("Featured Product"));
        }

        @Test
        void testGetFeaturedProducts_EmptyList() throws Exception {
                // Mock the service call to return an empty list (no featured products)
                when(productService.getFeaturedProducts()).thenReturn(List.of());

                // Perform the GET request to retrieve featured products
                mockMvc.perform(get("/product/getFeatured"))
                        .andExpect(status().isOk())  // Expect HTTP 200 OK
                        .andExpect(content().json("[]"));  // Expect an empty JSON array
        }

        @Test
        void testGetFeaturedProducts_ExceptionHandling() throws Exception {
                // Mock the service to throw a ResponseStatusException
                when(productService.getFeaturedProducts()).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));

                // Perform the GET request to retrieve featured products
                mockMvc.perform(get("/product/getFeatured"))
                        .andExpect(status().isInternalServerError())  // Expect HTTP 500 Internal Server Error
                        .andExpect(content().string(""));  // Response should be empty body
        }

        @Test
        void testGetDeliverOnly_Success() throws Exception {
                // Prepare mock data: list of product IDs that are deliverable only
                List<Integer> deliveryOnlyProductIds = Arrays.asList(1, 2, 3);

                // Mock the service call to return a list of product IDs
                when(productService.getDeliveryOnly()).thenReturn(deliveryOnlyProductIds);

                // Perform the GET request to retrieve deliverable-only product IDs
                mockMvc.perform(get("/product/getDeliverOnly"))
                        .andExpect(status().isOk())  // Expect HTTP 200 OK
                        .andExpect(jsonPath("$[0]").value(1))  // Verify the first product ID
                        .andExpect(jsonPath("$[1]").value(2))  // Verify the second product ID
                        .andExpect(jsonPath("$[2]").value(3));  // Verify the third product ID
        }

        @Test
        void testGetDeliverOnly_EmptyList() throws Exception {
                // Mock the service call to return an empty list (no deliverable-only products)
                when(productService.getDeliveryOnly()).thenReturn(List.of());

                // Perform the GET request to retrieve deliverable-only product IDs
                mockMvc.perform(get("/product/getDeliverOnly"))
                        .andExpect(status().isOk())  // Expect HTTP 200 OK
                        .andExpect(content().json("[]"));  // Expect an empty JSON array
        }

        @Test
        void testGetDeliverOnly_ExceptionHandling() throws Exception {
                // Mock the service to throw a ResponseStatusException
                when(productService.getDeliveryOnly()).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));

                // Perform the GET request to retrieve deliverable-only product IDs
                mockMvc.perform(get("/product/getDeliverOnly"))
                        .andExpect(status().isInternalServerError())  // Expect HTTP 500 Internal Server Error
                        .andExpect(content().string(""));  // Response should be empty body
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testUpdateFeaturedStatus_Success() throws Exception {
                // Prepare mock data: list of featured product IDs
                List<Integer> featuredItemIds = Arrays.asList(1, 2, 3);

                // Perform the POST request to update featured status
                mockMvc.perform(post("/product/updateFeaturedStatus")
                                .contentType("application/json")
                                .content("[1, 2, 3]"))  // JSON list of product IDs
                        .andExpect(status().isOk())  // Expect HTTP 200 OK
                        .andExpect(content().string("Featured status updated successfully."));  // Expect success message

                // Verify that the service method is called once with the correct argument
                verify(productService, times(1)).updateFeaturedStatus(featuredItemIds);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testUpdateFeaturedStatus_EmptyList() throws Exception {
                // Prepare mock data: empty list of featured product IDs
                List<Integer> emptyFeaturedItemIds = List.of();

                // Perform the POST request with an empty list
                mockMvc.perform(post("/product/updateFeaturedStatus")
                                .contentType("application/json")
                                .content("[]"))  // Empty JSON array
                        .andExpect(status().isOk())  // Expect HTTP 200 OK
                        .andExpect(content().string("Featured status updated successfully."));  // Success message

                // Verify that the service method is called once with the empty list
                verify(productService, times(1)).updateFeaturedStatus(emptyFeaturedItemIds);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testUpdateFeaturedStatus_Exception() throws Exception {
                // Simulate an exception being thrown during the service call
                doThrow(new RuntimeException("Error updating featured status")).when(productService).updateFeaturedStatus(anyList());

                // Perform the POST request
                mockMvc.perform(post("/product/updateFeaturedStatus")
                                .contentType("application/json")
                                .content("[1, 2, 3]"))
                        .andExpect(status().isInternalServerError());  // Expect HTTP 500 Internal Server Error

                // Verify that the service method was called
                verify(productService, times(1)).updateFeaturedStatus(anyList());
        }

        @Test
        void testGetAllProducts_Success() throws Exception {
                // Prepare mock data: list of products
                Product product1 = new Product(1, 2, 100, "Type A", "name1", "Description 1");
                Product product2 = new Product(2, 2, 200, "Type B", "name2", "Description 2");
                List<Product> products = Arrays.asList(product1, product2);

                // Mock the service call to return the products
                when(productService.getAllProducts()).thenReturn(products);

                // Perform the GET request to retrieve all products
                mockMvc.perform(get("/product/allProducts"))
                        .andExpect(status().isOk())  // Expect HTTP 200 OK
                        .andExpect(jsonPath("$[0].id").value(1))  // Expect the first product ID to be 1
                        .andExpect(jsonPath("$[0].name").value("name1"))  // Expect the first product name to be "Product 1"
                        .andExpect(jsonPath("$[1].id").value(2))  // Expect the second product ID to be 2
                        .andExpect(jsonPath("$[1].name").value("name2"));  // Expect the second product name to be "Product 2"

                // Verify that the service method is called once
                verify(productService, times(1)).getAllProducts();
        }

        @Test
        void testGetAllProducts_Exception() throws Exception {
                // Simulate an exception being thrown during the service call
                doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error")).when(productService).getAllProducts();

                // Perform the GET request
                mockMvc.perform(get("/product/allProducts"))
                        .andExpect(status().isInternalServerError());  // Expect HTTP 500 Internal Server Error

                // Verify that the service method was called
                verify(productService, times(1)).getAllProducts();
        }
}
