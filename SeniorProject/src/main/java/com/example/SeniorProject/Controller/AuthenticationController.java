package com.example.SeniorProject.Controller;

import com.example.SeniorProject.DTOs.LoginUserDTO;
import com.example.SeniorProject.DTOs.RegisterUserDTO;
import com.example.SeniorProject.Exception.BadRequestException;
import com.example.SeniorProject.LoginResponse;
import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RequestMapping("/auth")
@RestController
public class AuthenticationController
{
	private final JwtService jwtService;
	private final AuthenticationService authenticationService;
    private final JwtTokenBlacklistService jwtTokenBlacklistService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

	public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, JwtTokenBlacklistService jwtTokenBlacklistService, BlacklistedTokenRepository blacklistedTokenRepository)
    {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.jwtTokenBlacklistService = jwtTokenBlacklistService;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDTO registerUserDTO)
    {
        authenticationService.signUp(registerUserDTO);
        return ResponseEntity.ok("Your account has been successfully created. A verification email has been sent to the address provided. Please follow the instructions in the email to verify your account. Unverified accounts and associated profiles will be automatically deleted at 12:00 AM Pacific Time.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDTO loginUserDTO)
    {
        try
        {
            Account authenticatedUser = authenticationService.authenticate(loginUserDTO);
            String jwtToken = jwtService.generateToken(authenticatedUser);
            LoginResponse loginResponse = new LoginResponse(authenticatedUser,jwtToken, jwtService.getExpirationTime());
            return ResponseEntity.ok(loginResponse);
        }
        catch (BadRequestException exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials provided.");
        }
        catch (LockedException exception)
        {
            return ResponseEntity.status(HttpStatus.LOCKED).body("your account is id locked");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token)
    {
        String jwt = token.substring(7);
        jwtTokenBlacklistService.blacklistToken(jwt);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deleteExpiredTokens")
    public ResponseEntity<?> deleteExpiredTokens()
    {
        List<BlacklistedToken> tokens = blacklistedTokenRepository.findExpiredBlacklistTokens(LocalDateTime.now());
        if (tokens.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.OK).body("No expired tokens found");
        }
        blacklistedTokenRepository.deleteAll(tokens);
        return ResponseEntity.status(HttpStatus.OK).body("Expired tokens were deleted");
    }
}