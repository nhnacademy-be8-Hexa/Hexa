package com.nhnacademy.hello.common.security.OAuth2.service;



import com.nhnacademy.hello.common.security.OAuth2.converter.CustomOAuth2UserRequestEntityConverter;
import com.nhnacademy.hello.common.security.OAuth2.dto.CustomOAuth2User;
import com.nhnacademy.hello.common.security.OAuth2.dto.OAuth2Response;
import com.nhnacademy.hello.common.security.OAuth2.dto.impl.NaverResponse;
import com.nhnacademy.hello.common.security.OAuth2.dto.impl.PaycoResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.*;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String MISSING_USER_INFO_URI_ERROR_CODE = "missing_user_info_uri";

    private static final String MISSING_USER_NAME_ATTRIBUTE_ERROR_CODE = "missing_user_name_attribute";

    private static final String INVALID_USER_INFO_RESPONSE_ERROR_CODE = "invalid_user_info_response";

    private static final ParameterizedTypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE = new ParameterizedTypeReference<>() {
    };

    private Converter<OAuth2UserRequest, RequestEntity<?>> requestEntityConverter = new CustomOAuth2UserRequestEntityConverter();

    private Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter = (
            request) -> (attributes) -> attributes;

    private RestOperations restOperations;

    public CustomOAuth2UserService() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        this.restOperations = restTemplate;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Assert.notNull(userRequest, "userRequest cannot be null");
        String userNameAttributeName = getUserNameAttributeName(userRequest);
        RequestEntity<?> request = this.requestEntityConverter.convert(userRequest);
        ResponseEntity<Map<String, Object>> response = getResponse(userRequest, request);
        OAuth2AccessToken token = userRequest.getAccessToken();
        Map<String, Object> attributes = this.attributesConverter.convert(userRequest).convert(response.getBody());
        Collection<GrantedAuthority> authorities = getAuthorities(token, attributes, userNameAttributeName);
        OAuth2User oAuth2User = new DefaultOAuth2User(authorities, attributes, userNameAttributeName);


        String registrationId = userRequest.getClientRegistration().getRegistrationId();


        System.out.println(oAuth2User.getAttributes());

        OAuth2Response oAuth2Response = null;

        if(registrationId.equals("naver")){

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
//        else if(registrationId.equals("google")){
//
//            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
//        }
        else if(registrationId.equals("payco")){

            oAuth2Response = new PaycoResponse(oAuth2User.getAttributes());

        }
        else {
            return null;
        }

        String username = oAuth2Response.getProvider()+"_"+oAuth2Response.getProviderId();

        String role = "ROLE_MEMBER";


        return new CustomOAuth2User(oAuth2Response , role );

    }

    /**
     * Use this strategy to adapt user attributes into a format understood by Spring
     * Security; by default, the original attributes are preserved.
     *
     * <p>
     * This can be helpful, for example, if the user attribute is nested. Since Spring
     * Security needs the username attribute to be at the top level, you can use this
     * method to do:
     *
     * <pre>
     *     DefaultOAuth2UserService userService = new DefaultOAuth2UserService();
     *     userService.setAttributesConverter((userRequest) -> (attributes) ->
     *         Map&lt;String, Object&gt; userObject = (Map&lt;String, Object&gt;) attributes.get("user");
     *         attributes.put("user-name", userObject.get("user-name"));
     *         return attributes;
     *     });
     * </pre>
     * @param attributesConverter the attribute adaptation strategy to use
     * @since 6.3
     */
    public void setAttributesConverter(
            Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter) {
        Assert.notNull(attributesConverter, "attributesConverter cannot be null");
        this.attributesConverter = attributesConverter;
    }

    private ResponseEntity<Map<String, Object>> getResponse(OAuth2UserRequest userRequest, RequestEntity<?> request) {
        try {
            return this.restOperations.exchange(request, PARAMETERIZED_RESPONSE_TYPE);
        }
        catch (OAuth2AuthorizationException ex) {
            OAuth2Error oauth2Error = ex.getError();
            StringBuilder errorDetails = new StringBuilder();
            errorDetails.append("Error details: [");
            errorDetails.append("UserInfo Uri: ")
                    .append(userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri());
            errorDetails.append(", Error Code: ").append(oauth2Error.getErrorCode());
            if (oauth2Error.getDescription() != null) {
                errorDetails.append(", Error Description: ").append(oauth2Error.getDescription());
            }
            errorDetails.append("]");
            oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
                    "An error occurred while attempting to retrieve the UserInfo Resource: " + errorDetails.toString(),
                    null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
        }
        catch (UnknownContentTypeException ex) {
            String errorMessage = "An error occurred while attempting to retrieve the UserInfo Resource from '"
                    + userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri()
                    + "': response contains invalid content type '" + ex.getContentType().toString() + "'. "
                    + "The UserInfo Response should return a JSON object (content type 'application/json') "
                    + "that contains a collection of name and value pairs of the claims about the authenticated End-User. "
                    + "Please ensure the UserInfo Uri in UserInfoEndpoint for Client Registration '"
                    + userRequest.getClientRegistration().getRegistrationId() + "' conforms to the UserInfo Endpoint, "
                    + "as defined in OpenID Connect 1.0: 'https://openid.net/specs/openid-connect-core-1_0.html#UserInfo'";
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE, errorMessage, null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
        }
        catch (RestClientException ex) {
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
                    "An error occurred while attempting to retrieve the UserInfo Resource: " + ex.getMessage(), null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
        }
    }

    private String getUserNameAttributeName(OAuth2UserRequest userRequest) {
        if (!StringUtils
                .hasText(userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri())) {
            OAuth2Error oauth2Error = new OAuth2Error(MISSING_USER_INFO_URI_ERROR_CODE,
                    "Missing required UserInfo Uri in UserInfoEndpoint for Client Registration: "
                            + userRequest.getClientRegistration().getRegistrationId(),
                    null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        if (!StringUtils.hasText(userNameAttributeName)) {
            OAuth2Error oauth2Error = new OAuth2Error(MISSING_USER_NAME_ATTRIBUTE_ERROR_CODE,
                    "Missing required \"user name\" attribute name in UserInfoEndpoint for Client Registration: "
                            + userRequest.getClientRegistration().getRegistrationId(),
                    null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        return userNameAttributeName;
    }

    private Collection<GrantedAuthority> getAuthorities(OAuth2AccessToken token, Map<String, Object> attributes,
                                                        String userNameAttributeName) {
        Collection<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new OAuth2UserAuthority(attributes, userNameAttributeName));
        for (String authority : token.getScopes()) {
            authorities.add(new SimpleGrantedAuthority("SCOPE_" + authority));
        }
        return authorities;
    }

    /**
     * Sets the {@link Converter} used for converting the {@link OAuth2UserRequest} to a
     * {@link RequestEntity} representation of the UserInfo Request.
     * @param requestEntityConverter the {@link Converter} used for converting to a
     * {@link RequestEntity} representation of the UserInfo Request
     * @since 5.1
     */
    public final void setRequestEntityConverter(Converter<OAuth2UserRequest, RequestEntity<?>> requestEntityConverter) {
        Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
        this.requestEntityConverter = requestEntityConverter;
    }

    /**
     * Sets the {@link RestOperations} used when requesting the UserInfo resource.
     *
     * <p>
     * <b>NOTE:</b> At a minimum, the supplied {@code restOperations} must be configured
     * with the following:
     * <ol>
     * <li>{@link ResponseErrorHandler} - {@link OAuth2ErrorResponseErrorHandler}</li>
     * </ol>
     * @param restOperations the {@link RestOperations} used when requesting the UserInfo
     * resource
     * @since 5.1
     */
    public final void setRestOperations(RestOperations restOperations) {
        Assert.notNull(restOperations, "restOperations cannot be null");
        this.restOperations = restOperations;
    }

}

