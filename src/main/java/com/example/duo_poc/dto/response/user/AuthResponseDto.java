package com.example.duo_poc.dto.response.user;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDto {
    private String token;
    private int roleId;
    private String email;
}
