package com.example.duo_poc.dto.request.user;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Getter
@Setter
public class UserAuthRequest {
    private String email;
    private int otp;
}
