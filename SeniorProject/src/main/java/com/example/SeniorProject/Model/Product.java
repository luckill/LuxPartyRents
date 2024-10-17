package com.example.SeniorProject.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product_info")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int id;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price")
    private double price;

    @Column(name = "deposit")
    private double deposit;

    @Column(name = "type")
    private String type;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "deliver_only")
    private boolean deliverOnly;

    @Column(name = "feature_Product")
    private boolean featureProduct;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference("order_product")
    private Set<OrderProduct> orderProducts = new HashSet<>();

    public Product()
    {
        // Default constructor
    }

    public Product(int quantity, double price, String type, String name, String description)
    {
        this.quantity = quantity;
        this.price = price;
        this.deposit = price / 2;
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public Product(int quantity, double price, String type, String name, String description, boolean deliverOnly, boolean featureProduct)
    {
        this(quantity, price, type, name, description);
        this.deliverOnly = deliverOnly;
        this.featureProduct = featureProduct;
    }

    // Getters and Setters

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public double getPrice()
    {
        return price;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Set<OrderProduct> getOrderProducts()
    {
        return orderProducts;
    }

    public void setOrderProducts(Set<OrderProduct> orderProducts)
    {
        this.orderProducts = orderProducts;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public double getDeposit()
    {
        return deposit;
    }

    public void setDeposit(double deposit)
    {
        this.deposit = deposit;
    }

    public boolean isDeliverOnly()
    {
        return deliverOnly;
    }

    public void setDeliverOnly(boolean deliverOnly)
    {
        this.deliverOnly = deliverOnly;
    }

    public boolean isFeatureProduct()
    {
        return featureProduct;
    }

    public void setFeatureProduct(boolean featureProduct)
    {
        this.featureProduct = featureProduct;
    }
}