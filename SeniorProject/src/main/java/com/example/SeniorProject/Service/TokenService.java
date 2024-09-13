package com.example.SeniorProject.Service;

import org.springframework.stereotype.*;
import java.util.*;

@Service
public class TokenService
{
    public String generateToken(String email)
    {
        String token = email + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(token.getBytes());
    }
    
    public String validateToken(String token)
    {
        String tokenData = new String(Base64.getDecoder().decode(token));
        String[] parts = tokenData.split(":");
        String email = parts[0];
        long timestamp = Long.parseLong(parts[1]);
        long expirationDate = (60 * 60 * 1000)/2;//30 minutes expiration time

        if(System.currentTimeMillis() - timestamp > expirationDate)
        {
            return null;
        }
        return email;
    }
}
