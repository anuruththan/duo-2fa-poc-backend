package com.example.duo_poc.service;

import com.example.duo_poc.dto.request.user.InsertUserDto;
import com.example.duo_poc.dto.request.user.UserAuthRequestDto;
import com.example.duo_poc.dto.request.user.PasswordChangeDto;
import com.example.duo_poc.dto.response.GeneralResponse;
import com.example.duo_poc.dto.response.user.UserAuthResponseDto;

public interface UserAuthService {

    public GeneralResponse insertNewUser(InsertUserDto insertUserDto);

    // Add your new service here..........................................................

    public GeneralResponse authenticateUser(UserAuthRequestDto userAuthRequestDto);

    public GeneralResponse changePassword(PasswordChangeDto passwordChangeDto);


}
