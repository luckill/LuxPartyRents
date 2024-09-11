package com.example.SeniorProject.Service;

import com.example.SeniorProject.Email.EmailDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.*;

@Service
public class EmailService
{
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;
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
}