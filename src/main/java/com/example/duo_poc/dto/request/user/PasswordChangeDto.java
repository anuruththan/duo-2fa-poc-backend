package com.example.duo_poc.dto.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeDto {
    private String emailId;
    private String oldPassword;
    private String newPassword;
}
