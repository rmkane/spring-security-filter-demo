package org.acme.demo.security.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import org.acme.demo.security.filter.RequestResponseHeaderLoggingFilter;

@Configuration
@EnableConfigurationProperties(HeaderFilterProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RequestResponseHeaderLoggingFilter requestResponseHeaderLoggingFilter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(requestResponseHeaderLoggingFilter, SecurityContextHolderFilter.class);

        return http.build();
    }
}
