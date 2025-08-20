package com.example.duo_poc.service.impl;

import com.example.duo_poc.dto.request.user.InsertUserDto;
import com.example.duo_poc.dto.request.user.PasswordChangeDto;
import com.example.duo_poc.dto.request.user.UserAuthRequest;
import com.example.duo_poc.dto.request.user.UserVerificationRequestDto;
import com.example.duo_poc.dto.response.GeneralResponse;
import com.example.duo_poc.dto.response.user.UserVerificationResponseDto;
import com.example.duo_poc.dao.UserAuthDao;
import com.example.duo_poc.service.TotpService;
import com.example.duo_poc.service.UserAuthService;
import com.example.duo_poc.util.PasswordUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserAuthServiceImpl implements UserAuthService {

    @Autowired
    private UserAuthDao userAuthDao;

    @Autowired
    private TotpService totpService;

    @Override
    public GeneralResponse insertNewUser(InsertUserDto insertUserDto) {
        GeneralResponse generalResponse = new GeneralResponse();

        if (insertUserDto.getUserRoleId() == 3) {
            return userAuthDao.insertNewUser(insertUserDto);
        } else {
            generalResponse.setData(null);
            generalResponse.setMsg("Invalid User Role Id");
            generalResponse.setRes(false);
            generalResponse.setStatusCode(409);
        }

        return generalResponse;
    }


    @Override
    public GeneralResponse verifyUser(UserVerificationRequestDto userVerificationRequestDto) {
        String email = userVerificationRequestDto.getEmail();
        String password = userVerificationRequestDto.getPassword();

        GeneralResponse generalResponse = new GeneralResponse();
        UserVerificationResponseDto userVerificationResponseDto = new UserVerificationResponseDto();

        userVerificationResponseDto = userAuthDao.findByEmail(email);

        if (userVerificationResponseDto.getEmail() == null) {
            log.error("Empty email or user not found");
            generalResponse.setMsg("Empty email or user not found");
            return generalResponse;

        } else if (userVerificationResponseDto.getPassword().equals(PasswordUtils.hashSHA256(password))) {
            userAuthDao.verifyUser(email);
            generalResponse.setData(userVerificationResponseDto);
            generalResponse.setMsg("Authenticated");
            generalResponse.setRes(true);
            generalResponse.setStatusCode(200);
            return generalResponse;
        } else {
            log.error("Invalid email or password");
            generalResponse.setMsg("Not-Authenticated");
            generalResponse.setStatusCode(403);
            return generalResponse;
        }

    }

    @Override
    public GeneralResponse authenticateUser(UserAuthRequest userAuthRequest){
        GeneralResponse generalResponse = new GeneralResponse();
        generalResponse.setData(totpService.verifyUser(userAuthRequest.getEmail(),userAuthRequest.getOtp()));
        return  generalResponse;
    }

    @Override
    public GeneralResponse changePassword(PasswordChangeDto passwordChangeDto) {
        return userAuthDao.changePassword(passwordChangeDto);
    }

}
