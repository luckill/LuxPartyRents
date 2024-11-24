package com.example.SeniorProject.Service;

import static org.junit.jupiter.api.Assertions.*;
import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Model.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RentalServiceTest {

        @InjectMocks
        private RentalService rentalService;

        @Mock
        private RentalRepository rentalRepository;

        @Mock
        private Pageable pageable;

        @Mock
        private Page<Product> productPage;

        @BeforeEach
        public void setUp() {
                MockitoAnnotations.openMocks(this);
        }

        @Test
        public void testGetAllProducts_WithKeywordAndType() {
                // Arrange
                String keyword = "tent";
                String type = "outdoor";
                when(rentalRepository.findByNameContainingIgnoreCaseAndTypeIgnoreCase(keyword, type, pageable))
                        .thenReturn(productPage);

                // Act
                Page<Product> result = rentalService.getAllProducts(keyword, type, pageable);

                // Assert
                assertEquals(productPage, result);
                verify(rentalRepository).findByNameContainingIgnoreCaseAndTypeIgnoreCase(keyword, type, pageable);
        }

        @Test
        public void testGetAllProducts_WithKeywordOnly() {
                // Arrange
                String keyword = "tent";
                when(rentalRepository.findByNameContainingIgnoreCase(keyword, pageable))
                        .thenReturn(productPage);

                // Act
                Page<Product> result = rentalService.getAllProducts(keyword, null, pageable);

                // Assert
                assertEquals(productPage, result);
                verify(rentalRepository).findByNameContainingIgnoreCase(keyword, pageable);
        }

        @Test
        public void testGetAllProducts_WithTypeOnly() {
                // Arrange
                String type = "outdoor";
                when(rentalRepository.findByTypeIgnoreCase(type, pageable))
                        .thenReturn(productPage);

                // Act
                Page<Product> result = rentalService.getAllProducts(null, type, pageable);

                // Assert
                assertEquals(productPage, result);
                verify(rentalRepository).findByTypeIgnoreCase(type, pageable);
        }

        @Test
        public void testGetAllProducts_NoFilters() {
                // Arrange
                when(rentalRepository.findAll(pageable)).thenReturn(productPage);

                // Act
                Page<Product> result = rentalService.getAllProducts(null, null, pageable);

                // Assert
                assertEquals(productPage, result);
                verify(rentalRepository).findAll(pageable);
        }

        @Test
        public void testSearchProducts_WithKeywordOnly() {
                // Arrange
                String keyword = "tent";
                when(rentalRepository.findByNameContainingIgnoreCase(keyword, pageable))
                        .thenReturn(productPage);

                // Act
                Page<Product> result = rentalService.searchProducts(keyword, null, pageable);

                // Assert
                assertEquals(productPage, result);
                verify(rentalRepository).findByNameContainingIgnoreCase(keyword, pageable);
        }

        @Test
        public void testSearchProducts_WithTypeOnly() {
                // Arrange
                String type = "outdoor";
                when(rentalRepository.findByTypeIgnoreCase(type, pageable))
                        .thenReturn(productPage);

                // Act
                Page<Product> result = rentalService.searchProducts(null, type, pageable);

                // Assert
                assertEquals(productPage, result);
                verify(rentalRepository).findByTypeIgnoreCase(type, pageable);
        }

        @Test
        public void testSearchProducts_NoFilters() {
                // Arrange
                when(rentalRepository.findAll(pageable)).thenReturn(productPage);

                // Act
                Page<Product> result = rentalService.searchProducts(null, null, pageable);

                // Assert
                assertEquals(productPage, result);
                verify(rentalRepository).findAll(pageable);
        }

        @Test
        public void testGetDistinctProductTypes() {
                // Arrange
                List<String> expectedProductTypes = Arrays.asList("Furniture", "Decoration", "Lighting");
                when(rentalRepository.findDistinctProductTypes()).thenReturn(expectedProductTypes);

                // Act
                List<String> actualProductTypes = rentalService.getDistinctProductTypes();

                // Assert
                assertNotNull(actualProductTypes);
                assertEquals(expectedProductTypes.size(), actualProductTypes.size());
                assertTrue(actualProductTypes.contains("Furniture"));
                assertTrue(actualProductTypes.contains("Decoration"));
                assertTrue(actualProductTypes.contains("Lighting"));

                // Verify interaction with the mock
                verify(rentalRepository, times(1)).findDistinctProductTypes();
        }

        @Test
        public void testGetDistinctProductTypes_EmptyList() {
                // Arrange
                List<String> expectedProductTypes = Arrays.asList(); // Empty list
                when(rentalRepository.findDistinctProductTypes()).thenReturn(expectedProductTypes);

                // Act
                List<String> actualProductTypes = rentalService.getDistinctProductTypes();

                // Assert
                assertNotNull(actualProductTypes);
                assertTrue(actualProductTypes.isEmpty());

                // Verify interaction with the mock
                verify(rentalRepository, times(1)).findDistinctProductTypes();
        }
}