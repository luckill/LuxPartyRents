package com.example.SeniorProject.Controller;

import com.example.SeniorProject.DTOs.LoginUserDTO;
import com.example.SeniorProject.DTOs.RegisterUserDTO;
import com.example.SeniorProject.Exception.BadRequestException;
import com.example.SeniorProject.LoginResponse;
import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.context.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RequestMapping("/auth")
@RestController
public class AuthenticationController
{
	private final JwtService jwtService;
	private final AuthenticationService authenticationService;

	public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService)
    {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDTO registerUserDTO)
    {
        authenticationService.signUp(registerUserDTO);
        return ResponseEntity.ok("Your account has been successfully created. A verification email has been sent to the address provided. Please follow the instructions in the email to verify your account. Unverified accounts and associated profiles will be automatically deleted at 12:00 AM Pacific Time.");
    }

    @PostMapping("/login")
public ResponseEntity<?> authenticate(@RequestBody LoginUserDTO loginUserDTO) {
    try {
        Account authenticatedUser = authenticationService.authenticate(loginUserDTO);
        String jwtToken = jwtService.generateToken(authenticatedUser);

        // Get customer details from the authenticated user
        Customer customer = authenticatedUser.getCustomer();
        String firstName = customer != null ? customer.getFirstName() : "";

        // Create a login response containing the user's first name
        LoginResponse loginResponse = new LoginResponse(authenticatedUser, jwtToken, jwtService.getExpirationTime(), firstName);
        return ResponseEntity.ok(loginResponse);
    } catch (BadRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials provided.");
    } catch (LockedException exception) {
        return ResponseEntity.status(HttpStatus.LOCKED).body("Your account is locked.");
    }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response)
    {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_OK);
        return ResponseEntity.ok().build();
    }
}