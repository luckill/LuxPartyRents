package com.example.SeniorProject.DTOs;

import com.example.SeniorProject.Model.*;
import jakarta.persistence.Column;

import java.time.*;
import java.util.HashSet;
import java.util.Set;

public class OrderDTO
{
    private int id;
    private LocalDate creationDate;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private boolean paid;
    private String status;
    private double price;
    private Set<OrderProductDTO> products;
    private String address;
    private double deposit;
    private double tax;
    private double deliveryFee;
    private double subtotal;
    public OrderDTO()
    {

    }

    public OrderDTO(LocalDate date, LocalDate pickupDate, LocalDate returnDate, boolean paid, OrderStatus status, String address, double deposit, double tax, double deliveryFee, double price, double subtotal)
    {
        this.creationDate = date;
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        this.paid = paid;
        this.status = status.toString();
        this.address = address;
        this.products = new HashSet<>();
        this.deposit = deposit;
        this.tax = tax;
        this.deliveryFee = deliveryFee;
        this.price = price;
        this.subtotal = subtotal;
    }

    public int getId()
    {
        return id;
    }

	public void setId(int id)
    {
		this.id = id;
	}

	public LocalDate getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate)
    {
        this.creationDate = creationDate;
    }

    public boolean isPaid()
    {
        return paid;
    }

    public void setPaid(boolean paid)
    {
        this.paid = paid;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Set<OrderProductDTO> getOrderProducts()
    {
        return products;
    }
    public void setOrderProducts(Set<OrderProductDTO> products)
    {
        this.products = products;
    }
    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
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

    public double getTax()
    {
        return tax;
    }

    public void setTax(double tax)
    {
        this.tax = tax;
    }

    public double getDeliveryFee()
    {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee)
    {
        this.deliveryFee = deliveryFee;
    }

    public double getSubtotal()
    {
        return subtotal;
    }

    public void setSubtotal(double subtotal)
    {
        this.subtotal = subtotal;
    }

    public LocalDate getPickupDate()
    {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate)
    {
        this.pickupDate = pickupDate;
    }

    public LocalDate getReturnDate()
    {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate)
    {
        this.returnDate = returnDate;
    }
}