package com.example.duo_poc.controller;

import com.example.duo_poc.dto.request.user.InsertUserDto;
import com.example.duo_poc.dto.request.user.UserAuthRequestDto;
import com.example.duo_poc.dto.request.user.PasswordChangeDto;
import com.example.duo_poc.dto.response.GeneralResponse;
import com.example.duo_poc.service.UserAuthService;
import com.example.duo_poc.util.JwtUtil;
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
    private UserAuthService userAuthService;

    @Autowired
    private JwtUtil jwtUtil;

    @RequestMapping(path="/signUp", method=RequestMethod.POST)
    public GeneralResponse login(@RequestBody InsertUserDto insertUserDto) {
        GeneralResponse generalResponse = new GeneralResponse();
        generalResponse = userAuthService.insertNewUser(insertUserDto);
        return generalResponse; //get otp and send through the email
    }

    //add the verified email by the user

    @RequestMapping(path="/login", method=RequestMethod.POST)
    public GeneralResponse login(@RequestBody UserAuthRequestDto userAuthRequestDto) {
        GeneralResponse generalResponse = new GeneralResponse();
        generalResponse = userAuthService.authenticateUser(userAuthRequestDto);
        return generalResponse;
    }

    @RequestMapping(path="/changePassword", method=RequestMethod.POST)
    public ResponseEntity<GeneralResponse> changePassword(@RequestBody PasswordChangeDto passwordChangeDto) {
        GeneralResponse generalResponse = new GeneralResponse();
        generalResponse = userAuthService.changePassword(passwordChangeDto);
        return new ResponseEntity<>(generalResponse, HttpStatus.valueOf(generalResponse.getStatusCode()));
    }

}