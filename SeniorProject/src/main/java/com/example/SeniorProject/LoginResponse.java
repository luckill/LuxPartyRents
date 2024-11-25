package com.example.SeniorProject;

import com.example.SeniorProject.Model.Account;

public class LoginResponse
{
	private Account authenticatedAccount;
	private String token;
    private String firstName;

	public LoginResponse(Account authenticatedAccount, String token, String firstName)
	{
		this.authenticatedAccount = authenticatedAccount;
		this.token = token;
        this.firstName = firstName;
	}

	public String getToken()
    {
        return token;
    }

    public LoginResponse setToken(String token)
    {
        this.token = token;
        return this;
    }

    public Account getAuthenticateAccount()
    {
        return authenticatedAccount;
    }

    public void setAuthenticateAccount(Account authenticateAccount)
    {
        this.authenticatedAccount = authenticateAccount;
    }

    public String getFirstName()
    {
       return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }
	@Override
    public String toString()
    {
        return "LoginResponse{" +
            "token='" + token + '\'' +
            ", firstName='" + firstName + '\'' +
            '}';
    }
}