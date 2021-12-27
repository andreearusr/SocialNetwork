package com.map.socialnetwork.repository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AbstractRepository {
    protected final String url;
    protected final String username;
    protected final String password;
}
