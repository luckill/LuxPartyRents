package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.SecretsManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;



@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SecretsManagerService secretsManagerService;


    // Existing method to get account by id
    @GetMapping("/getAccount/{id}")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody Iterable<Account> getAccountById(@PathVariable int id) {
        return accountRepository.getAccountById(id);
    }

    // Existing method to delete an account
    @DeleteMapping("/deleteAccount/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAccount(@PathVariable int id) {
        accountRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Existing method to turn a user into an admin
    @PostMapping("/turnAdmin/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> turnAdmin(@PathVariable int id, @RequestParam String apiKey) {
        String key = secretsManagerService.getSecretValue("adminAccountKey");
        if(key == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No API key found in AWS Secret Manager.");
        }
        if(!key.equals(apiKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("API key does not match.");
        }

        Account account = accountRepository.getAccountById(id).get(0);
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);
        if (optionalRole.isPresent()) {
            account.setRole(optionalRole.get());
        }
        accountRepository.save(account);
        return ResponseEntity.ok().build();
    }

}
