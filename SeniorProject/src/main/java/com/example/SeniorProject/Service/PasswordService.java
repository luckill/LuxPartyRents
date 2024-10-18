package com.example.SeniorProject.Service;

import com.example.SeniorProject.DTOs.*;
import com.example.SeniorProject.Email.*;
import com.example.SeniorProject.Model.*;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.stereotype.*;
import org.springframework.web.server.*;

import java.util.*;

@Service
public class PasswordService
{
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    private final TokenService tokenService;
    private final EmailService emailService;

    public PasswordService(AccountRepository accountRepository, TokenService tokenService, EmailService emailService)
    {
        this.accountRepository = accountRepository;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    public void sendResetToken(String email, HttpServletRequest request)
    {
        String token = tokenService.generateToken(email);
        String url = "http://" + request.getServerName() + ":" + request.getServerPort()  + "/resetPassword?token=" + token;
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
    }

    public Map<String, String> verifyResetToken(String token)
    {
        String email = tokenService.validateToken(token);
        if (email == null)
        {
            Map<String,String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid or Expired token");
            return errorResponse;
        }

        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "Token is valid.");
        successResponse.put("email", email);
        return successResponse;
    }

    public void resetPassword(AccountInfo accountInfo, String token)
    {
        if (token == null || token.isEmpty())
        {
            throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing token.");
        }

        String email = tokenService.validateToken(token);
        if (email == null)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token.");
        }

        Account account = accountRepository.findAccountByEmail(accountInfo.getEmail());
        if (account == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found.");
        }

        String newPassword = passwordEncoder.encode(accountInfo.getPassword());
        account.setPassword(newPassword);
        account.setFailedLoginAttempt(0);
        account.setIsLocked(false);
        accountRepository.save(account);
        tokenService.markTokenAsUsed(token);
    }
}
