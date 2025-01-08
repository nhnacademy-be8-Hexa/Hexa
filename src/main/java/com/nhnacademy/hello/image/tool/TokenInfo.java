package com.nhnacademy.hello.image.tool;


import java.time.Instant;

public class TokenInfo {
    private String id;
    private Instant expires;

    public TokenInfo(String id, Instant expires) {
        this.id = id;
        this.expires = expires;
    }

    public String getId() {
        return id;
    }

    public Instant getExpires() {
        return expires;
    }
}
