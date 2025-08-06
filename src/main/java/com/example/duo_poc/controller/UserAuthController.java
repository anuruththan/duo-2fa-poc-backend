package com.example.duo_poc.controller;

import com.duosecurity.exception.DuoException;
import com.duosecurity.model.AuthResult;
import com.example.duo_poc.dto.request.user.InsertUserDto;
import com.example.duo_poc.dto.request.user.UserAuthRequestDto;
import com.example.duo_poc.dto.request.user.PasswordChangeDto;
import com.example.duo_poc.dto.response.GeneralResponse;
import com.example.duo_poc.service.DuoService;
import com.example.duo_poc.service.UserAuthService;
import com.example.duo_poc.util.JwtUtil;
import com.example.duo_poc.dto.response.user.UserAuthResponseDto;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.duosecurity.model.Token;

import java.util.Collections;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/auth")
public class UserAuthController {

    @Autowired
    private DuoService duoService;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private JwtUtil jwtUtil;


    @RequestMapping(path = "/signUp", method = RequestMethod.POST)
    public GeneralResponse login(@RequestBody InsertUserDto insertUserDto) {
        GeneralResponse generalResponse = new GeneralResponse();
        generalResponse = userAuthService.insertNewUser(insertUserDto);
        return generalResponse;
    }



    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public GeneralResponse login(@RequestBody UserAuthRequestDto userAuthRequestDto) throws DuoException {
        GeneralResponse generalResponse = new GeneralResponse();
        generalResponse = userAuthService.authenticateUser(userAuthRequestDto);

        if (generalResponse.getRes()) {
            UserAuthResponseDto user = (UserAuthResponseDto) generalResponse.getData();
            String state = UUID.randomUUID().toString();
            httpSession.setAttribute("duo_state", state);
            httpSession.setAttribute("duo_user_role_name", user.getUserRoleName());
            httpSession.setAttribute("duo_email", user.getEmail());
            httpSession.setAttribute("duo_roleId", user.getRoleId());
            String duoUrl = duoService.createAuthUrl(user.getUserRoleName(), state);
            generalResponse.setMsg("Password OK. Complete Duo 2FA.");
            generalResponse.setData(Collections.singletonMap("duo_url", duoUrl));
        }
        return generalResponse;
    }

    // This endpoint should match your duo.redirect.uri in Duo Admin Panel
    @RequestMapping(value = "/verify", method = RequestMethod.GET)
    public GeneralResponse duoCallback(
            @RequestParam("state") String state,
            @RequestParam("duo_code") String code) {

        GeneralResponse generalResponse = new GeneralResponse();
        String expectedState = (String) httpSession.getAttribute("duo_state");
        String user_role_name = (String) httpSession.getAttribute("duo_user_role_name");
        String email = (String) httpSession.getAttribute("duo_email");
        Integer roleId = (Integer) httpSession.getAttribute("duo_roleId");

        if (expectedState == null || !expectedState.equals(state)) {
            generalResponse.setMsg("Invalid state. Possible CSRF attack.");
            generalResponse.setStatusCode(403);
            return generalResponse;
        }

        try {
            Token token = duoService.exchangeAuthorizationCode(code, user_role_name);
            String username = token.getSub(); // Or token.getUser().getName()
            String displayName = token.getPreferred_username();
            AuthResult authResult = token.getAuth_result(); // Or token.getAuth_context().getResult()
            if (authResult == null) {
                generalResponse.setMsg("No AuthResult from Duo response.");
                generalResponse.setStatusCode(401);
                return generalResponse;
            }

            String status = authResult.getStatus();
            String result = authResult.getResult();
            if ("allow".equalsIgnoreCase(result) || "allow".equalsIgnoreCase(status)) {
                String jwt = jwtUtil.generateToken(email, roleId);
                generalResponse.setData(Collections.singletonMap("accessToken", jwt));
                generalResponse.setStatusCode(200);
                generalResponse.setMsg("Duo 2FA successful.");
                generalResponse.setRes(true);
                return generalResponse;
            } else {
                log.error("Invalid token response: {}", result);
                generalResponse.setMsg("Duo 2FA denied.");
                generalResponse.setStatusCode(401);
                return generalResponse;
            }
        } catch (Exception e) {
            log.error("Error occurred while processing Duo 2FA due to {}", e.getMessage());
            generalResponse.setStatusCode(500);
            generalResponse.setMsg("Duo 2FA error: " + e.getMessage());
            return generalResponse;
        }
    }

    @RequestMapping(path = "/changePassword", method = RequestMethod.POST)
    public ResponseEntity<GeneralResponse> changePassword(@RequestBody PasswordChangeDto passwordChangeDto) {
        GeneralResponse generalResponse = new GeneralResponse();
        generalResponse = userAuthService.changePassword(passwordChangeDto);
        return new ResponseEntity<>(generalResponse, HttpStatus.valueOf(generalResponse.getStatusCode()));
    }

}