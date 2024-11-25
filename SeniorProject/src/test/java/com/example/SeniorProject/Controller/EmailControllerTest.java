package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Email.EmailDetails;
import com.example.SeniorProject.Service.EmailService;
import com.example.SeniorProject.Service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private HttpServletRequest httpServletRequest;

    @Test
    void testSendEmail_Success() throws Exception {
        when(emailService.sendSimpleEmail(any(EmailDetails.class))).thenReturn("Message sent successfully");

        mockMvc.perform(post("/email/sendEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "recipient": "test@example.com",
                                "subject": "Test Subject",
                                "messageBody": "Test Body"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("Message sent successfully"));
    }

    @Test
    void testSendEmail_Failure() throws Exception {
        doThrow(new RuntimeException("Error occurred"))
                .when(emailService).sendSimpleEmail(any(EmailDetails.class));

        mockMvc.perform(post("/email/sendEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "recipient": "test@example.com",
                                "subject": "Test Subject",
                                "messageBody": "Test Body"
                            }
                        """))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSendEmail_ResponseStatusException() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request"))
                .when(emailService).sendSimpleEmail(any(EmailDetails.class));

        mockMvc.perform(post("/email/sendEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "recipient": "test@example.com",
                                "subject": "Test Subject",
                                "messageBody": "Test Body"
                            }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request"));
    }

    @Test
    void testSendVerificationEmail_Success() throws Exception {
        doNothing().when(emailService).sendVerificationEmail(eq("test@example.com"), any(HttpServletRequest.class));

        mockMvc.perform(post("/email/sendVerificationEmail")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testSendVerificationEmail_Failure() throws Exception {
        doThrow(new RuntimeException("Verification error"))
                .when(emailService).sendVerificationEmail(eq("test@example.com"), any(HttpServletRequest.class));

        mockMvc.perform(post("/email/sendVerificationEmail")
                        .param("email", "test@example.com"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSendVerificationEmail_MissingEmail() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/email/sendVerificationEmail"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testVerifyEmail_Success() throws Exception {
        when(emailService.verifyEmail("valid-token")).thenReturn(true);

        mockMvc.perform(get("/email/verify-email")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("your email has been verified successfully."));
    }

    @Test
    void testVerifyEmail_InvalidToken() throws Exception {
        when(emailService.verifyEmail("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/email/verify-email")
                        .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid or expired token. Please try again."));
    }

    @Test
    void testVerifyEmail_NullToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/email/verify-email"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSendEmailWithAttachment_Success() throws Exception {
        when(emailService.sendEmailWithAttachment(any(EmailDetails.class))).thenReturn("Message sent successfully");

        mockMvc.perform(post("/email/sendEmailWithAttachment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "recipient": "test@example.com",
                                "subject": "Test Subject",
                                "messageBody": "Test Body"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("Message sent successfully"));
    }

    @Test
    void testSendEmailWithAttachment_Failure() throws Exception {
        doThrow(new RuntimeException("Attachment error"))
                .when(emailService).sendEmailWithAttachment(any(EmailDetails.class));

        mockMvc.perform(post("/email/sendEmailWithAttachment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "recipient": "test@example.com",
                                "subject": "Test Subject",
                                "messageBody": "Test Body"
                            }
                        """))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSendEmailWithAttachment_ResponseStatusException() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Attachment error"))
                .when(emailService).sendEmailWithAttachment(any(EmailDetails.class));

        mockMvc.perform(post("/email/sendEmailWithAttachment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "recipient": "test@example.com",
                                "subject": "Test Subject",
                                "messageBody": "Test Body"
                            }
                        """))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Attachment error"));
    }
}
