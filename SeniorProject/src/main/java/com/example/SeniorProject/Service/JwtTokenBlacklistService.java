package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
public class JwtTokenBlacklistService
{
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;

    public JwtTokenBlacklistService(BlacklistedTokenRepository blacklistedTokenRepository, JwtService jwtService)
    {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.jwtService = jwtService;
    }

    public void blacklistToken(String token)
    {
        LocalDateTime createDate = LocalDateTime.now();
        LocalDateTime expiredDate = createDate.plusSeconds(jwtService.getRemainingValidTime(token));
        BlacklistedToken blacklistedToken = new BlacklistedToken(token, createDate, expiredDate);
        blacklistedTokenRepository.save(blacklistedToken);
    }

    public boolean isTokenBlacklisted(String token)
    {
        return blacklistedTokenRepository.existsByToken(token);
    }

    public void invalidateTokensForUser(String username) {
        // Optionally, implement logic to invalidate tokens based on the user, if necessary.
        // If you need to associate tokens with users, you could modify the BlacklistedToken model.
    }

    public ResponseEntity<?> deleteExpiredTokens(LocalDateTime localDateTime)
    {
        List<BlacklistedToken> tokens = blacklistedTokenRepository.findExpiredBlacklistTokens(LocalDateTime.now());
        blacklistedTokenRepository.deleteAll(tokens);
        return ResponseEntity.ok().build();
    }
}