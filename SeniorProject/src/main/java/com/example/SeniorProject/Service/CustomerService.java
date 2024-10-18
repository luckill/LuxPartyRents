package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.*;

import java.io.*;

@Service
public class CustomerService
{
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;

    public Account checkIfAccountExist(String email)
    {
        Account account = accountRepository.findAccountByEmail(email);
        if (account != null)
        {
            return account;
        }
        else
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
    }

    public Customer getUserById(int id)
    {
        Customer customer = customerRepository.findById(id).orElse(null);
        if(customer == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        return customer;
    }

    public void updateCustomer(Customer customer)
    {
        try
        {
            // Fetch existing customer from the database
            Customer existingCustomer = customerRepository.findById(customer.getId()).orElse(null);

            if (existingCustomer == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }

            // Update fields if they are not null
            if (customer.getFirstName() != null)
            {
                existingCustomer.setFirstName(customer.getFirstName());
            }
            if (customer.getLastName() != null)
            {
                existingCustomer.setLastName(customer.getLastName());
            }
            if (customer.getPhone() != null)
            {
                existingCustomer.setPhone(customer.getPhone());
            }
            if (customer.getAddress() != null)
            {
                existingCustomer.setAddress(customer.getAddress());
            }
            if (customer.getCity() != null)
            {
                existingCustomer.setCity(customer.getCity());
            }
            if (customer.getState() != null)
            {
                existingCustomer.setState(customer.getState());
            }
            if (customer.getZipCode() != null)
            {
                existingCustomer.setZipCode(customer.getZipCode());
            }

            // Save updated customer
            customerRepository.save(existingCustomer);
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the customer");
        }
    }

    public ResponseEntity<?> deleteCustomer(int id)
    {
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }
        customerRepository.deleteById(id);
        return ResponseEntity.ok("Customer has been successfully deleted");
    }
}
