package com.example.SeniorProject.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import javax.validation.constraints.NotNull;
import java.time.*;
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
    @Column(name = "pickup_date")
    private LocalDate pickupDate;


    @NotNull
    @Column(name = "return_date")
    private LocalDate returnDate;

    @NotNull
    @Column(name = "payment_status")
    private boolean paid;

    @NotNull
    @Column(nullable = false, name = "order_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.RECEIVED;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "address")
    private String address;

    @Column(nullable = false, name = "price")
    private double price;

    @Column(nullable = false, name = "deposit")
    private double deposit;

    @Column(nullable = false, name = "tax")
    private double tax;

    @Column(nullable = false, name = "deliveryFee")
    private double deliveryFee;

    @Column(nullable = false, name = "subtotal")
    private double subtotal;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    @JsonBackReference
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<OrderProduct> orderProducts = new HashSet<>();

    public Order(int id, LocalDate pickupDate, LocalDate returnDate, boolean paid, String address)
    {
        this.id = id;
        this.creationDate = LocalDate.now();
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        this.paid = paid;
        this.status = OrderStatus.RECEIVED;
        this.address = address;
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

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public double getDeposit()
    {
        return deposit;
    }

    public void setDeposit(double deposit)
    {
        this.deposit = deposit;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public @NotNull LocalDate getPickupDate()
    {
        return pickupDate;
    }

    public void setPickupDate(@NotNull LocalDate pickupDate)
    {
        this.pickupDate = pickupDate;
    }

    public @NotNull LocalDate getReturnDate()
    {
        return returnDate;
    }

    public void setReturnDate(@NotNull LocalDate returnDate)
    {
        this.returnDate = returnDate;
    }
}