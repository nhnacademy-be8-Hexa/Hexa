package com.nhnacademy.hello.common.security.OAuth2.dto.impl;


import com.nhnacademy.hello.common.security.OAuth2.dto.OAuth2Response;

import java.util.Map;

public class NaverResponse implements OAuth2Response {

    private final Map<String,Object> attribute;

    public NaverResponse(Map<String, Object> attribute) {

        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return attribute.get("name").toString();
    }

    @Override
    public String getPhone() {
        if (attribute.get("mobile") != null) {
            String phone = attribute.get("mobile").toString();
            return phone.replaceAll("-", "");
        }
        return "";
    }

    @Override
    public String getBirthday() {

        return  (attribute.get("birthyear") != null ? attribute.get("birthyear").toString() : "") +
                (attribute.get("birthday") != null ? attribute.get("birthday").toString().replace("-", "") : "");
    }
}
