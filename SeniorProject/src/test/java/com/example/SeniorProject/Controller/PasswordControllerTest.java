package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.AccountInfo;
import com.example.SeniorProject.Service.PasswordService;
import com.example.SeniorProject.Service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(passwordController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordService passwordService;

    @MockBean
    private JwtService jwtService; // Mock JwtService to fix dependency issues

    @MockBean
    private HttpServletRequest httpServletRequest;

    @Test
    void testSendResetToken_Success() throws Exception {
        // Arrange
        doNothing().when(passwordService).sendResetToken(eq("test@example.com"), any(HttpServletRequest.class));

        // Act & Assert
        mockMvc.perform(post("/password/send-reset-token")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset instruction has been sent to your email address on file, please check your email."));
    }

    @Test
    void testSendResetToken_MissingEmail() throws Exception {
        // Arrange
        doThrow(new RuntimeException("missing email"))
                .when(passwordService).sendResetToken(eq(""), any(HttpServletRequest.class));

        // Act & Assert
        mockMvc.perform(post("/password/send-reset-token")
                        .param("email", ""))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testVerifyResetToken_Success() throws Exception {
        // Arrange
        Map<String, String> response = new HashMap<>();
        response.put("message", "Token is valid.");
        response.put("email", "test@example.com");
        when(passwordService.verifyResetToken("valid-token")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/password/verify-reset-token")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "message": "Token is valid.",
                            "email": "test@example.com"
                        }
                        """));
    }

    @Test
    void testVerifyResetToken_InvalidToken() throws Exception {
        // Arrange
        Map<String, String> response = new HashMap<>();
        response.put("message", "Invalid or Expired token");
        when(passwordService.verifyResetToken("invalid-token")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/password/verify-reset-token")
                        .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "message": "Invalid or Expired token"
                        }
                        """));
    }

    @Test
    void testResetPassword_Success() throws Exception {
        // Arrange
        AccountInfo accountInfo = new AccountInfo("test@example.com", "newPassword");
        doNothing().when(passwordService).resetPassword(eq(accountInfo), eq("valid-token"));

        // Act & Assert
        mockMvc.perform(put("/password/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "newPassword"
                                }
                                """)
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Password has been successfully updated."));
    }

    @Test
    void testResetPassword_InvalidToken() throws Exception {
        // Arrange
        AccountInfo accountInfo = new AccountInfo("test@example.com", "newPassword");
        doNothing().when(passwordService).resetPassword(eq(accountInfo), eq("invalid-token"));

        // Act & Assert
        mockMvc.perform(put("/password/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "test@example.com",
                                "password": "newPassword"
                            }
                            """)
                        .param("token", "invalid-token"))
                .andExpect(status().isOk()) // Expect 200 OK because the controller doesn't differentiate errors
                .andExpect(content().string("Password has been successfully updated."));
    }

    @Test
    void testResetPassword_MissingToken() throws Exception {
        // Arrange
        AccountInfo accountInfo = new AccountInfo("test@example.com", "newPassword");
        doNothing().when(passwordService).resetPassword(eq(accountInfo), eq(""));

        // Act & Assert
        mockMvc.perform(put("/password/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "test@example.com",
                                "password": "newPassword"
                            }
                            """)
                        .param("token", ""))
                .andExpect(status().isOk()) // Expect 200 OK because the controller doesn't check for missing tokens
                .andExpect(content().string("Password has been successfully updated."));
    }

    @Test
    void testSendResetToken_ResponseStatusException() throws Exception {
        // Arrange
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing email"))
                .when(passwordService).sendResetToken(eq(""), any(HttpServletRequest.class));

        // Act & Assert
        mockMvc.perform(post("/password/send-reset-token")
                        .param("email", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("missing email"));
    }

    @Test
    void testVerifyResetToken_ResponseStatusException() throws Exception {
        // Arrange
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing token"))
                .when(passwordService).verifyResetToken(eq(""));

        // Act & Assert
        mockMvc.perform(get("/password/verify-reset-token")
                        .param("token", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("missing token"));
    }

    @Test
    void testResetPassword_ResponseStatusException() throws Exception {
        // Arrange
        AccountInfo accountInfo = new AccountInfo("test@example.com", "newPassword");
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"))
                .when(passwordService).resetPassword(eq(accountInfo), eq("invalid-token"));

        // Act & Assert
        mockMvc.perform(put("/password/resetPassword")
                        .param("token", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "test@example.com",
                                "password": "newPassword"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid or expired token"));
    }

}
