package com.example.duo_poc.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthResponseDto {
    private String accessToken;
    private String email;
    private String password;
    private int roleId;
    private int userId;
    private String fullName;
    private String userRoleName;
    private String mobileNumber;
}