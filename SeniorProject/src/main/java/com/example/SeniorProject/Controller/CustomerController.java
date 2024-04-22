package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/customer")
public class CustomerController
{
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    
    private final ObjectMapper mapper = new ObjectMapper();
   
    @PostMapping("/login")
    public ResponseEntity<?> Login(@RequestBody AccountInfo accountInfo)
    {
        String email = accountInfo.getEmail();
        String password = accountInfo.getPassword();
        if(passwordEncoder.matches(password, accountRepository.findAccountByEmail(email).getPassword()))
        {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        else
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/findAccount")
    public ResponseEntity<?> checkIfAccountExist(@RequestParam("email") String email)
    {
        Account account = accountRepository.findAccountByEmail(email);
        if (account != null)
        {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody AccountInfo accountInfo)
    {
        Account account = accountRepository.findAccountByEmail(accountInfo.getEmail());
        String newPassword = passwordEncoder.encode(accountInfo.getPassword());
        account.setPassword(newPassword);
        accountRepository.save(account);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/getCustomer/{id}")
    public @ResponseBody Iterable<Customer> getUserById(@PathVariable int id) {
        return customerRepository.getCustomerById(id);
    }

    @PutMapping("/updateCustomer")
    public @ResponseBody ResponseEntity<?> updateCustomer(@RequestBody Customer customer)
    {
        try
        {
            ObjectNode overrides = mapper.createObjectNode();
            String firstName = customer.getFirstName();
            String lastName = customer.getLastName();
            String phone = customer.getPhone();
            String address = customer.getAddress();
            String city = customer.getCity();
            String state = customer.getState();
            String zipCode = customer.getZipCode();
            if(customer.getFirstName() != null)
            {
                overrides.put("firstName", firstName);
            }
            if(customer.getLastName() != null)
            {
                overrides.put("lastName", lastName);
            }
            if(customer.getPhone() != null)
            {
                overrides.put("phone", phone);
            }
            if(customer.getAddress() != null)
            {
                overrides.put("address", address);
            }
            if (customer.getCity() != null)
            {
                overrides.put("city", city);

            }
            if (customer.getState() != null)
            {
                overrides.put("state", state);

            }
            if (customer.getZipCode() != null)
            {
                overrides.put("zipCode", zipCode);

            }
            ObjectReader reader = mapper.readerForUpdating(customer);
            customerRepository.save(reader.readValue(overrides));

            return ResponseEntity.ok("customer updated successfully");
        }
        catch (JsonProcessingException exception)
        {
            exception.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }
    }

    @DeleteMapping("/deleteCustomer/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable int id) { 
        Account account=accountRepository.getReferenceById(id);
        if (account!=null) {  
        }
        int accountId=account.getId();
        accountRepository.deleteById(accountId);

        customerRepository.deleteById(id);
        return ResponseEntity.ok("Customer has been successfully deleted");
    }
    private void createCustomer(Customer customer, Account account)
    {
        this.accountRepository.save(account);
        customer.setAccount(account);
        this.customerRepository.save(customer);
    }

}
