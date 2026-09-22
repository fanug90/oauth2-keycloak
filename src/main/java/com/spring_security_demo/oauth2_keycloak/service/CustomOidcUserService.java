package com.spring_security_demo.oauth2_keycloak.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final JwtDecoder jwtDecoder;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    public CustomOidcUserService(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser defaultUser =  super.loadUser(userRequest);

        // Get the access token
        String accessToken = userRequest.getAccessToken().getTokenValue();

        // Extract the role
        Set<GrantedAuthority> keycloakRoles = extractKeycloakRoles(accessToken);
        keycloakRoles.addAll(defaultUser.getAuthorities());

        return new DefaultOidcUser(
                keycloakRoles,
                defaultUser.getIdToken(),
                defaultUser.getUserInfo()
        );


    }

    private Set<GrantedAuthority> extractKeycloakRoles(String accessToken) {
        // Decode the access token
        Jwt jwt = jwtDecoder.decode(accessToken);

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if(resourceAccess == null) {
            return Collections.emptySet();
        }

        Map<String, Object> clientResource = (Map<String, Object>) resourceAccess.get(clientId);

        if(clientResource == null) {
            return Collections.emptySet();
        }

        List<String> roles = (List<String>) clientResource.get("roles");

        if(roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_"+role.toUpperCase()))
                .collect(Collectors.toSet());

    }
}
