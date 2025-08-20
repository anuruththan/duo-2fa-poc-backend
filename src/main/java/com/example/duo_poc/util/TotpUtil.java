package com.example.duo_poc.util;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

public class TotpUtil {
    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public static GoogleAuthenticatorKey generateSecret() {
        return gAuth.createCredentials();
    }

    public static boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }

    public static String getOtpAuthURL(String user, String secret) {
        return String.format("otpauth://totp/%s?secret=%s&issuer=%s","Smartzi" , secret, user);
    }
}