package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Model.RentalRepository;
import com.example.SeniorProject.Service.RentalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class RentalControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private RentalService rentalService;

        @MockBean
        private RentalRepository rentalRepository;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
        }

        // Test for getAllProducts
        @Test
        void testGetAllProducts() throws Exception {
                Page<Product> productPage = new PageImpl<>(List.of(new Product(), new Product())); // Mocked Product List

                // Mock service call
                when(rentalService.getAllProducts(anyString(), anyString(), any(Pageable.class)))
                        .thenReturn(productPage);

                mockMvc.perform(get("/rental/getAll")
                                .param("kw", "test")
                                .param("type", "type1")
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content.length()").value(2));
        }

        // Test for searchProducts
        @Test
        void testSearchProducts() throws Exception {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Product> productPage = new PageImpl<>(List.of(new Product(), new Product())); // Mocked Product List

                // Mock service call
                when(rentalService.searchProducts(anyString(), anyString(), any(Pageable.class)))
                        .thenReturn(productPage);

                mockMvc.perform(get("/rental/search")
                                .param("kw", "keyword")
                                .param("type", "typeA")
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content.length()").value(2));
        }

        // Test for getDistinctTypes
        @Test
        void testGetDistinctTypes() throws Exception {
                // Mock service call
                when(rentalService.getDistinctProductTypes()).thenReturn(List.of("type1", "type2", "type3"));

                mockMvc.perform(get("/rental/types")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$[0]").value("type1"))
                        .andExpect(jsonPath("$[1]").value("type2"))
                        .andExpect(jsonPath("$[2]").value("type3"));
        }
}
