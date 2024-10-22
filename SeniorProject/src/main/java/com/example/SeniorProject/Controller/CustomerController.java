package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.*;

@RestController
@RequestMapping("/customer")
public class CustomerController
{
    @Autowired
    private CustomerService customerService;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/findAccount")
    public ResponseEntity<?> checkIfAccountExist(@RequestParam("email") String email)
    {
        try
        {
            Account account = customerService.checkIfAccountExist(email);
            return ResponseEntity.status(HttpStatus.OK).body(account);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @GetMapping("/getCustomer/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        try
        {
            Customer customer = customerService.getUserById(id);
            return ResponseEntity.status(HttpStatus.OK).body(customer);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @PutMapping("/updateCustomer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCustomer(@RequestBody Customer customer)
    {
        try
        {
            customerService.updateCustomer(customer);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @DeleteMapping("/deleteCustomer/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteCustomer(@PathVariable int id)
    {
        try
        {
            String message = customerService.deleteCustomer(id);
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }
}
