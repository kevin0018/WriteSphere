package com.writesphere.writesphere.user.application;

public interface PasswordHash {
    String encode(String password);
}
