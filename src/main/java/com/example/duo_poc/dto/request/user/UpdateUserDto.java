package com.example.duo_poc.dto.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {
    private String email;
    private String mobileNumber;
    private String firstName;
    private String lastName;
    private int userRoleId;
    private String password;
}
