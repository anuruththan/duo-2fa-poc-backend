package com.example.duo_poc.controller;

import com.example.duo_poc.dto.request.user.*;
import com.example.duo_poc.dto.response.GeneralResponse;
import com.example.duo_poc.dto.response.user.GenerateSecretResponse;
import com.example.duo_poc.service.TotpService;
import com.example.duo_poc.service.UserAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/auth")
public class UserAuthController {

    @Autowired
    private TotpService totpService;

    @Autowired
    private UserAuthService userAuthService;


    @RequestMapping(path = "/signUp", method = RequestMethod.POST)
    public GeneralResponse login(@RequestBody InsertUserDto insertUserDto) {
        GeneralResponse generalResponse;
        generalResponse = userAuthService.insertNewUser(insertUserDto);
        return generalResponse;
    }

    @RequestMapping(path = "/verify", method=RequestMethod.POST)
    public GeneralResponse login(@RequestBody UserVerificationRequestDto userVerificationRequestDto) {
        GeneralResponse generalResponse;
        generalResponse = userAuthService.verifyUser(userVerificationRequestDto);
        return generalResponse;
    }

    @RequestMapping(path = "/authenticate", method=RequestMethod.POST)
    public GeneralResponse authenticate(@RequestBody UserAuthRequest userAuthRequestDto) {
        GeneralResponse generalResponse;
        generalResponse = userAuthService.authenticateUser(userAuthRequestDto);
        return generalResponse;
    }

    @RequestMapping(path = "/enable_2fa", method=RequestMethod.POST)
    public GeneralResponse enable2fa(@RequestBody GenerateSecretRequest generateSecretRequest) {
        GeneralResponse generalResponse = new GeneralResponse();
        GenerateSecretResponse generateSecretResponse;

        generateSecretResponse = totpService.generateSecretForUser(generateSecretRequest.getEmail());
        if (generateSecretResponse.getOtpauthUrl()!=null) {
            generalResponse.setData(generateSecretResponse);
            generalResponse.setRes(true);
            generalResponse.setMsg("User secret key generated successfully");
            generalResponse.setStatusCode(200);
        }
        else {
            generalResponse.setMsg("User secret key does not exist");
            generalResponse.setStatusCode(400);
        }
        return generalResponse;
    }


    @RequestMapping(path = "/changePassword", method = RequestMethod.POST)
    public ResponseEntity<GeneralResponse> changePassword(@RequestBody PasswordChangeDto passwordChangeDto) {
        GeneralResponse generalResponse = new GeneralResponse();
        generalResponse = userAuthService.changePassword(passwordChangeDto);
        return new ResponseEntity<>(generalResponse, HttpStatus.valueOf(generalResponse.getStatusCode()));
    }

}