package com.example.SeniorProject.Controller;

import com.example.SeniorProject.*;
import com.example.SeniorProject.Model.*;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
        String newPassword = passwordEncoder.encode(account.getPassword());
        account.setPassword(newPassword);
        accountRepository.save(account);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private void createCustomer(Customer customer, Account account)
    {
        this.accountRepository.save(account);
        customer.setAccount(account);
        this.customerRepository.save(customer);
    }
}
