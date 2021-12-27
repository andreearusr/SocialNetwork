package com.map.socialnetwork.domain;

import com.map.socialnetwork.utils.Hashes;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Credentials {
    private final String username;
    private final String password;

    public static Credentials of(String username, String password) {
        return new Credentials(username, Hashes.MD5(password));
    }
}
