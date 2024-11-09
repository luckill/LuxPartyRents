package com.example.SeniorProject.Controller;

import com.example.SeniorProject.DTOs.CustomerDTO;
import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.*;
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

    @PutMapping("/updateCustomer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCustomer(@RequestBody CustomerDTO customer)
    {

        try {
            customerService.updateCustomer(customer);
            return ResponseEntity.ok("Customer has been successfully updated");
        }catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
        // Fetch existing customer from the database

    }



    @DeleteMapping("/deleteCustomer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteCustomer(@RequestBody CustomerDTO customer) {
        Account account=accountRepository.findAccountByEmail(customer.getEmail());
        Account costAccount=customerRepository.findAccountByCustomerName(customer.getFirstName(), customer.getLastName());
        if (account!=null && costAccount != null) {
            accountRepository.deleteById(account.getId());
            customerRepository.deleteById(costAccount.getId());
        }


        return ResponseEntity.ok("Customer has been successfully deleted");
    }

    @GetMapping("/getCustomerInfo")
    public ResponseEntity<?> getCustomerInfo(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");

        // Validate the token and extract username
        if (jwtService.isTokenExpired(jwtToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Your session has expired, please log in again");
        }

        String username = jwtService.extractUsername(jwtToken);
        Account account = accountRepository.findAccountByEmail(username);
        Customer customer = customerRepository.findCustomersByEmail(account.getEmail());

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No account found with email " + username);
        }

        // Map the Customer entity to CustomerDTO
        CustomerDTO customerDTO = new CustomerDTO(customer.getFirstName(), customer.getLastName(), customer.getEmail(), customer.getPhone());

        // Add any other fields from Customer to the DTO as needed

        return ResponseEntity.ok(customerDTO);
    }
}
