package com.example.duo_poc.dao;

import com.example.duo_poc.dto.request.user.InsertUserDto;
import com.example.duo_poc.dto.response.GeneralResponse;
import com.example.duo_poc.dto.request.user.InsertUserDto;
import com.example.duo_poc.dto.request.user.PasswordChangeDto;
import com.example.duo_poc.dto.response.GeneralResponse;
import com.example.duo_poc.dto.response.user.UserAuthResponseDto;

public interface UserAuthDao {

    public GeneralResponse insertNewUser(InsertUserDto insertUserDto);

    public UserAuthResponseDto findByEmail(String email);

    public GeneralResponse changePassword(PasswordChangeDto passwordChangeDto);
}
