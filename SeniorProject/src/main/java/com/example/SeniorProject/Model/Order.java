package com.example.SeniorProject.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Entity
@Table(name = "order_info")
public class Order {

    @Id
    @Column(name = "order_id", unique = true, nullable = false)
    private int id;

    @NotNull
    @Column(name = "order_date")
    private String date;

    @NotNull
    @Column(name = "rental_time")
    private int rentalTime;

    @NotNull
    @Column(name = "payment_status")
    private boolean paid;

    @NotNull
    @Column(name = "order_status")
    private String status;

    @NotNull
    @Column(name = "price")
    private double price;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    @JsonBackReference
    private Customer customer;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinTable(name = "order_product", joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Product> products = new HashSet<>();

    // Constructor that assigns a random unique int as the order ID
    public Order(String date, int rentalTime, boolean paid, String status, double price, Customer customer) {
        this.id = generateRandomUniqueOrderId();  // Generate random int for order_id
        this.date = date;
        this.rentalTime = rentalTime;
        this.paid = paid;
        this.status = status;
        this.price = price;
        this.customer = customer;
    }

    public Order() {
        this.id = generateRandomUniqueOrderId();  // Generate random int for order_id in default constructor
    }

    // Generate a random unique int for the order ID
    private int generateRandomUniqueOrderId() {
        Random random = new Random();
        return random.nextInt(Integer.MAX_VALUE);  // Generates a random positive integer
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getRentalTime() {
        return rentalTime;
    }

    public void setRentalTime(int rentalTime) {
        this.rentalTime = rentalTime;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }
}