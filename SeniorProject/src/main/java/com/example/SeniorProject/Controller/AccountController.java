package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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
    private JwtService jwtService;

    @GetMapping("/getAccount/{id}")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody Iterable<Account>getAccountById(@PathVariable int id) {
        return accountRepository.getAccountById(id);
    }

    @DeleteMapping("/deleteAccount/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAccount(@PathVariable int id) {
        accountRepository.deleteById(id);
        return ResponseEntity.ok().build();
        
    }

    @PostMapping("/turnAdmin/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> turnAdmin(@PathVariable int id) {
        Account account = accountRepository.getAccountById(id).get(0);
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);
        if (!optionalRole.isEmpty()) {
            account.setRole(optionalRole.get());
        }
        accountRepository.save(account);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getAccountInfo")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");

        // Validate the token and extract username
        if (jwtService.isTokenExpired(jwtToken))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Your session has expired, please log in again");
        }

        String username = jwtService.extractUsername(jwtToken);
        Account account = accountRepository.findAccountByEmail(username);

        if (account == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No account found with email " + username);
        }

        return ResponseEntity.ok(account);
    }
}