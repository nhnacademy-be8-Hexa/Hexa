package com.nhnacademy.hello.common.security.OAuth2.dto;

public interface OAuth2Response {

    String getProvider();

    String getProviderId();

    String getEmail();

    String getName();

    String getPhone();

    String getBirthday();
}
