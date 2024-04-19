package com.example.SeniorProject.Model;

import jakarta.persistence.*;

import javax.validation.constraints.*;
import java.util.*;

@Entity
@Table(name = "product_info")
public class Product
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int id;
    @NotNull
    @Column(name = "quantity")
    private int quantity;
    @NotNull
    @Column(name = "price")
    private double price;
    @NotNull
    @Column(name = "type")
    private String type;
    @NotNull
    @Column(name = "name")
    private String name;
    @NotNull
    @Column(name = "description")
    private String description;
    @NotNull
    @Column(name = "location")
    private String location;

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
}
