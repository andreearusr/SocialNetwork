package com.map.socialnetwork.service;

import com.map.socialnetwork.domain.Credentials;
import com.map.socialnetwork.exceptions.AuthenticationException;
import com.map.socialnetwork.repository.CredentialsRepository;
import com.map.socialnetwork.utils.Hashes;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

public class Authentication {
    private final CredentialsRepository credentialsRepository;

    public Authentication(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    @Getter
    private Long userId = null;

    public Long logIn(String username, String password) throws AuthenticationException {
        if (userId != null) {
            throw new AuthenticationException("Already logged in!");
        }

        Optional<Long> id = credentialsRepository.getId(username);

        if (id.isEmpty()) {
            throw new AuthenticationException("Wrong username or password!");
        }

        if (Objects.equals(credentialsRepository.getPassword(id.get()), Hashes.MD5(password))) {
            userId = id.get();
        } else {
            throw new AuthenticationException("Wrong username or password!");
        }

        return id.get();
    }

    public void logOut() throws AuthenticationException {
        if (userId == null) {
            throw new AuthenticationException("You are not logged in!");
        }

        userId = null;
    }

    public void addCredentials(Long userId, String username, String password) {
        credentialsRepository.store(userId, Credentials.of(username, password));
    }

    public void changePassword(String newPassword) throws AuthenticationException {
        if (userId == null) {
            throw new AuthenticationException("You are not logged in!");
        }

        credentialsRepository.update(userId, Hashes.MD5(newPassword));
    }

    public void deleteCredentials() throws AuthenticationException {
        if (userId == null) {
            throw new AuthenticationException("You are not logged in!");
        }

        credentialsRepository.delete(userId);
    }
}
