package com.example.SeniorProject;

import com.example.SeniorProject.Model.*;

public class CustomerAccountWrapper
{
    private String firstName, lastName, email, phone, password;
    private boolean isAdmin;
    private Customer customer;
    private Account account;

    public CustomerAccountWrapper(String firstName, String lastName, String email, String phone, boolean isAdmin, String password)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.account = new Account(email,"");
        this.customer = new Customer(firstName, lastName, email, phone);
    }

    public Customer getCustomer()
    {
        return customer;
    }
    public Account getAccount()
    {
        return account;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPhone()
    {
        return phone;
    }

    public String getPassword()
    {
        return password;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }
}
