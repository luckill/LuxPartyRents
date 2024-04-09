package com.example.SeniorProject.Model;

import jakarta.persistence.*;

import java.sql.Date;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "order_info")
public class Order
{
    @Id
    @Column(name = "order_id")
    private int id;
    @Column(name = "order_date")
    private String date;
    @Column(name = "rental_time")
    private int rentalTime;
    @Column(name = "payment_status")
    private boolean hasBeenPaid;
    @Column(name = "order_status")
    private String status;
    @Column(name = "price")
    private double price;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    private Customer customer;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinTable(name = "order_product", joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Product> products = new HashSet<>();

    public Order(String date, int rentalTime, boolean hasBeenPaid, String status)
    {
        this.id = generateOrderNumber();
        this.date = date;
        this.rentalTime = rentalTime;
        this.hasBeenPaid = hasBeenPaid;
        this.status = status;
    }

    public Order()
    {

    }

    public long getId()
    {
        return id;
    }

    public String getDate()
    {
        return date;
    }

    public int getRentalTime()
    {
        return rentalTime;
    }

    public boolean isHasBeenPaid()
    {
        return hasBeenPaid;
    }

    public String getStatus()
    {
        return status;
    }

    public Customer getCustomer()
    {
        return customer;
    }

    public Set<Product> getProducts()
    {
        return products;
    }

    public void setProducts(Set<Product> productList)
    {
        this.products = productList;
    }

    public void setCustomer(Customer customer)
    {
        this.customer = customer;
    }

    private int generateOrderNumber()
    {
        UUID uniqueID = UUID.randomUUID();
        int hashCode = uniqueID.hashCode();
        String hashString = Integer.toString(hashCode);
        hashString = hashString.replaceAll("-", "");
        hashString = hashString.replaceAll("[^\\d]", "");
        return Integer.parseInt(hashString.substring(0, 8));
    }
}
