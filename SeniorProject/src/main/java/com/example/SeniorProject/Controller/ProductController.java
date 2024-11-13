package com.example.SeniorProject.Controller;

import com.example.SeniorProject.DTOs.ProductDTO;
import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;

@Controller
@RequestMapping(path="/product")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(path="/addProduct") // Map ONLY POST Requests
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> addProduct (@RequestBody ProductDTO productDTO)
    {
        Product saved = productService.addProduct(productDTO);
        if(saved == null)
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping(path="/getAll")
    public @ResponseBody List<ProductDTO> getAllProducts(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchTerm) {
        List<Product> products = productService.getAllProducts(sortBy, searchType, searchTerm);

        return products.stream().map(this::mapToProductDTO).toList();
    }

    
    @PostMapping(path ="/update")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateProduct (@RequestBody ProductDTO productDTO)
    {
        Product updated = productService.updateProduct(productDTO);
        if(updated == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR!!! - product not found");
        }
        return ResponseEntity.ok("Product updated successfully");

    }


    @DeleteMapping (path ="/delete")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> deleteProduct (@RequestParam int id)
    {
        boolean delete = productService.deleteProduct(id);
        if(!delete)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error!!! - Order associated with this id is not found in the database");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully");
    }

    @GetMapping(path="/getByName")
    public ResponseEntity<?> getProductByName(@RequestParam String name)
    {
        Product result = productService.getProductByName(name);
        return ResponseEntity.status(HttpStatus.OK).body(this.mapToProductDTO(result));
    }
    @GetMapping(path="/getById")
    public ResponseEntity<?> getProductById(@RequestParam int id)
    {
        Product result = productService.getProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(this.mapToProductDTO(result));
    }

    

    // Meant for searching with the given column types
    @GetMapping(path="/search")
    public ResponseEntity<?> searchProducts(@RequestParam String type, @RequestParam String term)
    {
        List<Product> result = productService.searchProducts(type, term);

        if(result == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR!!! - product not found");
        }
        return ResponseEntity.ok(result.stream().map(this::mapToProductDTO).toList());
    }
    // Searches in Modal
    @GetMapping("/searchModal")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        List<Product> products = productService.findAllByNameContaining(name);
        return ResponseEntity.ok(products);
    }
    // Gets all featured items
    @GetMapping("/getFeatured")
    public ResponseEntity<List<Product>> getFeaturedProducts() {
        List<Product> featuredProducts = productService.getFeaturedProducts();
        return ResponseEntity.ok(featuredProducts);
    }

    @PostMapping("/updateFeaturedStatus")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateFeaturedStatus(@RequestBody List<Integer> featuredItemIds) {
        productService.updateFeaturedStatus(featuredItemIds);
        return ResponseEntity.ok("Featured status updated successfully.");
    }

    private ProductDTO mapToProductDTO(Product product)
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

    // New endpoint to fetch all products
    @GetMapping("/allProducts")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
}