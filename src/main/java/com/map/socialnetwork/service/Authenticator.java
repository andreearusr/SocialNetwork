package com.map.socialnetwork.service;

import com.map.socialnetwork.domain.Credentials;
import com.map.socialnetwork.exceptions.AuthenticationException;
import com.map.socialnetwork.exceptions.DuplicateEntityException;
import com.map.socialnetwork.exceptions.ValidatorException;
import com.map.socialnetwork.repository.credentialsRepository.CredentialsDBRepository;
import com.map.socialnetwork.utils.Hashes;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

public class Authenticator {
    private final CredentialsDBRepository credentialsDBRepository;

    public Authenticator(CredentialsDBRepository credentialsDBRepository) {
        this.credentialsDBRepository = credentialsDBRepository;
    }

    @Getter
    private Long userId = null;

    public Long logIn(String username, String password) throws AuthenticationException {
        if (userId != null) {
            throw new AuthenticationException("Already logged in!");
        }

        Optional<Long> id = credentialsDBRepository.getId(username);

        if (id.isEmpty()) {
            throw new AuthenticationException("Wrong username or password!");
        }

        if (Objects.equals(credentialsDBRepository.getPassword(id.get()), Hashes.MD5(password))) {
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

    public void addCredentials(Long userId, String username, String password) throws ValidatorException, DuplicateEntityException {
        credentialsDBRepository.store(userId, Credentials.of(username, password));
    }

    public Boolean checkIfExistsUsername(String userName){
        return credentialsDBRepository.checkIfExistsUsername(userName);
    }

    public void changePassword(String newPassword) throws AuthenticationException {
        if (userId == null) {
            throw new AuthenticationException("You are not logged in!");
        }

        credentialsDBRepository.update(userId, Hashes.MD5(newPassword));
    }

    public void deleteCredentials() throws AuthenticationException {
        if (userId == null) {
            throw new AuthenticationException("You are not logged in!");
        }

        credentialsDBRepository.delete(userId);
    }
}
