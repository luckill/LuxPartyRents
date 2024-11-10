package com.example.SeniorProject.Model;

public enum OrderStatus
{
    RECEIVED,
    CONFIRMED,
    READY_FOR_PICK_UP,
    PICK_UP,
    CANCELLED,
    RETURNED,
    COMPLETED;

    public static OrderStatus fromString(String status)
    {
        return OrderStatus.valueOf(status.toUpperCase());
    }
}