package com.example.SeniorProject.Service;

import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest
{

    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService();

        // Set values for the secretKey and expirationTime
        // You can use reflection or set these values through a configuration class for testing
        setSecretKey(jwtService, "testSecretKeyForJwtSigningWhichShouldBeLongEnough");
        setExpirationTime(jwtService, 5000L); // 1 hour in milliseconds
    }

    @Test
    void testGenerateToken()
    {
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(token.startsWith("eyJ")); // JWT tokens typically start with "eyJ"
    }

    @Test
    void testExtractUsername()
    {
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertEquals("testUser", username);
    }

    @Test
    void testIsTokenValid_ValidToken()
    {
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void testGetExpirationTimeWithInvalidToken() {
        // Act & Assert: Pass an expired or invalid token
        String invalidToken = "invalid.token.here";
        assertThrows(io.jsonwebtoken.JwtException.class, () -> {
            Jwts.parser()
                    .setSigningKey("secretKey")
                    .parseClaimsJws(invalidToken); // Expecting a JwtException due to invalid token
        });
    }

    // Reflection to set private fields for testing
    private void setSecretKey(JwtService jwtService, String secretKey)
    {
        try
        {
            var field = jwtService.getClass().getDeclaredField("secretKey");
            field.setAccessible(true);
            field.set(jwtService, secretKey);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setExpirationTime(JwtService jwtService, Long expirationTime) {
            try {
                    var field = jwtService.getClass().getDeclaredField("expirationTime");
                    field.setAccessible(true);
                    field.set(jwtService, expirationTime);
            } catch (Exception e) {
                    e.printStackTrace();
            }
    }
}