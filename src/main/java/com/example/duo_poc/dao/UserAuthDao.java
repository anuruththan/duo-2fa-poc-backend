package com.example.duo_poc.dao;

import com.example.duo_poc.dto.request.user.InsertUserDto;
import com.example.duo_poc.dto.response.GeneralResponse;
import com.example.duo_poc.dto.request.user.PasswordChangeDto;
import com.example.duo_poc.dto.response.user.UserVerificationResponseDto;

public interface UserAuthDao {

    public GeneralResponse insertNewUser(InsertUserDto insertUserDto);

    public UserVerificationResponseDto findByEmail(String email);

    public GeneralResponse changePassword(PasswordChangeDto passwordChangeDto);

    public void verifyUser(String email);

    public void unverifyUser(String email);

    public String getSecretKey(String email);

    public void insertSecretKey(String email, String secretKey);
}
