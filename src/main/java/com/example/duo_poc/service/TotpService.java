package com.example.duo_poc.service;

import com.example.duo_poc.dto.response.user.GenerateSecretResponse;
import com.example.duo_poc.dto.response.user.UserAuthResponseDto;

public interface TotpService {

    public GenerateSecretResponse generateSecretForUser(String email);

    public UserAuthResponseDto verifyUser(String email, int code);

}
