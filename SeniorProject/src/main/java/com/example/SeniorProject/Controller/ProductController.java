package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Model.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/product")
public class ProductController {
        @Autowired
        private ProductRepository productRepository;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @PostMapping(path="/addProduct") // Map ONLY POST Requests
        public @ResponseBody String addProduct (@RequestBody String product) {
                // @ResponseBody means the returned String is the response, not a view name
                // @RequestParam means it is a parameter from the GET or POST request
                try {
                        Product n = objectMapper.readValue(product, Product.class);
                        productRepository.save(n);
                        return "Saved";
                }catch (Exception e)
                {
                        e.printStackTrace();
                        return "Error";
                }
        }

        @GetMapping(path="/getAll")
        public @ResponseBody Page<Product> getAllProducts(Pageable pageable) {
            return productRepository.findAll(pageable);
        }
        @PostMapping(path ="/update")
        public @ResponseBody String updateProduct (@RequestBody String product) {
                System.out.println(product);
                try {
                        Product n = objectMapper.readValue(product, Product.class);
                        productRepository.save(n);
                        return "Updated";
                }catch (Exception e)
                {
                        e.printStackTrace();
                        return "Error";
                }
        }

        @PostMapping(path ="/delete")
        public @ResponseBody String deleteProduct (@RequestBody String product) {
                try {
                        Product n = objectMapper.readValue(product, Product.class);
                        productRepository.deleteById(n.getId());
                        return "Deleted";
                }catch (Exception e)
                {
                        e.printStackTrace();
                        return "Error";
                }
        }

        @GetMapping(path="/getByName")
        public @ResponseBody Iterable<Product> getProductByName(@RequestBody String name) {
                // This returns a JSON or XML with the users
                try {
                        Product n = objectMapper.readValue(name, Product.class);
                        return productRepository.getProductByName(n.getName().toString());
                }catch (Exception e)
                {
                        e.printStackTrace();
                        return null;
                }
        }

        @GetMapping(path="/getById")
        public @ResponseBody Iterable<Product> getProductByName(@RequestParam int id) {
                // This returns a JSON or XML with the users
                try {
                        return productRepository.getProductById(id);
                }catch (Exception e)
                {
                        e.printStackTrace();
                        return null;
                }
        }
        @GetMapping("/names")
    public List<String> getProductNames() {
        return productRepository.findAllProductNames();
    }
}
