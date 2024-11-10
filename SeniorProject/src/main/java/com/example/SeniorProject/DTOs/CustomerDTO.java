package com.example.SeniorProject.DTOs;

import com.fasterxml.jackson.annotation.*;

public class CustomerDTO
{
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public CustomerDTO()
    {

    }

    @JsonCreator
    public CustomerDTO(@JsonProperty("firstName")String firstName, @JsonProperty("lastName")String lastName, @JsonProperty("email")String email, @JsonProperty("phone")String phone)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public CustomerDTO(int id, String firstName, String lastName, String email, String phone)
    {
        this(firstName, lastName, email, phone);
        this.id = id;
    }



    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
