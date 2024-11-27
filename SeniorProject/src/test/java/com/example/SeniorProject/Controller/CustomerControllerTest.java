package com.example.SeniorProject.Controller;

import com.example.SeniorProject.DTOs.*;
import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.*;
import org.springframework.security.core.context.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;
import org.springframework.web.server.*;
import org.springframework.context.annotation.Import;
import com.example.SeniorProject.Configuration.TestSecurityConfig;

import javax.annotation.meta.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(CustomerController.class)
public class CustomerControllerTest
{

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AccountRepository accountRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup()
    {
        MockitoAnnotations.openMocks(this);
        objectMapper.findAndRegisterModules();
    }

    // Test case 1: checkIfAccountExist
    @Test
    void checkIfAccountExist_AccountExists_ReturnsAccount() throws Exception {
        // Arrange: Prepare mock role
        Role role = new Role();
        role.setName(RoleEnum.ADMIN);  // Assuming 'RoleEnum' has an 'ADMIN' value

        // Arrange: Prepare the account
        String email = "existing@example.com";
        Account account = new Account(email, "testPassword");
        account.setRole(role);  // Set the role on the account

        // Mocking the roleRepository to return the role when checking for 'ADMIN' role
        when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(role));

        // Mocking the customerService to return the account when checking if it exists
        when(customerService.checkIfAccountExist(email)).thenReturn(account);

        // Act & Assert: Perform the request and check the response
        mockMvc.perform(post("/customer/findAccount")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email)) ;
        assertEquals(RoleEnum.ADMIN, role.getName());
    }

    @Test
    void checkIfAccountExist_AccountNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        String email = "nonexistent@example.com";
        when(customerService.checkIfAccountExist(email)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        // Act & Assert
        mockMvc.perform(post("/customer/findAccount")
                        .param("email", email))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Account not found"));
    }

    @Test
    void checkIfAccountExist_ServiceError_ReturnsInternalServerError() throws Exception {
        // Arrange
        String email = "error@example.com";
        when(customerService.checkIfAccountExist(email)).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal service error"));

        // Act & Assert
        mockMvc.perform(post("/customer/findAccount")
                        .param("email", email))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal service error"));
    }

    // Test case 2: getUserById
    @Test
    void getUserById_ShouldReturnCustomer_WhenIdExists() throws Exception
    {
        int customerId = 1;
        //    public Customer(String firstName, String lastName, String email, String phone)
        Customer customer = new Customer("John", "Doe", "john@example.com", "1234556666");
        Mockito.when(customerService.getUserById(customerId)).thenReturn(customer);

        mockMvc.perform(MockMvcRequestBuilders.get("/customer/getCustomer/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(customer.getEmail()))
                .andExpect(jsonPath("$.firstName").value(customer.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(customer.getLastName()))
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.phone").value(customer.getPhone()));
    }

    @Test
    void getUserById_ShouldReturnError_WhenCustomerNotFound() throws Exception
    {
        int customerId = 999;
        Mockito.when(customerService.getUserById(customerId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/customer/getCustomer/{id}", customerId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Customer not found"));
    }

    // Test case 3: updateCustomer
    @Test
    void updateCustomer_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO("John", "Doe", "john.doe@example.com", "1234445566");
        doNothing().when(customerService).updateCustomer(any(CustomerDTO.class));

        // Act
        mockMvc.perform(post("/customer/updateCustomer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(customerDTO)))
            .andExpect(status().isOk())
            .andExpect(content().string("Customer has been successfully updated"));
    }

    @Test
    void updateCustomer_CustomerNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO("NonExistent", "User", "nonexistent@example.com", "1222222222");
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"))
            .when(customerService).updateCustomer(any(CustomerDTO.class));

        // Act
        mockMvc.perform(post("/customer/updateCustomer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(customerDTO)))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Customer not found"));
    }

    @Test
    void updateCustomer_ServiceError_ReturnsInternalServerError() throws Exception {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO("John", "Doe", "john.doe@example.com", "1234445566");
        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error"))
            .when(customerService).updateCustomer(any(CustomerDTO.class));

        // Act
        mockMvc.perform(post("/customer/updateCustomer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(customerDTO)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Database error"));
    }

    // Test case 4: deleteCustomer
    @Test
    void deleteCustomer_ShouldReturnSuccess_WhenValidCustomer() throws Exception
    {
        CustomerDTO customerDTO = new CustomerDTO("John", "Doe", "john@example.com", "1234445566");
        Customer customer = new Customer("John", "Doe", "john@example.com", "1234445566");
        Account account = new Account("john@example.com", "hashed_password");

        Mockito.when(customerRepository.findCustomersByEmail(customerDTO.getEmail())).thenReturn(customer);
        Mockito.when(accountRepository.findAccountByEmail(customerDTO.getEmail())).thenReturn(account);
        Mockito.doNothing().when(customerRepository).deleteById(customer.getId());
        Mockito.doNothing().when(accountRepository).deleteById(account.getId());

        mockMvc.perform(delete("/customer/deleteCustomer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Customer has been successfully deleted"));
    }

    @Test
    void deleteCustomer_ShouldReturnError_WhenCustomerOrAccountNotFound() throws Exception
    {
        CustomerDTO customerDTO = new CustomerDTO("Nonexistent", "User", "nonexistent@example.com", "1234445566");
        Mockito.when(customerRepository.findCustomersByEmail(customerDTO.getEmail())).thenReturn(null);
        Mockito.when(accountRepository.findAccountByEmail(customerDTO.getEmail())).thenReturn(null);

        mockMvc.perform(delete("/customer/deleteCustomer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Customer has been successfully deleted"));
    }

    @Test
    void deleteCustomer_ExceptionThrown_ReturnsError() throws Exception
    {
        CustomerDTO customerDTO = new CustomerDTO("John", "Doe", "john.doe@example.com", "1223456767");

        // Mock repositories to return valid objects
        Customer customer = new Customer("John", "Doe", "john.doe@example.com", "1234445555");
        Account account = new Account("testuser", "password");

        when(customerRepository.findCustomersByEmail(customerDTO.getEmail())).thenReturn(customer);
        when(accountRepository.findAccountByEmail(customerDTO.getEmail())).thenReturn(account);

        // Simulate an exception during deletion
        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error"))
                .when(customerRepository).deleteById(anyInt());

        // Act
        mockMvc.perform(delete("/customer/deleteCustomer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john.doe@example.com\"}"))
                .andExpect(status().isInternalServerError())  // Assert HTTP status is INTERNAL_SERVER_ERROR
                .andExpect(content().string("Database error"));  // Assert error message
    }

    // Test case 5: getCustomerInfo
    @Test
    void getCustomerInfo_ShouldReturnCustomerInfo_WhenTokenIsValid() throws Exception
    {
        String token = "Bearer valid.token.here";
        CustomerDTO customer = new CustomerDTO("John", "Doe", "john@example.com", "1234546666");
        Mockito.when(customerService.getCustomerInfo(token)).thenReturn(customer);

        mockMvc.perform(MockMvcRequestBuilders.get("/customer/getCustomerInfo")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.email").value(customer.getEmail()));
    }

    @Test
    void getCustomerInfo_ShouldReturnError_WhenTokenIsInvalid() throws Exception
    {
        String token = "Bearer invalid.token.here";
        Mockito.when(customerService.getCustomerInfo(token))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        mockMvc.perform(MockMvcRequestBuilders.get("/customer/getCustomerInfo")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));
    }



    private String asJsonString(Object obj) throws JsonProcessingException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }
}
