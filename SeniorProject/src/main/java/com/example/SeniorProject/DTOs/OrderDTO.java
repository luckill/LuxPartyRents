package com.example.SeniorProject.DTOs;

import com.example.SeniorProject.Model.*;

import java.time.*;
import java.util.HashSet;
import java.util.Set;

public class OrderDTO
{
    private int id;
    private LocalDate creationDate;
    private int rentalTime;
    private boolean paid;
    private String status;
    private double price;
    private Set<OrderProductDTO> products;
    public OrderDTO()
    {

    }
    public OrderDTO(LocalDate date, int rentalTime, boolean paid, OrderStatus status)
    {
        this.creationDate = date;
        this.rentalTime = rentalTime;
        this.paid = paid;
        this.status = status.toString();
        this.products = new HashSet<>();
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
}