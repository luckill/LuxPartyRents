package com.example.SeniorProject.Model;

public class TokenData
{
    private final String email;
    private final long timestamp;
    private boolean isUsed;

    public TokenData(String email, long timestamp, boolean isUsed) {
        this.email = email;
        this.timestamp = timestamp;
        this.isUsed = isUsed;
    }

    public String getEmail() {
        return email;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }
}