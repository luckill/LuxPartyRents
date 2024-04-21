package com.example.SeniorProject.Model;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "product_info")
public class Product
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int id;
    @Column(name = "quantity")
    private int quantity;
    @Column(name = "price")
    private double price;
    @Column(name = "type")
    private String type;
    @Column(name = "name")
    private String name;

    @Column(name = "location")
    private String location;
    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "products")
    private Set<Order> orders = new HashSet<>();

    public Product()
    {

    }

    public Product(int quantity, double price, String type)
    {
        this.quantity = quantity;
        this.price = price;
        this.type = type;
    }

    public int getId()
    {
        return id;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public double getPrice()
    {
        return price;
    }

    public String getType()
    {
        return type;
    }

    public Set<Order> getOrders()
    {
        return orders;
    }

    public void setOrders(Set<Order> orders)
    {
        this.orders = orders;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
