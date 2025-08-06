package com.example.duo_poc.service.impl;

import com.duosecurity.exception.DuoException;
import com.example.duo_poc.service.DuoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.duosecurity.Client;
import com.duosecurity.model.Token;

@Service
public class DuoServiceImpl implements DuoService {

    @Value("${duo.integration.key}")
    private String clientId; // aka client_id

    @Value("${duo.secret.key}")
    private String clientSecret; // aka client_secret

    @Value("${duo.api.hostname}")
    private String apiHost;

    @Value("${duo.redirect.uri}")
    private String redirectUri;

    @Override
    public Client getClient() throws DuoException {
        return new Client(clientId, clientSecret, apiHost, redirectUri);
    }

    @Override
    public String createAuthUrl(String username, String state) throws DuoException {
        return getClient().createAuthUrl(username, state);
    }

    @Override
    public Token exchangeAuthorizationCode(String code, String username) throws Exception {
        return getClient().exchangeAuthorizationCodeFor2FAResult(code, username);
    }

}
