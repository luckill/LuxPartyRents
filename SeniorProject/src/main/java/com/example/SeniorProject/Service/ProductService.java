package com.example.SeniorProject.Service;

import com.example.SeniorProject.DTOs.ProductDTO;
import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Model.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ProductService {
        @Autowired
        private ProductRepository productRepository;

        public Product addProduct (ProductDTO productDTO)
        {
                Product product = new Product(productDTO.getQuantity(), productDTO.getPrice(), productDTO.getType(), productDTO.getName(), productDTO.getDescription());
                Product saved = productRepository.save(product);
                return saved;
        }

        public List<Product> getAllProducts(String sortBy, String searchType, String searchTerm) {
                List<Product> products;

                // Filter based on searchType and searchTerm
                if (searchType != null && !searchType.isEmpty() && searchTerm != null && !searchTerm.isEmpty()) {
                        switch (searchType.toLowerCase()) {
                                case "name":
                                        products = productRepository.findAllByNameContaining(searchTerm);
                                        break;
                                case "type":
                                        products = productRepository.findAllByTypeContaining(searchTerm);
                                        break;
                                case "id":
                                        products = productRepository.findAllByIdContaining(Integer.valueOf(searchTerm));
                                        break;
                                default:
                                        products = productRepository.findAll();
                                        break;
                        }
                } else {
                        products = productRepository.findAll();
                }

                // Handle sorting
                if (sortBy != null && !sortBy.isEmpty()) {
                        switch (sortBy.toLowerCase()) {
                                case "id":
                                        products.sort(Comparator.comparingInt(Product::getId));
                                        break;
                                case "name":
                                        products.sort(Comparator.comparing(Product::getName));
                                        break;
                                case "price":
                                        products.sort(Comparator.comparingDouble(Product::getPrice));
                                        break;
                                case "quantity":
                                        products.sort(Comparator.comparingInt(Product::getQuantity));
                                        break;
                                case "type":
                                        products.sort(Comparator.comparing(Product::getType));
                                        break;
                                default:
                                        break;
                        }
                }

                return products;
        }

        public Product updateProduct (ProductDTO productDTO)
        {
                Product product = productRepository.findById(productDTO.getId()).orElse(null);
                if(product == null)
                {
                        return null;
                }
                if(productDTO.getName() != null)
                {
                        product.setName(productDTO.getName());
                }
                if (productDTO.getDescription() != null)
                {
                        product.setDescription(productDTO.getDescription());
                }
                if (productDTO.getType() != null)
                {
                        product.setType(productDTO.getType());
                }
                if (productDTO.getPrice() != 0)
                {
                        product.setPrice(productDTO.getPrice());
                }
                if (productDTO.getQuantity() != 0)
                {
                        product.setQuantity(productDTO.getQuantity());
                }
                productRepository.save(product);
                return product;
        }


        public boolean deleteProduct (int id)
        {

                Product product = productRepository.findById(id).orElse(null);
                if (product == null)
                {
                        return false;
                }
                productRepository.deleteById(id);
                return true;

        }

        public Product getProductByName(String name)
        {
                Product product = productRepository.getProductByName(name);
                if(product == null)
                {
                        return null;
                }
                return product;
        }

        public Product getProductById(int id)
        {
                return productRepository.findById(id).orElse(null);
        }

        // New function to change the featured in this list
        public void updateFeaturedStatus(List<Integer> featuredItemIds) {
                List<Product> allProducts = productRepository.findAll();
                
                for (Product product : allProducts) {
                    boolean isFeatured = featuredItemIds.contains(product.getId());
                    product.setFeatureProduct(isFeatured);  // Assuming 'featureProduct' is a boolean property
                    productRepository.save(product);  // Update the product in the database
                }
        }

        public ProductDTO mapToProductDTO(Product product)
        {
                ProductDTO productDTO = new ProductDTO();
                productDTO.setId(product.getId());
                productDTO.setName(product.getName());
                productDTO.setPrice(product.getPrice());
                productDTO.setType(product.getType());
                productDTO.setQuantity(product.getQuantity());
                productDTO.setDescription(product.getDescription());
                return productDTO;
        }


        // Meant for searching with the given column types
        public List<Product> searchProducts( String type,  String term) {
                List<Product> products = productRepository.findAll();
                List<Product> result = new ArrayList<>();

                switch (type.toLowerCase()) {
                        case "name":
                                result = products.stream()
                                        .filter(product -> product.getName().toLowerCase().contains(term.toLowerCase()))
                                        .toList();
                                break;
                        case "type":
                                result = products.stream()
                                        .filter(product -> product.getType().toLowerCase().contains(term.toLowerCase()))
                                        .toList();
                                break;
                        case "id":
                                try {
                                        int id = Integer.parseInt(term);
                                        result = products.stream()
                                                .filter(product -> product.getId() == id)
                                                .toList();
                                } catch (NumberFormatException e) {
                                        return null;
                                }
                                break;
                        default:
                                return null;
                }

                return result;
        }

        public List<Product> findAllByNameContaining(String name) {
                if (name != null && !name.isEmpty()) {
                        return productRepository.findAllByNameContaining(name);
                } else {
                        return productRepository.findAll();
                }
                
        }

        public List<Product> getFeaturedProducts() {
                return productRepository.findAllByFeatureProduct(true);
        }

        public List<Integer> getDeliveryOnly() {
                return productRepository.findAllDeliveryOnly();
            }

        public List<Product> getAllProducts() {
                return productRepository.findAll(); // Fetch all products from the database
        }        
        
}
