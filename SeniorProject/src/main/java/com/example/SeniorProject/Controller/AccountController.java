package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.Account;
import com.example.SeniorProject.Model.AccountRepository;
import com.example.SeniorProject.Model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {

    
    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/getAccount/{id}")
    public @ResponseBody Iterable<Account>getAccountById(@PathVariable int id) {
        return accountRepository.getAccountById(id);
    }

    @DeleteMapping("/deleteAccount/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable int id) {
        accountRepository.deleteById(id);
        return ResponseEntity.ok().build();
        
    }
}