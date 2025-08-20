package com.example.duo_poc.dto.response.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateSecretResponse {
    private String secret;
    private String otpauthUrl;
}