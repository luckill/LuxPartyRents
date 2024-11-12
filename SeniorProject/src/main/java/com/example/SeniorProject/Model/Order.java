package com.example.SeniorProject.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import javax.validation.constraints.NotNull;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "order_info")
public class Order
{

    @Id
    @Column(name = "order_id")
    private int id;

    @NotNull
    @Column(name = "order_date")
    private LocalDate creationDate;

    @NotNull
    @Column(name = "rental_time")
    private int rentalTime;

    @NotNull
    @Column(name = "pick_up_date")
    private LocalDate pick_up_date;


    @NotNull
    @Column(name = "payment_status")
    private boolean paid;

    @NotNull
    @Column(nullable = false, name = "order_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @NotNull
    private String paymentReference;

    @NotNull
    @Column(name = "price")
    private double price;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    @JsonBackReference
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<OrderProduct> orderProducts = new HashSet<>();

    public Order(int id, int rentalTime, boolean paid, LocalDate PickUpDate )
    {
        this.id = id;
        this.creationDate = LocalDate.now();
        this.rentalTime = rentalTime;
        this.pick_up_date = PickUpDate; ;
        this.paid = paid;
        this.status = OrderStatus.RECEIVED;
    }

    public Order()
    {
        // Default constructor
    }

    // Add product to the order with quantity
    public void addProduct(Product product, int quantity)
    {
        OrderProduct orderProduct = new OrderProduct(this, product, quantity);
        orderProducts.add(orderProduct);
        product.getOrderProducts().add(orderProduct); // Associate product with the order
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

    public int getRentalTime()
    {
        return rentalTime;
    }

    public void setRentalTime(int rentalTime)
    {
        this.rentalTime = rentalTime;
    }

    public boolean isPaid()
    {
        return paid;
    }

    public void setPaid(boolean paid)
    {
        this.paid = paid;
    }

    public OrderStatus getStatus()
    {
        return status;
    }

    public void setStatus(OrderStatus status)
    {
        this.status = status;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public Customer getCustomer()
    {
        return customer;
    }

    public void setCustomer(Customer customer)
    {
        this.customer = customer;
    }

    public Set<OrderProduct> getOrderProducts()
    {
        return orderProducts;
    }

    public void setOrderProducts(Set<OrderProduct> orderProducts)
    {
        this.orderProducts = orderProducts;
    }

    public @NotNull LocalDate getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(@NotNull LocalDate creationDate)
    {
        this.creationDate = creationDate;
    }

    public @NotNull String getPaymentReference()
    {
        return paymentReference;
    }

    public void setPaymentReference(@NotNull String paymentReference)
    {
        this.paymentReference = paymentReference;
    }

    public void setPick_up_date (LocalDate UpdateDate){
        this.pick_up_date = UpdateDate;
    }
    public LocalDate getPick_up_date(){
        return this.pick_up_date;
    }

}