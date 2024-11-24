package com.example.SeniorProject.Controller;

import com.example.SeniorProject.DTOs.CustomerDTO;
import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.*;

import java.util.Collections;

@RestController
@RequestMapping("/customer")
public class CustomerController
{
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private JwtService jwtService;


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

    @PostMapping("/updateCustomer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCustomer(@RequestBody CustomerDTO customer)
    {
        try
        {
            customerService.updateCustomer(customer);
            return ResponseEntity.ok("Customer has been successfully updated");
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @DeleteMapping("/deleteCustomer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteCustomer(@RequestBody CustomerDTO customer) {
        try
        {
            Customer costAccount=customerRepository.findCustomersByEmail(customer.getEmail());
            Account account=accountRepository.findAccountByEmail(customer.getEmail());
            if (account!=null && costAccount != null) {
                customerRepository.deleteById(costAccount.getId());
                accountRepository.deleteById(account.getId());

            }
            return ResponseEntity.ok("Customer has been successfully deleted");
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @GetMapping("/getCustomerInfo")
    public ResponseEntity<?> getCustomerInfo(@RequestHeader("Authorization") String token)
    {
        try
        {
            return ResponseEntity.ok(customerService.getCustomerInfo(token));
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }
}
