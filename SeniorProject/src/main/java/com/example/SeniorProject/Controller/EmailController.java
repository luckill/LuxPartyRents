package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Email.*;
import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.*;

import javax.validation.constraints.Email;
import java.util.HashMap;

@RestController
@RequestMapping(path="/email")
public class EmailController
{
    @Autowired
    private EmailService emailService;

    @PostMapping("/sendEmail")
    public ResponseEntity<?> sendEmail(@RequestBody EmailDetails details)
    {
        try
        {
            String response = emailService.sendSimpleEmail(details);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @PostMapping("/sendEmailWithAttachment")
    public ResponseEntity<?> sendMailWithAttachment(@RequestBody EmailDetails details)
    {
        try
        {
            String response = emailService.sendEmailWithAttachment(details);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token)
    {
        try
        {
            if(emailService.verifyEmail(token))
            {
                return ResponseEntity.ok("your email has been verified successfully.");
            }
            else
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token. Please try again.");
            }
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @PostMapping("/sendVerificationEmail")
    public ResponseEntity<?> sendVerificationEmail(@RequestParam("email") String email, HttpServletRequest request)
    {
        try
        {
            emailService.sendVerificationEmail(email, request);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }
}
