package com.example.SeniorProject.Model;

import jakarta.persistence.*;
import org.springframework.security.crypto.bcrypt.*;

import javax.validation.constraints.*;

@Entity
@Table(name = "account")
public class Account
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private int id;
    @NotNull
    @Column(name = "email")
    private String email;
    @NotNull
    @Size(min = 8, message = "password must be at least 8 character long")
    @Column(name = "password")
    private String password;
    @NotNull
    @Column(name = "is_admin")
    private boolean isAdmin;
    @NotNull
    @Column(name = "is_verified")
    private boolean isVerified;
    @OneToOne(mappedBy = "account")
    private Customer customer;

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
