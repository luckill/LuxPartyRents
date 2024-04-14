package com.example.SeniorProject.Model;

import jakarta.persistence.*;
import org.springframework.security.crypto.bcrypt.*;

@Entity
@Table(name = "account")
public class Account
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private int id;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;

    @OneToOne(mappedBy = "account")
    private Customer customer;
    @Column(name = "admin")
    private boolean isAdmin;

    public Account()
    {

    }

    public Account(String email, String password, boolean isAdmin)
    {
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public String getEmail()
    {
        return email;
    }

    public Customer getCustomer()
    {
        return customer;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
