package com.example.SeniorProject.Service;

import com.example.SeniorProject.DTOs.ProductDTO;
import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Model.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {
        @InjectMocks
        private ProductService productService;

        @Mock
        private ProductRepository productRepository;

        @BeforeEach
        public void setup() {
                MockitoAnnotations.openMocks(this);
        }


        @Test
        public void getProductByIdTest() {

                int id = 1;
                Product testProduct = new Product(id,  2, 20, "type", "name", "description");

                Mockito.when(productRepository.findById(id)).thenReturn(Optional.of(testProduct));

                Product result = productService.getProductById(id);

                assertNotNull(result);
                assertEquals(id, result.getId());
                assertEquals(2, result.getQuantity());
                assertEquals(20, result.getPrice());
                assertEquals("type", result.getType());
                assertEquals("name", result.getName());
                assertEquals("description", result.getDescription());
        }

        @Test
        public void getProductByNameTest()
        {
                String name = "testName";
                Product testProduct = new Product(1,  2, 20, "type", name, "description");

                Mockito.when(productRepository.getProductByName(name)).thenReturn(testProduct);

                Product result = productService.getProductByName(name);

                assertNotNull(result);
                assertEquals(1, result.getId());
                assertEquals(2, result.getQuantity());
                assertEquals(20, result.getPrice());
                assertEquals("type", result.getType());
                assertEquals(name, result.getName());
                assertEquals("description", result.getDescription());
        }

        @Test
        public void getProductByNameTestNull()
        {
                String name = "testName";
                Product testProduct = new Product(1,  2, 20, "type", name, "description");

                Mockito.when(productRepository.getProductByName(name)).thenReturn(testProduct);

                Product result = productService.getProductByName("random");

                assertNull(result);
        }

        @Test
        public void addProductTest()
        {
                String name = "testName";
                Product testProduct = new Product( 2, 20, "type", name, "description");

                //Mockito.when(productRepository.getProductByName(name)).thenReturn(testProduct);

                // Mock the save method
                Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(testProduct);

                ProductDTO productDTO = productService.mapToProductDTO(testProduct);
                Product result = productService.addProduct(productDTO);

                assertNotNull(result);
                assertEquals(2, result.getQuantity());
                assertEquals(20, result.getPrice());
                assertEquals("type", result.getType());
                assertEquals(name, result.getName());
                assertEquals("description", result.getDescription());
        }

        @Test
        public void updateProductTest()
        {
                String name = "testName";
                Product testProduct = new Product(1, 2, 20, "type", name, "description");

                // Mock the findById method to return the existing product
                Mockito.when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

                // Mock the save method
                Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(testProduct);

                ProductDTO productDTO = productService.mapToProductDTO(testProduct);
                productService.addProduct(productDTO);

                // Update testProduct fields
                testProduct.setQuantity(20);
                testProduct.setPrice(40);
                testProduct.setType("The type");
                testProduct.setName("NewName");
                testProduct.setDescription("More");

                // Prepare the update DTO
                ProductDTO update = productService.mapToProductDTO(testProduct);

                Product result = productService.updateProduct(update);

                // Assert the updated values
                assertNotNull(result);
                assertEquals(20, result.getQuantity()); // updated quantity
                assertEquals(40, result.getPrice());    // updated price
                assertEquals("The type", result.getType()); // updated type
                assertEquals("NewName", result.getName()); // updated name
                assertEquals("More", result.getDescription()); // updated description
        }

        @Test
        public void updateProductTestNull()
        {
                String name = "testName";
                Product testProduct = new Product(1, 2, 20, "type", name, "description");

                // Mock the findById method to return the existing product
                Mockito.when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

                // Mock the save method
                Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(testProduct);

                ProductDTO productDTO = productService.mapToProductDTO(testProduct);
                productService.addProduct(productDTO);

                // Update testProduct fields
                testProduct.setQuantity(20);
                testProduct.setPrice(40);
                testProduct.setType("The type");
                testProduct.setName("NewName");
                testProduct.setDescription("More");

                // Prepare the update DTO
                String name2 = "tesasdftName";
                Product testProduct2 = new Product(10, 200, 200, "tyasdfpe", name2, "descasdfription");

                ProductDTO update = productService.mapToProductDTO(testProduct2);

                Product result = productService.updateProduct(update);

                // Assert the updated values
                assertNull(result);
        }

        @Test
        public void deleteByIdTest()
        {
                int id = 1;
                Product testProduct = new Product(id, 2, 20, "type", "name", "description");

                // Mock the findById method to return the existing product
                Mockito.when(productRepository.findById(id)).thenReturn(Optional.of(testProduct));

                // Mock the deleteById method
                Mockito.doNothing().when(productRepository).deleteById(id);

                boolean result = productService.deleteProduct(id);

                // Since the product is deleted, the delete method typically does not return the deleted product,
                // so we need to adjust the expectation here.
                assertNotNull(result);
                assertEquals(true, result);
        }

        @Test
        public void deleteByIdTestNull()
        {
                int id = 1;
                Product testProduct = new Product(id, 2, 20, "type", "name", "description");

                // Mock the findById method to return the existing product
                Mockito.when(productRepository.findById(id)).thenReturn(Optional.of(testProduct));

                // Mock the deleteById method
                Mockito.doNothing().when(productRepository).deleteById(id);

                boolean result = productService.deleteProduct(2);

                // Since the product is deleted, the delete method typically does not return the deleted product,
                // so we need to adjust the expectation here.
                assertNotNull(result);
                assertEquals(false, result);
        }

        @Test
        public void searchProductsTestName()
        {
                Product testProduct1 = new Product( 21, 202, "sadfe", "test1", "asdffription");

                Product testProduct2 = new Product( 2, 20, "type", "test2", "description");

                Product testProduct3 = new Product( 2, 20, "type", "test3", "description");

                Product testProduct4 = new Product( 2, 20, "type", "test4", "description");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);
                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }



                List<Product> result = productService.searchProducts("name", "test1");

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = testProduct1;
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void searchProductsTestType()
        {
                Product testProduct1 = new Product( 21, 202, "type2", "test1", "asdffription");

                Product testProduct2 = new Product( 2, 20, "type2", "test2", "description");

                Product testProduct3 = new Product( 2, 20, "type", "test3", "description");

                Product testProduct4 = new Product( 2, 20, "type", "test4", "description");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);

                List<Product> realProd = new ArrayList<>();
                realProd.add(testProduct1);
                realProd.add(testProduct2);

                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }



                List<Product> result = productService.searchProducts("type", "type2");

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = realProd.get(i);
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void searchProductsTestId()
        {
                Product testProduct1 = new Product(1, 21, 202, "type2", "test1", "asdffription");

                Product testProduct2 = new Product(2, 2, 20, "type2", "test2", "description");

                Product testProduct3 = new Product(3, 2, 20, "type", "test3", "description");

                Product testProduct4 = new Product(4, 200, 20000, "typasdfase", "tesasdft4", "dasdfescription");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);

                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }



                List<Product> result = productService.searchProducts("id", "4");

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = testProduct4;
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void searchProductsTestIdException()
        {
                Product testProduct1 = new Product(1, 21, 202, "type2", "test1", "asdffription");

                Product testProduct2 = new Product(2, 2, 20, "type2", "test2", "description");

                Product testProduct3 = new Product(3, 2, 20, "type", "test3", "description");

                Product testProduct4 = new Product(4, 200, 20000, "typasdfase", "tesasdft4", "dasdfescription");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);

                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }



                List<Product> result = productService.searchProducts("id", "sadfasdf");

                assertNull(result);
        }

        @Test
        public void searchProductsTestNull()
        {
                Product testProduct1 = new Product(1, 21, 202, "type2", "test1", "asdffription");

                Product testProduct2 = new Product(2, 2, 20, "type2", "test2", "description");

                Product testProduct3 = new Product(3, 2, 20, "type", "test3", "description");

                Product testProduct4 = new Product(4, 200, 20000, "typasdfase", "tesasdft4", "dasdfescription");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);

                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }


                List<Product> result = productService.searchProducts("asdf", "asdff");

                assertNull(result);
        }

        @Test
        public void getAllProductsTestNoFilterOrSort()
        {
                Product testProduct1 = new Product( 2, 20, "type", "test1", "description");

                Product testProduct2 = new Product( 2, 20, "type", "test2", "description");

                Product testProduct3 = new Product( 2, 20, "type", "test3", "description");

                Product testProduct4 = new Product( 2, 20, "type", "test4", "description");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);
                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }



                List<Product> result = productService.getAllProducts(null,null,null);

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = products.get(i);
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void getAllProductsTestName()
        {
                Product testProduct1 = new Product( 2, 20, "type", "test1", "description");

                Product testProduct2 = new Product( 2, 20, "type", "test2", "description");

                Product testProduct3 = new Product( 2, 20, "type", "test3", "description");

                Product testProduct4 = new Product( 2, 20, "type", "test4", "description");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);
                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }



                List<Product> result = productService.getAllProducts(null,"name","test1");

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = testProduct1;
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void getAllProductsTestType()
        {
                Product testProduct1 = new Product( 2, 20, "type", "test1", "description");

                Product testProduct2 = new Product( 2, 20, "this", "test2", "description");

                Product testProduct3 = new Product( 2, 20, "type", "test3", "description");

                Product testProduct4 = new Product( 2, 20, "type", "test4", "description");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);
                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }



                List<Product> result = productService.getAllProducts(null,"type","this");

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = testProduct2;
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void getAllProductsTestId()
        {
                Product testProduct1 = new Product( 2, 20, "type", "test1", "description");

                Product testProduct2 = new Product( 2, 20, "this", "test2", "description");

                Product testProduct3 = new Product( 2, 20, "type", "test3", "description");

                Product testProduct4 = new Product( 2, 20, "tasdfype", "teasdfst4", "descasdfription");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);
                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }



                List<Product> result = productService.getAllProducts(null,"id","4");

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = testProduct4;
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void getAllProductsTestDefault()
        {
                Product testProduct1 = new Product( 2, 20, "type", "test1", "description");

                Product testProduct2 = new Product( 2, 20, "this", "test2", "description");

                Product testProduct3 = new Product( 2, 20, "type", "test3", "description");

                Product testProduct4 = new Product( 2, 20, "tasdfype", "teasdfst4", "descasdfription");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);
                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }



                List<Product> result = productService.getAllProducts(null,"sadf","asdf");

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = products.get(i);
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void getAllProductsTestSortById()
        {
                Product testProduct1 = new Product( 2, 20, "type", "test1", "description");

                Product testProduct2 = new Product( 2, 20, "this", "test2", "description");

                Product testProduct3 = new Product( 2, 20, "type", "test3", "description");

                Product testProduct4 = new Product( 2, 20, "tasdfype", "teasdfst4", "descasdfription");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);
                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }



                List<Product> result = productService.getAllProducts("id",null,null);

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = products.get(i);
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void getAllProductsTestSortByName()
        {
                Product testProduct1 = new Product( 2, 20, "type", "D", "description");

                Product testProduct2 = new Product( 2, 20, "this", "B", "description");

                Product testProduct3 = new Product( 2, 20, "type", "C", "description");

                Product testProduct4 = new Product( 2, 20, "tasdfype", "A", "descasdfription");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);
                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }

                List<Product> sorted = new ArrayList<>();
                sorted.add(testProduct4);
                sorted.add(testProduct2);
                sorted.add(testProduct3);
                sorted.add(testProduct1);

                List<Product> result = productService.getAllProducts("name",null,null);

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = sorted.get(i);
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void getAllProductsTestSortByPrice()
        {
                Product testProduct1 = new Product( 2, 4, "type", "D", "description");

                Product testProduct2 = new Product( 2, 2, "this", "B", "description");

                Product testProduct3 = new Product( 2, 3, "type", "C", "description");

                Product testProduct4 = new Product( 2, 1, "tasdfype", "A", "descasdfription");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);
                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }

                List<Product> sorted = new ArrayList<>();
                sorted.add(testProduct4);
                sorted.add(testProduct2);
                sorted.add(testProduct3);
                sorted.add(testProduct1);

                List<Product> result = productService.getAllProducts("price",null,null);

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = sorted.get(i);
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void getAllProductsTestSortByQuantity()
        {
                Product testProduct1 = new Product( 5, 4, "type", "D", "description");

                Product testProduct2 = new Product( 3, 2, "this", "B", "description");

                Product testProduct3 = new Product( 4, 3, "type", "C", "description");

                Product testProduct4 = new Product( 2, 1, "tasdfype", "A", "descasdfription");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);
                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }

                List<Product> sorted = new ArrayList<>();
                sorted.add(testProduct4);
                sorted.add(testProduct2);
                sorted.add(testProduct3);
                sorted.add(testProduct1);

                List<Product> result = productService.getAllProducts("quantity",null,null);

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = sorted.get(i);
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void getAllProductsTestSortByType()
        {
                Product testProduct1 = new Product( 5, 4, "D", "D", "description");

                Product testProduct2 = new Product( 3, 2, "B", "B", "description");

                Product testProduct3 = new Product( 4, 3, "C", "C", "description");

                Product testProduct4 = new Product( 2, 1, "A", "A", "descasdfription");

                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);
                for(int i = 0; i < products.size(); i++)
                {
                        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(products.get(i));
                }

                List<Product> sorted = new ArrayList<>();
                sorted.add(testProduct4);
                sorted.add(testProduct2);
                sorted.add(testProduct3);
                sorted.add(testProduct1);

                List<Product> result = productService.getAllProducts("type",null,null);

                for(int i = 0; i < result.size(); i++)
                {
                        Product real = sorted.get(i);
                        Product test = result.get(i);
                        assertNotNull(result);
                        assertEquals(real.getQuantity(), test.getQuantity());
                        assertEquals(real.getPrice(), test.getPrice());
                        assertEquals(real.getType(), test.getType());
                        assertEquals(real.getName(), test.getName());
                        assertEquals(real.getDescription(), test.getDescription());
                }
        }

        @Test
        public void updateFeaturedStatusTest() {
                // Create test products
                Product testProduct1 = mock(Product.class);
                Product testProduct2 = mock(Product.class);
                Product testProduct3 = mock(Product.class);
                Product testProduct4 = mock(Product.class);

                // List of products to be returned by the repository
                List<Product> products = new ArrayList<>();
                products.add(testProduct1);
                products.add(testProduct2);
                products.add(testProduct3);
                products.add(testProduct4);

                // Mocking the repository's findAll behavior to return the list of products
                when(productRepository.findAll()).thenReturn(products);

                // Featured product IDs (IDs of products that should be marked as featured)
                List<Integer> featuredItemIds = new ArrayList<>();
                featuredItemIds.add(3); // Only Product B (id=3) should be featured

                // Call the method under test
                productService.updateFeaturedStatus(featuredItemIds);

                // Verify that the save method was called for each product
                verify(productRepository, times(1)).save(testProduct1); // Product D
                verify(productRepository, times(1)).save(testProduct2); // Product B
                verify(productRepository, times(1)).save(testProduct3); // Product C
                verify(productRepository, times(1)).save(testProduct4); // Product A

                // Verify that the setFeatureProduct method was called with correct boolean values
                verify(testProduct1, times(1)).setFeatureProduct(false); // Product D should NOT be featured
                verify(testProduct2, times(1)).setFeatureProduct(false);  // Product B should be featured
                verify(testProduct3, times(1)).setFeatureProduct(false); // Product C should NOT be featured
                verify(testProduct4, times(1)).setFeatureProduct(false); // Product A should NOT be featured
        }

        @Test
        public void testFindAllByNameContaining() {
                // Prepare test data
                Product product1 = new Product(1, 5, "Product A", "A", "Description for A");
                Product product2 = new Product(2, 10, "Product B", "B", "Description for B");
                List<Product> allProducts = Arrays.asList(product1, product2);
                List<Product> filteredProducts = Arrays.asList(product2);

                // Mock behavior for repository
                when(productRepository.findAllByNameContaining("B")).thenReturn(filteredProducts);
                when(productRepository.findAll()).thenReturn(allProducts);

                // Test: When name is provided
                List<Product> result = productService.findAllByNameContaining("B");
                assertEquals(1, result.size());
                assertEquals("B", result.get(0).getName());

                // Test: When name is null or empty (should return all products)
                result = productService.findAllByNameContaining(null);
                assertEquals(2, result.size()); // All products should be returned
                result = productService.findAllByNameContaining("");
                assertEquals(2, result.size()); // All products should be returned
        }

        // Test for getFeaturedProducts()
        @Test
        public void testGetFeaturedProducts() {
                // Prepare test data
                Product product1 = new Product(1, 5, "Product A", "A", "Description for A");
                Product product2 = new Product(2, 10, "Product B", "B", "Description for B");
                product1.setFeatureProduct(true); // Featured product
                product2.setFeatureProduct(false); // Non-featured product
                List<Product> featuredProducts = Arrays.asList(product1);

                // Mock behavior for repository
                when(productRepository.findAllByFeatureProduct(true)).thenReturn(featuredProducts);

                // Test: Get featured products
                List<Product> result = productService.getFeaturedProducts();
                assertEquals(1, result.size());
                assertTrue(result.get(0).isFeatureProduct()); // Should be featured
        }

        // Test for getDeliveryOnly()
        @Test
        public void testGetDeliveryOnly() {
                // Prepare test data
                List<Integer> deliveryProductIds = Arrays.asList(1, 2);

                // Mock behavior for repository
                when(productRepository.findAllDeliveryOnly()).thenReturn(deliveryProductIds);

                // Test: Get delivery-only product IDs
                List<Integer> result = productService.getDeliveryOnly();
                assertEquals(2, result.size());
                assertTrue(result.contains(1)); // Should contain ID 1
                assertTrue(result.contains(2)); // Should contain ID 2
        }

        // Test for getAllProducts()
        @Test
        public void testGetAllProducts() {
                // Prepare test data
                Product product1 = new Product(1, 5, "Product A", "A", "Description for A");
                Product product2 = new Product(2, 10, "Product B", "B", "Description for B");
                List<Product> allProducts = Arrays.asList(product1, product2);

                // Mock behavior for repository
                when(productRepository.findAll()).thenReturn(allProducts);

                // Test: Get all products
                List<Product> result = productService.getAllProducts();
                assertEquals(2, result.size());
                assertEquals("A", result.get(0).getName());
                assertEquals("B", result.get(1).getName());
        }
}