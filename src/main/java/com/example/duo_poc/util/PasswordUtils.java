package com.example.duo_poc.util;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;


@Slf4j
public class PasswordUtils {
    public static String hashSHA256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error hashing password {}",e.getMessage());
            return null;
        }
    }
}