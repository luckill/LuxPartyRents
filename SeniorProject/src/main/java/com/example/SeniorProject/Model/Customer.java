package com.example.SeniorProject.Model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.*;

@Entity
@Table(name = "customer")
public class Customer
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private int id;
    @Column(name = "customer_firstName")
    @NotNull
    private String firstName;
    @Column(name = "customer_lastName")
    @NotNull
    private String lastName;
    @NotNull
    @Column(name = "customer_email")
    private String email;
    @NotNull
    @Size(min = 10, max = 10)
    @Column(name = "customer_phone")
    private String phone;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Order> orders = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id")
    @JsonManagedReference
    private Account account;

    public Customer()
    {

    }

    public Customer(String firstName, String lastName, String email, String phone)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public int getId()
    {
        return id;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public List<Order> getOrders()
    {
        return orders;
    }

    public void setOrders(List<Order> order)
    {
        this.orders = order;
    }

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        this.account = account;
    }

    public void setId(int id)
    {
        this.id = id;
    }
}
