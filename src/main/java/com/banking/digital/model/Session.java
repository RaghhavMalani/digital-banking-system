package com.banking.digital.model;

import java.time.LocalDateTime;

public class Session {
    private int sessionID;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;

    public void createSession() {
        this.loginTime = LocalDateTime.now();
        this.expireTime = this.loginTime.plusMinutes(30);
        // Generate and assign sessionID
        this.sessionID = (int) (Math.random() * 100000);
    }

    public void invalidateSession() {
        this.expireTime = LocalDateTime.now();
    }

    public boolean isSessionActive() {
        return expireTime != null && LocalDateTime.now().isBefore(this.expireTime);
    }

    // --- Getters and Setters ---

    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
}
