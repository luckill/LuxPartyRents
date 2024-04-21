package com.example.SeniorProject.Model;

public class AccountInfo
{
    private String email;
    private String password;

    public AccountInfo(String email, String password)
    {
        this.email = email;
        this.password = password;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPassword()
    {
        return password;
    }
}
