package com.example.SeniorProject.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "order_info")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
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

    public Order(String date, int rentalTime, boolean paid, String status, double price, Customer customer) {
        this.date = date;
        this.rentalTime = rentalTime;
        this.paid = paid;
        this.status = status;
        this.price = price;
        this.customer = customer;
    }

    public Order() {
        // Default constructor
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