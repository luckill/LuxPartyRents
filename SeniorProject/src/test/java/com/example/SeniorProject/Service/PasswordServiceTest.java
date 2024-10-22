package com.example.SeniorProject.Service;

import com.example.SeniorProject.Email.*;
import com.example.SeniorProject.Model.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.web.server.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PasswordServiceTest
{

    private PasswordService passwordService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private HttpServletRequest request;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordService = new PasswordService(accountRepository, tokenService, emailService);
    }

    @Test
    public void testSendResetToken_Success() {
        String email = "test@example.com";
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);
        when(tokenService.generateToken(email)).thenReturn("validToken");

        passwordService.sendResetToken(email, request);

        ArgumentCaptor<EmailDetails> emailDetailsCaptor = ArgumentCaptor.forClass(EmailDetails.class);
        verify(emailService).sendSimpleEmail(emailDetailsCaptor.capture());
        assertEquals(email, emailDetailsCaptor.getValue().getRecipient());
        assertTrue(emailDetailsCaptor.getValue().getMessageBody().contains("http://localhost:8080/resetPassword?token=validToken"));
    }

    @Test
    public void testSendResetToken_NullEmail() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            passwordService.sendResetToken(null, request);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("missing email", exception.getReason());
    }

    @Test
    public void testVerifyResetToken_ValidToken() {
        String token = "validToken";
        String expectedEmail = "test@example.com";
        when(tokenService.validateToken(token)).thenReturn(expectedEmail);

        Map<String, String> response = passwordService.verifyResetToken(token);

        assertEquals("Token is valid.", response.get("message"));
        assertEquals(expectedEmail, response.get("email"));
    }

    @Test
    public void testVerifyResetToken_InvalidToken() {
        String token = "invalidToken";
        when(tokenService.validateToken(token)).thenReturn(null);

        Map<String, String> response = passwordService.verifyResetToken(token);

        assertEquals("Invalid or Expired token", response.get("message"));
    }

    @Test
    public void testVerifyResetToken_NullToken() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            passwordService.verifyResetToken(null);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("missing token", exception.getReason());
    }

    @Test
    public void testResetPassword_Valid()
    {
        String email = "test@example.com";
        String token = "validToken";
        AccountInfo accountInfo = new AccountInfo(email, "newPassword");
        Account account = new Account();
        account.setEmail(email);
        account.setPassword("oldPassword");

        when(tokenService.validateToken(token)).thenReturn(email);
        when(accountRepository.findAccountByEmail(email)).thenReturn(account);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        passwordService.resetPassword(accountInfo, token);

        assertTrue(passwordEncoder.matches("newPassword", account.getPassword()));
        assertFalse(account.isLocked());
        assertEquals(0, account.getFailedLoginAttempt());
        verify(tokenService).markTokenAsUsed(token);
    }

    @Test
    public void testResetPassword_MissingToken() {
        AccountInfo accountInfo = new AccountInfo("test@example.com", "newPassword");
        assertThrows(ResponseStatusException.class, () -> {
            passwordService.resetPassword(accountInfo, null);
        });
    }

    @Test
    public void testResetPassword_InvalidToken() {
        String email = "test@example.com";
        String token = "invalidToken";
        AccountInfo accountInfo = new AccountInfo(email, "newPassword");

        when(tokenService.validateToken(token)).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            passwordService.resetPassword(accountInfo, token);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid or expired token.", exception.getReason());
    }

    @Test
    public void testResetPassword_AccountNotFound() {
        String email = "notfound@example.com";
        String token = "validToken";
        AccountInfo accountInfo = new AccountInfo(email, "newPassword");

        when(tokenService.validateToken(token)).thenReturn(email);
        when(accountRepository.findAccountByEmail(email)).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            passwordService.resetPassword(accountInfo, token);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found.", exception.getReason());
    }

    @Test
    public void testResetPassword_ValidTokenButAccountInfoNull()
    {
        String token = "validToken";
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            passwordService.resetPassword(null, token);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Missing account info.", exception.getReason());
    }

    @Test
    public void testResetPassword_EmptyToken() {
        String email = "test@example.com";
        AccountInfo accountInfo = new AccountInfo(email, "newPassword");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        {
            passwordService.resetPassword(accountInfo, "");
        });
        assertEquals(HttpStatus.BAD_REQUEST, ((ResponseStatusException) exception).getStatusCode());
        assertEquals("Missing token.", exception.getReason());
    }
}
