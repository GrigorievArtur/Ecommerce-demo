package com.example.ecommercedemo.configs.auth;

import com.example.ecommercedemo.components.carts.CartCookie;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.filters.auth.JwtFilter;
import com.example.ecommercedemo.services.carts.CartService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.io.IOException;

/**
 * Two filter chains:
 * <ul>
 *     <li>{@code /api/**} — stateless, JWT bearer tokens, no CSRF (used by tests &amp; integrations).</li>
 *     <li>everything else — server-rendered site with session form login and CSRF protection.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain apiFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/cart/**").permitAll()
                        .requestMatchers("/api/products/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webFilterChain(HttpSecurity http, AuthenticationSuccessHandler successHandler) throws Exception {
        var csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName("_csrf");

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/account/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .csrf(csrf -> csrf.csrfTokenRequestHandler(csrfHandler));

        return http.build();
    }

    /**
     * Merges any guest cart into the user's cart, then redirects to the originally-requested
     * page after login — but never onto /error (which can surface when a stale saved request
     * points at an error URL).
     */
    @Bean
    AuthenticationSuccessHandler authenticationSuccessHandler(CartService cartService) {
        SavedRequestAwareAuthenticationSuccessHandler handler =
                new SavedRequestAwareAuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        Authentication authentication)
                            throws IOException, ServletException {
                        if (authentication.getPrincipal() instanceof User user) {
                            cartService.mergeGuestCartIntoAccount(CartCookie.read(request), user);
                        }
                        super.onAuthenticationSuccess(request, response, authentication);
                    }

                    @Override
                    protected String determineTargetUrl(HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        Authentication authentication) {
                        String cont = request.getParameter("continue");
                        if (cont != null && cont.startsWith("/") && !cont.startsWith("//") && !cont.startsWith("/error")) {
                            return cont;
                        }
                        String target = super.determineTargetUrl(request, response, authentication);
                        return (target != null && target.startsWith("/error")) ? "/account" : target;
                    }
                };
        handler.setDefaultTargetUrl("/account");
        return handler;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
