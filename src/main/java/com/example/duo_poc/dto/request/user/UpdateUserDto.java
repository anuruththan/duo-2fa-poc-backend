package com.example.duo_poc.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {
    private String email;
    private String mobileNumber;
    private String firstName;
    private String lastName;
    private int userRoleId;
    private String password;
}
