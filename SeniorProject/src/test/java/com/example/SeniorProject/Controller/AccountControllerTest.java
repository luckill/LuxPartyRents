package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Configuration.TestSecurityConfig;
import com.example.SeniorProject.Model.Account;
import com.example.SeniorProject.Model.Customer;
import com.example.SeniorProject.Model.Role;
import com.example.SeniorProject.Model.RoleEnum;
import com.example.SeniorProject.Service.AccountService;
import com.example.SeniorProject.Service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;


import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(AccountController.class)
public class AccountControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private AccountService accountService;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private HttpServletRequest httpServletRequest;


        @Test
        public void testGetAccountById_Success() throws Exception {
                int accountId = 1;
                Role role = new Role();  // Ensure role is not null
                role.setName(RoleEnum.ADMIN);
                Account account = new Account("John Doe", "johndoe@example.com");
                account.setId(accountId);
                account.setRole(role);  // Set a valid role

                when(accountService.getAccountById(accountId)).thenReturn(account);

                mockMvc.perform(get("/account/getAccount/{id}", accountId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(accountId))
                        .andExpect(jsonPath("$.username").value("John Doe"));
        }

        @Test
        void testGetAccountById_NotFound() throws Exception {
                when(accountService.getAccountById(1)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

                mockMvc.perform(get("/account/getAccount/1"))
                        .andExpect(status().isNotFound())
                        .andExpect(content().string("Account not found"));
        }

        @Test
        void testDeleteAccount_Success() throws Exception {
                doNothing().when(accountService).deleteAccount(1);

                mockMvc.perform(delete("/account/deleteAccount/1"))
                        .andExpect(status().isOk())
                        .andExpect(content().string("your account has been successfully deleted."));
        }

        @Test
        void testDeleteAccount_NotFound() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")).when(accountService).deleteAccount(1);

                mockMvc.perform(delete("/account/deleteAccount/1"))
                        .andExpect(status().isNotFound())
                        .andExpect(content().string("Account not found"));
        }

        @Test
        void testTurnAdmin_Success() throws Exception {
                String apiKey = "validApiKey";
                doNothing().when(accountService).turnAdmin(1, apiKey);

                mockMvc.perform(post("/account/turnAdmin/1")
                                .param("apiKey", apiKey))
                        .andExpect(status().isOk())
                        .andExpect(content().string("you have successfully converted this account to admin."));
        }

        @Test
        void testTurnAdmin_Error() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid API Key")).when(accountService).turnAdmin(1, "invalidApiKey");

                mockMvc.perform(post("/account/turnAdmin/1")
                                .param("apiKey", "invalidApiKey"))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().string("Invalid API Key"));
        }

        @Test
        void testGetUserInfo_Success() throws Exception {
                int accountId = 1;
                Role role = new Role();  // Ensure role is not null
                role.setName(RoleEnum.ADMIN);
                Account account = new Account("John Doe", "johndoe@example.com");
                account.setId(accountId);
                account.setRole(role);
                when(accountService.getUserInfo(anyString())).thenReturn(account);

                mockMvc.perform(get("/account/getAccountInfo")
                                .header("Authorization", "Bearer token"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        void testGetUserInfo_Error() throws Exception {
                when(accountService.getUserInfo(anyString())).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

                mockMvc.perform(get("/account/getAccountInfo")
                                .header("Authorization", "Bearer invalid-token"))
                        .andExpect(status().isUnauthorized())
                        .andExpect(content().string("Unauthorized"));
        }

        /*
        @Test
        void testDeleteUnverifiedAccounts_Success() throws Exception {

                doNothing().when(accountService).deleteAllUnverifiedAccounts();

                mockMvc.perform(delete("/account/deleteUnverifiedAccounts"))
                        .andExpect(status().isOk())
                        .andExpect(content().string("All unverified accounts have been successfully deleted."));
        }

        @Test
        void testDeleteUnverifiedAccounts_ResponseStatusException() throws Exception {
                // Simulate a ResponseStatusException being thrown with a custom message and status code (e.g., NOT_FOUND)
                doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No unverified accounts found"))
                        .when(accountService).deleteAllUnverifiedAccounts();

                // Perform the DELETE request to /account/deleteUnverifiedAccounts
                mockMvc.perform(delete("/account/deleteUnverifiedAccounts"))
                        .andExpect(status().isNotFound())  // Check that the status returned is 404 (NOT_FOUND)
                        .andExpect(content().string("No unverified accounts found"));  // Verify that the message from the exception is returned
        }

        @Test
        void testDeleteUnverifiedAccounts_Failure() throws Exception {
                doThrow(new RuntimeException("An error occurred while deleting unverified accounts")).when(accountService).deleteAllUnverifiedAccounts();

                mockMvc.perform(delete("/account/deleteUnverifiedAccounts"))
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().string("An error occurred while deleting unverified accounts."));
        }

         */

        @Test
        void testGetCustomerId_Success() throws Exception {
                Account account = new Account();
                account.setId(1);
                Customer customer = new Customer();
                customer.setId(123);
                account.setCustomer(customer);
                when(accountService.getUserInfo(anyString())).thenReturn(account);

                mockMvc.perform(get("/account/customerId")
                                .header("Authorization", "Bearer token"))
                        .andExpect(status().isOk())
                        .andExpect(content().string("123"));
        }

        @Test
        void testGetCustomerId_AccountNotFound() throws Exception {
                // Simulate the accountService throwing a ResponseStatusException
                when(accountService.getUserInfo(anyString())).thenReturn(null);  // Simulating a scenario where no account is found

                mockMvc.perform(get("/account/customerId")
                                .header("Authorization", "Bearer token"))
                        .andExpect(status().isNotFound())  // Expecting 404
                        .andExpect(content().string("Account not found"));  // The error message you expect
        }

        @Test
        void testGetCustomerId_CustomerNotFound() throws Exception {
                // Given: Mock the accountService to return an Account with a null Customer
                Account mockAccount = new Account();
                mockAccount.setCustomer(null);  // Set customer to null

                when(accountService.getUserInfo(anyString())).thenReturn(mockAccount);

                // When: Perform the GET request to /account/customerId with a valid token
                mockMvc.perform(get("/account/customerId")
                                .header("Authorization", "Bearer some-token"))
                        .andExpect(status().isNotFound())  // Expecting 404 status (NOT_FOUND)
                        .andExpect(content().string("Customer ID not found"));  // Expect the response body to contain the message
        }

        @Test
        void testGetCustomerId_GeneralException() throws Exception {
                // Given: Mock the accountService to throw a generic Exception
                when(accountService.getUserInfo(anyString()))
                        .thenThrow(new RuntimeException("Some unexpected error"));

                // When: Perform the GET request to /account/customerId with a valid token
                mockMvc.perform(get("/account/customerId")
                                .header("Authorization", "Bearer some-token"))
                        .andExpect(status().isInternalServerError())  // Expect 500 status (INTERNAL_SERVER_ERROR)
                        .andExpect(content().string("An error occurred"));  // Expect the generic error message
        }
}