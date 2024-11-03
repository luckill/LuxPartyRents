package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Model.RentalRepository;
import com.example.SeniorProject.Service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(path = "/rental")
public class RentalController { 
    @Autowired
    private RentalRepository rentalRepository;
    @Autowired
    private RentalService rentalService;

    // New search method that includes both type and regular search
    @GetMapping(path = "/getAll")
    public @ResponseBody Page<Product> getAllProducts(@RequestParam(value = "kw", required = false) String keyword, @RequestParam(value = "type", required = false) String type, Pageable pageable)
    {
        return rentalService.getAllProducts(keyword, type, pageable);
    }

    // New search method
    @GetMapping(path = "/search")
    public @ResponseBody Page<Product> searchProducts(@RequestParam(name = "kw", required = false) String keyword, @RequestParam(name = "type", required = false) String type, Pageable pageable)
    {
        return rentalService.searchProducts(keyword, type, pageable);
    }

    @GetMapping(path = "/types")
    public @ResponseBody List<String> getDistinctTypes() {
        return rentalService.getDistinctProductTypes();
    }
}