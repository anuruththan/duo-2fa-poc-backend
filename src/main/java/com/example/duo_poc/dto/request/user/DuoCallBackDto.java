package com.example.duo_poc.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DuoCallBackDto {
    String code;
    String state;
    String email;
    int userRoleId;
}
