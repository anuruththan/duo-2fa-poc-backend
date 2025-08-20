package com.example.duo_poc.service.impl;

import com.example.duo_poc.dto.response.user.GenerateSecretResponse;
import com.example.duo_poc.dto.response.user.UserAuthResponseDto;
import com.example.duo_poc.service.TotpService;
import com.example.duo_poc.util.TotpUtil;
import com.example.duo_poc.dao.UserAuthDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TotpServiceImpl implements TotpService {

    @Autowired
    private UserAuthDao userDao;

    public GenerateSecretResponse generateSecretForUser(String email) {
        String existing = userDao.getSecretKey(email);
        String secret;
        if (existing == null) {
            secret = TotpUtil.generateSecret().getKey();
            userDao.insertSecretKey(email, secret);
        } else {
            secret = existing;
        }
        GenerateSecretResponse resp = new GenerateSecretResponse();
        resp.setSecret(secret);
        resp.setOtpauthUrl(TotpUtil.getOtpAuthURL(email, secret));
        return resp;
    }

    public UserAuthResponseDto verifyUser(String email, int code) {
        String secret = userDao.getSecretKey(email);
        UserAuthResponseDto resp = new UserAuthResponseDto();
        if (secret == null) {
            resp.setVerified(false);
            return resp;
        }
        boolean verified = TotpUtil.verifyCode(secret, code);
        if (verified) {
            userDao.verifyUser(email);
            resp.setVerified(true);
        } else {
            resp.setVerified(false);
        }
        return resp;
    }
}