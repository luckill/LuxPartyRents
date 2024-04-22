package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Email.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmailController
{
    @Autowired
    private EmailService emailService;

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
    public String verifyEmail(@RequestParam("token") String token)
    {

        if (token.equals("validToken123")) {
            return "Email verified successfully!!!";
        } else {
            return "Invalid or expired token";
        }
    }

    @PostMapping("/sendVerificationEmail")
    public String sendVerificationEmail(@RequestParam("email") String email) {
        // Generating a verification token
        String token = generateVerificationToken();

        // Constructing the verification URL with the token
        String verificationUrl = "http://localhost:8080/verify-email?token=" + token;

        // Creating the email details
        EmailDetails details = new EmailDetails();
        details.setRecipient(email);
        details.setSubject("Email Verification");
        details.setMessageBody("Please click the link to verify your email: " + verificationUrl);

        // Sending the email
        return emailService.sendSimpleEmail(details);
    }

    private String generateVerificationToken() {
        // Generating a random verification token logic goes here
        return "validToken123";
    }

}
