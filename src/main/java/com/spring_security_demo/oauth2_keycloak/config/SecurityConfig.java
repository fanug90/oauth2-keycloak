package com.spring_security_demo.oauth2_keycloak.config;

import com.spring_security_demo.oauth2_keycloak.service.CustomOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CustomOidcUserService oidcUserService;


    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository, CustomOidcUserService oidcUserService) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.oidcUserService = oidcUserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        OidcClientInitiatedLogoutSuccessHandler handler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        http
               .authorizeHttpRequests(auth->auth
                       .requestMatchers("/login","/error").permitAll()
                       .requestMatchers("/user").hasRole("USER")
                       .requestMatchers("/manager").hasRole("MANAGER")
                       .requestMatchers("/admin").hasRole("ADMIN")
                       .anyRequest().authenticated())
                               .oauth2Login(oauth2->oauth2
                               .loginPage("/login")
                               .defaultSuccessUrl("/home", true)
                                       .userInfoEndpoint(
                                               userInfo -> userInfo
                                                       .oidcUserService(oidcUserService))
                               )
         .logout(logout->logout
                .logoutSuccessHandler(handler));
       return http.build();


    }
}
