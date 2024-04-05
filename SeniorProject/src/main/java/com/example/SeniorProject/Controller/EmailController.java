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
}
