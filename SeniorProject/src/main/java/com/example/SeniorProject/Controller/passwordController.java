package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Email.*;
import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.*;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/password")
public class passwordController {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    private final TokenService tokenService;
    private final EmailService emailService;

    public passwordController(AccountRepository accountRepository, TokenService tokenService, EmailService emailService) {
        this.accountRepository = accountRepository;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @PostMapping("/send-reset-token")
    public ResponseEntity<?> sendResetToken(@RequestParam String email) {
        String token = tokenService.generateToken(email);
        String url = "http://localhost:8080/resetPassword?token=" + token;
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(email);
        emailDetails.setSubject("Reset Token");
        emailDetails.setMessageBody(
                "Please click on the link to reset your password: " + url +
                        "\n\nThe link will expire after 30 minutes." +
                        "\n\nIf you did not request this password reset, please ignore this email." +
                        "\nFor security reasons, do not share this link with anyone."
        );
        emailService.sendSimpleEmail(emailDetails);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<?> verifyResetToken(@RequestParam String token) {
        String email = tokenService.validateToken(token);
        if (email == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "Token is valid.");
        successResponse.put("email", email);
        return ResponseEntity.ok(successResponse);
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody AccountInfo accountInfo, @RequestParam(required = true) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing token.");
        }

        String email = tokenService.validateToken(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }

        Account account = accountRepository.findAccountByEmail(accountInfo.getEmail());
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found.");
        }

        String newPassword = passwordEncoder.encode(accountInfo.getPassword());
        account.setPassword(newPassword);
        account.setFailedLoginAttempt(0);
        account.setIsLocked(false);
        accountRepository.save(account);
        tokenService.markTokenAsUsed(token);
        return ResponseEntity.status(HttpStatus.OK).body("Password reset successfully.");
    }
}
