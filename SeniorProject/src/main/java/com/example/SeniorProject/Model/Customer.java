package com.example.SeniorProject.Model;

import jakarta.persistence.*;
import javax.validation.constraints.*;
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
    @Column(name = "address")
    private String address;
    @Column(name = "city")
    private String city;
    @Column(name = "state")
    private String state;
    @Size(min = 5,max = 5, message = "zip code must be at most 5 character")
    @Column(name = "zip_code")
    private String zipCode;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id")
    private Account account;

    public Customer()
    {

    }

    public Customer(String firstName, String lastName, String email, String phone, String address, String city, String state, String zipCode)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
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

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getZipCode()
    {
        return zipCode;
    }

    public void setZipCode(String zipCode)
    {
        this.zipCode = zipCode;
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
}
