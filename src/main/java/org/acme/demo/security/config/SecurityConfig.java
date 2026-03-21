package org.acme.demo.security.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import org.acme.demo.security.config.properties.HeaderFilterProperties;
import org.acme.demo.security.config.properties.HeadersProperties;
import org.acme.demo.security.filter.MtlsHeadersAuthenticationFilter;
import org.acme.demo.security.filter.RequestResponseHeaderLoggingFilter;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({HeaderFilterProperties.class, HeadersProperties.class})
public class SecurityConfig {

    /**
     * Granted by {@link MtlsHeadersAuthenticationFilter} when mTLS headers validate successfully.
     */
    public static final String ROLE_MTLS_CLIENT = "MTLS_CLIENT";

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            HeadersProperties headersProperties,
            RequestResponseHeaderLoggingFilter requestResponseHeaderLoggingFilter,
            MtlsHeadersAuthenticationFilter mtlsHeadersAuthenticationFilter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                    authorize.requestMatchers("/error").permitAll();
                    if (headersProperties.requireMtlsHeaders()) {
                        authorize.requestMatchers("/api/**").hasRole(ROLE_MTLS_CLIENT);
                        authorize.anyRequest().denyAll();
                    } else {
                        authorize.anyRequest().permitAll();
                    }
                })
                .addFilterBefore(requestResponseHeaderLoggingFilter, SecurityContextHolderFilter.class)
                .addFilterAfter(mtlsHeadersAuthenticationFilter, SecurityContextHolderFilter.class);

        return http.build();
    }
}
