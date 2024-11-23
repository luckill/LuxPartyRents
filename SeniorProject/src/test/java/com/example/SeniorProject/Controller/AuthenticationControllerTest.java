package com.example.SeniorProject.Controller;

import com.example.SeniorProject.DTOs.*;
import com.example.SeniorProject.Exception.BadRequestException;
import com.example.SeniorProject.LoginResponse;
import com.example.SeniorProject.Model.Account;
import com.example.SeniorProject.Model.Customer;
import com.example.SeniorProject.Service.AuthenticationService;
import com.example.SeniorProject.Service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.core.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.*;
import org.springframework.web.server.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest
{

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;


    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();

    }

    @Test
    void testRegister_validSignup() throws Exception
    {
        RegisterUserDTO registerUserDTO = new RegisterUserDTO("test@example.com", "password", "testUser", "testUser", "1234445555");

        // Mocking the behavior of authenticationService.signUp
        doNothing().when(authenticationService).signUp(any(RegisterUserDTO.class));

        mockMvc.perform(post("/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerUserDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Your account has been successfully created. A verification email has been sent to the address provided. Please follow the instructions in the email to verify your account. Unverified accounts and associated profiles will be automatically deleted the next day."));

        verify(authenticationService, times(1)).signUp(any(RegisterUserDTO.class));
    }

    @Test
    void testLogin_invalidCredentials() throws Exception
    {
        LoginUserDTO loginUserDTO = new LoginUserDTO("wrongUser", "wrongPassword");

        // Mock the behavior of authenticationService.authenticate to throw BadRequestException
        when(authenticationService.authenticate(any(LoginUserDTO.class)))
                .thenThrow(new BadRequestException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid credentials provided."));
    }

    @Test
    void testLogin_lockedAccount() throws Exception
    {
        LoginUserDTO loginUserDTO = new LoginUserDTO("lockedUser", "password");

        // Mock the behavior of authenticationService.authenticate to throw LockedException
        when(authenticationService.authenticate(any(LoginUserDTO.class)))
                .thenThrow(new org.springframework.security.authentication.LockedException("Your account is locked."));

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isLocked())
                .andExpect(content().string("Your account is locked."));
    }

    @Test
    void authenticate_ValidCredentials_ReturnsLoginResponse() {
        // Arrange
        LoginUserDTO loginUserDTO = new LoginUserDTO("testuser", "password");
        Customer customer = new Customer("John", "Doe", "john.doe@example.com", "1234445555");
        Account account = new Account("testuser", "password");
        account.setCustomer(customer);  // Set the mocked customer in the account

        // Mock dependencies
        when(authenticationService.authenticate(any(LoginUserDTO.class))).thenReturn(account);
        when(jwtService.generateToken(any(Account.class))).thenReturn("mockJwtToken");

        // Act
        ResponseEntity<?> response = authenticationController.authenticate(loginUserDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(LoginResponse.class, response.getBody());
        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertEquals("John", loginResponse.getFirstName());  // Verify firstName is "John"
    }

    @Test
    void authenticate_InvalidCredentials_ReturnsBadRequest()
    {

        LoginUserDTO loginUserDTO = new LoginUserDTO("testuser", "password");
        Customer customer = new Customer("John", "Doe", "john.doe@example.com", "1234445555");
        Account account = new Account("testuser", "password");

        // Arrange
        when(authenticationService.authenticate(any(LoginUserDTO.class))).thenThrow(new BadRequestException("Invalid credentials"));

        // Act
        ResponseEntity<?> response = authenticationController.authenticate(loginUserDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid credentials provided.", response.getBody());
    }

    @Test
    void authenticate_LockedAccount_ReturnsLockedResponse()
    {

        LoginUserDTO loginUserDTO = new LoginUserDTO("testuser", "password");
        Customer customer = new Customer("John", "Doe", "john.doe@example.com", "1234445555");
        Account account = new Account("testuser", "password");

        // Arrange
        when(authenticationService.authenticate(any(LoginUserDTO.class))).thenThrow(new org.springframework.security.authentication.LockedException("Account is locked"));

        // Act
        ResponseEntity<?> response = authenticationController.authenticate(loginUserDTO);

        // Assert
        assertEquals(HttpStatus.LOCKED, response.getStatusCode());
        assertEquals("Your account is locked.", response.getBody());
    }

    @Test
    void authenticate_NullCustomer_ReturnsLoginResponseWithEmptyFirstName()
    {
        LoginUserDTO loginUserDTO = new LoginUserDTO("testuser", "password");
        Customer customer = new Customer("John", "Doe", "john.doe@example.com", "1234445555");
        Account account = new Account("testuser", "password");

        // Arrange
        account.setCustomer(null); // Simulating no associated customer
        when(authenticationService.authenticate(any(LoginUserDTO.class))).thenReturn(account);
        when(jwtService.generateToken(any(Account.class))).thenReturn("mockJwtToken");

        // Act
        ResponseEntity<?> response = authenticationController.authenticate(loginUserDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof LoginResponse);
        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertEquals("", loginResponse.getFirstName());  // Empty first name
    }

    @Test
    void authenticate_UnexpectedException_ReturnsInternalServerError()
    {
        LoginUserDTO loginUserDTO = new LoginUserDTO("testuser", "password");
        Customer customer = new Customer("John", "Doe", "john.doe@example.com", "1234445555");
        Account account = new Account("testuser", "password");

        // Arrange
        when(authenticationService.authenticate(any(LoginUserDTO.class))).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error"));

        // Act
        ResponseEntity<?> response = authenticationController.authenticate(loginUserDTO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(String.class, response.getBody());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void authenticate_MissingRequestBody_ReturnsBadRequest()
    {
        LoginUserDTO loginUserDTO = new LoginUserDTO("testuser", "password");
        Customer customer = new Customer("John", "Doe", "john.doe@example.com", "1234445555");
        Account account = new Account("testuser", "password");

        // Arrange
        // Sending null instead of a LoginUserDTO
        loginUserDTO = null;

        // Act
        ResponseEntity<?> response = authenticationController.authenticate(loginUserDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request body!!!! - request body can't be null", response.getBody());
    }

    @Test
    void logout_SuccessfulLogout_ReturnsOkResponse() {
        // Act
        ResponseEntity<?> responseEntity = authenticationController.logout(request, response);

        // Assert
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);  // Verify status is set to 200 OK
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());  // Ensure response status is 200 OK
    }

    @Test
    void logout_NullRequestAndResponse_ReturnsOkResponse() {
        // Arrange: Mock the response object to avoid NullPointerException
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        // Act
        ResponseEntity<?> responseEntity = authenticationController.logout(null, mockResponse);

        // Assert
        verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_OK);  // Verify status is set to 200 OK
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());  // Ensure response status is 200 OK
    }

    @Test
    void logout_EmptySession_ReturnsOkResponse() {
        // Arrange: simulate an empty session (no security context)
        SecurityContextHolder.clearContext();  // Ensure no context exists

        // Act
        ResponseEntity<?> responseEntity = authenticationController.logout(request, response);

        // Assert
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);  // Verify status is set to 200 OK
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());  // Ensure response status is 200 OK
    }
}
