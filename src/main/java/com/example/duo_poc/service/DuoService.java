package com.example.duo_poc.service;
import com.duosecurity.Client;
import com.duosecurity.exception.DuoException;
import com.duosecurity.model.Token;

public interface DuoService {

    public  Client getClient() throws DuoException;

    public String createAuthUrl(String username, String state) throws DuoException;

    public Token exchangeAuthorizationCode(String code, String username) throws Exception;
}
