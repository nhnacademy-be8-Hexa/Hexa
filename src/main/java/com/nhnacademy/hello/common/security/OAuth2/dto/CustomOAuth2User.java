package com.nhnacademy.hello.common.security.OAuth2.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2Response oAuth2Response;

    private final String role;

    public CustomOAuth2User(OAuth2Response oAuth2Response, String role) {
        this.oAuth2Response = oAuth2Response;
        this.role = role;
    }

    @Override
    public Map<String, Object> getAttributes() {
        Map<String,Object> attributes = new HashMap<String,Object>();
        attributes.put("providerId",oAuth2Response.getProviderId());
        attributes.put("email",oAuth2Response.getEmail());
        attributes.put("name",oAuth2Response.getName());
        attributes.put("phone",oAuth2Response.getPhone());
        attributes.put("birthday",oAuth2Response.getBirthday());
        attributes.put("provider",oAuth2Response.getProvider());
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return role;
            }
        });

        return collection;

    }

    @Override
    public String getName() {
        return oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
    }

    public String getUsername(){
        return oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
    }
}
