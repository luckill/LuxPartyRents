package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Email.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.parser.Entity;
import java.util.HashMap;

@RestController
public class EmailController
{
    @Autowired
    private EmailService emailService;

    private HashMap<String,String> emailMap = new HashMap<>();

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/sendEmail")
    public String sendEmail(@RequestBody EmailDetails details)
    {
        return emailService.sendSimpleEmail(details);
    }

    @PostMapping("/sendEmailWithAttachment")
    public String sendMailWithAttachment(@RequestBody EmailDetails details)
    {
        return emailService.sendEmailWithAttachment(details);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        if (emailMap.containsKey(token)) {
            String email = emailMap.get(token);
            emailMap.remove(token); // Remove the token from the map after verification
            return ResponseEntity.ok("Email verified successfully for: " + email);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token. Please try again.");
        }
    }

    @PostMapping("/sendVerificationEmail")
    public String sendVerificationEmail(@RequestParam("email") String email, HttpServletRequest request) {
        // Generating a verification token
        String token = generateVerificationToken(email);
        if(!emailMap.containsKey(token)){
            emailMap.put(token,email);
        } else if(emailMap.containsValue(email) && (!emailMap.get(token).equals(email))){
            emailMap.remove(token);
            emailMap.put(token,email);
        }

        // Constructing the verification URL with the token
        //String verificationUrl = "http://localhost:8080/verify-email?token=" + token;

        String verificationUrl = "http://" + request.getServerName() + ":" + request.getServerPort()  + "/verify-email?token=" + token;

        // Creating the email details
        EmailDetails details = new EmailDetails();
        details.setRecipient(email);
        details.setSubject("Email Verification");
        details.setMessageBody("Please click the link to verify your email: " + verificationUrl);

        // Sending the email
        return emailService.sendSimpleEmail(details);
    }

    private String generateVerificationToken(String email) {
        // Generating a random verification token logic goes here
        return passwordEncoder.encode(email);
    }

}
