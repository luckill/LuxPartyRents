package com.example.SeniorProject.Service;

import com.example.SeniorProject.DTOs.LoginUserDTO;
import com.example.SeniorProject.DTOs.RegisterUserDTO;
import com.example.SeniorProject.Exception.BadRequestException;
import com.example.SeniorProject.Model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest
{
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterUserDTO registerUserDTO;
    private LoginUserDTO loginUserDTO;
    private Account account;
    private Customer customer;
    private Role role;

    @BeforeEach
    public void setUp()
    {
        // Initialize test objects
        registerUserDTO = new RegisterUserDTO("John", "Doe", "john.doe@example.com", "password123", "1234567890");
        loginUserDTO = new LoginUserDTO("john.doe@example.com", "password123");

        account = new Account("john.doe@example.com", "encodedPassword", false);
        account.setFailedLoginAttempt(0);

        customer = new Customer("John", "Doe", "john.doe@example.com", "1234567890");
        customer.setAccount(account);

        role = new Role();
        role.setName(RoleEnum.USER);
    }

    // Test for successful registration
    @Test
    public void testSignUp_SuccessfulRegistration()
    {
        // Mock dependencies
        when(accountRepository.findAccountByEmail(registerUserDTO.getEmail())).thenReturn(null);
        when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Call the method
        authenticationService.signUp(registerUserDTO);

        // Verify interactions
        verify(accountRepository).save(any(Account.class));
        verify(customerRepository).save(any(Customer.class));
    }

    // Test for registration when the email already exists
    @Test
    void testSignUp_EmailAlreadyExists()
    {
        // Mock that the email already exists
        when(accountRepository.findAccountByEmail(registerUserDTO.getEmail())).thenReturn(account);

        // Assert exception
        assertThrows(BadRequestException.class, () -> authenticationService.signUp(registerUserDTO));


        // Verify no interactions with repositories
        verify(customerRepository, never()).save(any(Customer.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    // Test for role not found during registration
    @Test
    void testSignUp_RoleNotFound()
    {
        // Mock role repository returning empty
        when(accountRepository.findAccountByEmail(registerUserDTO.getEmail())).thenReturn(null);
        when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Call the method
        authenticationService.signUp(registerUserDTO);

        // Verify account is saved without role
        verify(accountRepository).save(any(Account.class));
        verify(customerRepository).save(any(Customer.class));
    }

    // Test for successful login
    @Test
    void testAuthenticate_SuccessfulLogin()
    {
        // Mock dependencies
        when(accountRepository.findAccountByEmail(loginUserDTO.getEmail())).thenReturn(account);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);

        // Call the method
        Account result = authenticationService.authenticate(loginUserDTO);

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(accountRepository).save(account);

        // Assert that failed attempts are reset
        assertEquals(0, account.getFailedLoginAttempt());
    }

    // Test for invalid credentials
    @Test
    void testAuthenticate_InvalidCredentials()
    {
        // Mock dependencies
        when(accountRepository.findAccountByEmail(loginUserDTO.getEmail())).thenReturn(account);
        doThrow(new BadCredentialsException("Invalid credentials")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Call the method and assert exception
        assertThrows(BadRequestException.class, () -> authenticationService.authenticate(loginUserDTO));

        // Verify failed attempts increment
        assertEquals(1, account.getFailedLoginAttempt());
        verify(accountRepository).save(account);
    }

    // Test for locking account after multiple failed login attempts
    @Test
    void testAuthenticate_AccountLockedAfterFailedAttempts()
    {
        // Set failed attempts to 2
        account.setFailedLoginAttempt(2);

        // Mock authentication failure
        when(accountRepository.findAccountByEmail(loginUserDTO.getEmail())).thenReturn(account);
        doThrow(new BadCredentialsException("Invalid credentials")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Call the method and assert exception
        assertThrows(LockedException.class, () -> authenticationService.authenticate(loginUserDTO));

        // Verify account is locked
        assertTrue(account.getIsLock());
        verify(accountRepository).save(account);
    }

    // Test for login attempt with a locked account
    @Test
    void testAuthenticate_LockedAccount()
    {
        // Set account to locked
        account.setIsLocked(true);
        account.setFailedLoginAttempt(3);

        // Mock account retrieval
        when(accountRepository.findAccountByEmail(loginUserDTO.getEmail())).thenReturn(account);

        // Call the method and assert exception
        assertThrows(LockedException.class, () -> authenticationService.authenticate(loginUserDTO));
        assertEquals(3,account.getFailedLoginAttempt());
        assertTrue(account.getIsLock());
    }

    // Test for non-existent email login attempt
    @Test
    void testAuthenticate_NonExistentEmail()
    {
        // Mock non-existent account
        when(accountRepository.findAccountByEmail(loginUserDTO.getEmail())).thenReturn(null);

        // Call the method and assert exception
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authenticationService.authenticate(loginUserDTO));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("no account associated with this email", exception.getReason());
    }
}
