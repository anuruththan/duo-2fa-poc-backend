package com.example.duo_poc.service;

import com.example.duo_poc.dto.request.user.InsertUserDto;
import com.example.duo_poc.dto.request.user.UserAuthRequest;
import com.example.duo_poc.dto.request.user.UserVerificationRequestDto;
import com.example.duo_poc.dto.request.user.PasswordChangeDto;
import com.example.duo_poc.dto.response.GeneralResponse;

public interface  UserAuthService {

    public GeneralResponse insertNewUser(InsertUserDto insertUserDto);

    // Add your new service here..........................................................

    public GeneralResponse verifyUser(UserVerificationRequestDto userVerificationRequestDto);

    public GeneralResponse authenticateUser(UserAuthRequest userAuthRequest);

    public GeneralResponse changePassword(PasswordChangeDto passwordChangeDto);

}
