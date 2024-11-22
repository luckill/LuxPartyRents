package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.*;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.*;

import java.util.*;

@Controller
@RequestMapping("/password")
public class passwordController
{
    private final PasswordService passwordService;

    public passwordController(PasswordService passwordService)
    {
        this.passwordService = passwordService;
    }

    @PostMapping("/send-reset-token")
    public ResponseEntity<?> sendResetToken(@RequestParam String email, HttpServletRequest request)
    {
        try
        {
            passwordService.sendResetToken(email, request);
            return ResponseEntity.status(HttpStatus.OK).body("Password reset instruction has been sent to your email address on file, please check your email.");
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<?> verifyResetToken(@RequestParam String token)
    {
        try
        {
            Map<String, String> response = passwordService.verifyResetToken(token);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody AccountInfo accountInfo, @RequestParam(required = true) String token)
    {
        try
        {
            passwordService.resetPassword(accountInfo, token);
            return ResponseEntity.status(HttpStatus.OK).body("Password has been successfully updated.");
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }
}
