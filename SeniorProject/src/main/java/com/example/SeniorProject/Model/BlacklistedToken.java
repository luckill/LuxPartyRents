package com.example.SeniorProject.Model;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "blacklisted_tokens")
public class BlacklistedToken
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String token;
    private LocalDateTime createAt;
    private LocalDateTime expiredAt;

    public BlacklistedToken()
    {

    }

    public BlacklistedToken(String token, LocalDateTime createAt, LocalDateTime expiredAt)
    {
        this.token = token;
        this.createAt = createAt;
        this.expiredAt = expiredAt;
    }

    public long getId()
    {
        return id;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public LocalDateTime getCreateAt()
    {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt)
    {
        this.createAt = createAt;
    }

    public LocalDateTime getExpiredAt()
    {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt)
    {
        this.expiredAt = expiredAt;
    }
}
