package com.example.SeniorProject.Service;

import com.example.SeniorProject.Email.EmailDetails;
import com.example.SeniorProject.Model.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.*;

import java.util.*;

@Service
public class EmailService
{
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private AccountRepository accountRepository;

    @Value("${spring.mail.username}")
    private String sender;

    private HashMap<String,String> emailMap = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public String sendSimpleEmail(EmailDetails details)
    {
        try
        {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(details.getRecipient());
            message.setSubject(details.getSubject());
            message.setText(details.getMessageBody());
            javaMailSender.send(message);
        }
        catch (MailSendException exception)
        {
            exception.printStackTrace();
            return "Error occur while sending the message!!!";
        }
        catch (MailAuthenticationException exception)
        {
            exception.printStackTrace();
            return "authentication failed!!!";
        }
        catch (MailException exception)
        {
            exception.printStackTrace();
            return"";
        }
        return "Message sent successfully";
    }

    public String sendEmailWithAttachment(EmailDetails details)
    {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try
        {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getMessageBody());
            mimeMessageHelper.setSubject(details.getSubject());

            FileSystemResource file = new FileSystemResource(details.getAttachment());
            mimeMessageHelper.addAttachment(file.getFilename(), file);
            javaMailSender.send(mimeMessage);
            return "Message sent successfully";
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    @GetMapping("/verify-email")
    public boolean verifyEmail(@RequestParam("token") String token)
    {
        if (emailMap.containsKey(token))
        {
            String email = emailMap.get(token);
            emailMap.remove(token); // Remove the token from the map after verification
            Account account = accountRepository.findAccountByEmail(email);
            if(account != null)
            {
                account.setVerified(true);
                accountRepository.save(account);
            }
            else
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account not found");
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    @PostMapping("/sendVerificationEmail")
    public void sendVerificationEmail(@RequestParam("email") String email, HttpServletRequest request)
    {
        // Generating a verification token
        String token = generateVerificationToken(email);
        if(!emailMap.containsKey(token))
        {
            emailMap.put(token,email);
        }
        else if(emailMap.containsValue(email) && (!emailMap.get(token).equals(email)))
        {
            emailMap.remove(token);
            emailMap.put(token,email);
        }

        String verificationUrl = "http://" + request.getServerName() + ":" + request.getServerPort()  + "/email/verify-email?token=" + token;

        // Creating the email details
        EmailDetails details = new EmailDetails();
        details.setRecipient(email);
        details.setSubject("Email Verification");
        details.setMessageBody("Please click the link to verify your email: " + verificationUrl);

        // Sending the email
        sendSimpleEmail(details);
    }

    // User/CX Email notifications
    public void sendCxPickupNotification ( Order order){
        //setting the up the email
        EmailDetails CxEmailDetails = new EmailDetails();
        CxEmailDetails.setRecipient(order.getCustomer().getEmail());
        CxEmailDetails.setSubject("Wedding Rental Pickup Reminder");

        //filling the email body
        String emailBody = "Thank you for coming to us for your rental needs!\n"
                + "Here is an important reminder for your rental pickup.\n"
                + order.getCustomer().getFirstName() + " "
                + order.getCustomer().getLastName() + " your Order, "
                + order.getId() + " pickup is on, " + order.getCreationDate();

        //sending email
        CxEmailDetails.setMessageBody(emailBody);
        sendSimpleEmail(CxEmailDetails);

    }//Pickup

    public void sendCxReturnNotification ( Order order){
        //setting the up the email
        EmailDetails CxEmailDetails = new EmailDetails();
        CxEmailDetails.setRecipient(order.getCustomer().getEmail());
        CxEmailDetails.setSubject("Wedding Rental Return Reminder");

        //filling the email body
        String emailBody = "I hope your special day was memory worthy!\n"
                + "Below is important return information for your rental\n"
                + order.getCustomer().getFirstName() + " "
                + order.getCustomer().getLastName() + " your Order, "
                + order.getId() + " return is on, " + order.getRentalTime();

        //sending email
        CxEmailDetails.setMessageBody(emailBody);
        sendSimpleEmail(CxEmailDetails);

    }//Return

    //Note that sendCxCanceledNotification requires a CanceledReason to be
    //passed into the function for the email to be complete.
    public void sendCxCanceledNotification ( String CanceledReason, Order order){
        //setting the up the email
        EmailDetails CxEmailDetails = new EmailDetails();
        CxEmailDetails.setRecipient(order.getCustomer().getEmail());
        CxEmailDetails.setSubject(" Important Wedding Rental Order Update");

        //filling the email body
        String emailBody = order.getCustomer().getFirstName() + " "
                + order.getCustomer().getLastName() + " your Order, "
                + order.getId() + " has been canceled for, " + CanceledReason
                + "\n Please reach out to our service number for any questions.";

        //sending email
        CxEmailDetails.setMessageBody(emailBody);
        sendSimpleEmail(CxEmailDetails);

    }//Canceled

    private String generateVerificationToken(String email)
    {
        // Generating a random verification token logic goes here
        return passwordEncoder.encode(email);
    }
}