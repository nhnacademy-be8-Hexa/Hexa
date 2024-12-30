package com.nhnacademy.hello.common.security.OAuth2.dto.impl;



import com.nhnacademy.hello.common.security.OAuth2.dto.OAuth2Response;

import java.util.Map;

public class PaycoResponse implements OAuth2Response {

    public PaycoResponse(Map<String, Object> attribute) {

        Map<String,Object> tmp1 = (Map<String,Object>)attribute.get("data");
        Map<String,Object> tmp2 = (Map<String,Object>)tmp1.get("data");
        Map<String,Object> tmp3 = (Map<String,Object>)tmp1.get("member");
        Map<String,Object> tmp4 = (Map<String,Object>)tmp1.get("member");


        this.attribute = tmp4 ;
    }

    private final Map<String, Object> attribute;

    @Override
    public String getProvider() {
        return "payco";
    }

    @Override
    public String getProviderId() {
        return attribute.get("idNo").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email") != null ? attribute.get("email").toString() : "";
    }

    @Override
    public String getName() {
        return attribute.get("name") != null ? attribute.get("name").toString() : "";
    }

    @Override
    public String getPhone() {
        if (attribute.get("mobile") != null) {
            String phone = attribute.get("mobile").toString();
            return phone.startsWith("82") ? phone.replaceFirst("^82", "0") : phone;
        }
        return "";
    }

    @Override
    public String getBirthday() {
        return attribute.get("birthdayMMdd") != null ? attribute.get("birthdayMMdd").toString() : "";
    }



}
