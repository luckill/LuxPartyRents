package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.TokenData;
import org.springframework.stereotype.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService
{
    private final Map<String, TokenData> tokenStore = new ConcurrentHashMap<>();

    public String generateToken(String email)
    {
        if (email == null)
        {
            throw new IllegalArgumentException("Email cannot be null");
        }
        String token = email + ":" + System.currentTimeMillis();
        String encodedToken = Base64.getEncoder().encodeToString(token.getBytes());

        TokenData tokenData = new TokenData(email, System.currentTimeMillis(), false);
        tokenStore.put(encodedToken, tokenData);

        return encodedToken;
    }

    public String validateToken(String token)
    {
        TokenData tokenData = tokenStore.get(token);

        if (tokenData == null || tokenData.isUsed()) {
            return null; // Token does not exist or has already been used
        }
        long expirationTime = (60 * 60 * 1000) / 2; // 30 minutes expiration time

        if (System.currentTimeMillis() - tokenData.getTimestamp() > expirationTime) {
            tokenStore.remove(token);
            return null;
        }
        return tokenData.getEmail();
    }

    public void markTokenAsUsed(String token)
    {
        TokenData tokenData = tokenStore.get(token);
        if (tokenData != null) {
            tokenData.setUsed(true);
        }
    }

    public Map<String, TokenData> getTokenStore()
    {
        return tokenStore;
    }
}
