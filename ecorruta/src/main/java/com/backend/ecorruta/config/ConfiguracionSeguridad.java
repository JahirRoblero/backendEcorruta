package com.backend.ecorruta.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class ConfiguracionSeguridad {

        @Bean
        public SecurityFilterChain cadenaSeguridad(HttpSecurity http) throws Exception {
                http.csrf(
                                configuracion -> configuracion.disable())
                                .cors(
                                                configuracion -> configuracion
                                                                .configurationSource(fuenteDeConfiguracionSource()))

                                .sessionManagement(
                                                configuracion -> configuracion
                                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(autorizacion -> autorizacion.requestMatchers(
                                                "/api/auth/**",
                                                "/api/recuperacion").permitAll()
                                                .requestMatchers(HttpMethod.OPTIONS, "/**")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**")
                                                .hasRole("ADMINISTRADOR")
                                                .anyRequest()
                                                .authenticated())

                                .exceptionHandling(configuracion -> configuracion.authenticationEntryPoint(
                                                (solicitud, respuesta, excepcion) -> {
                                                        respuesta.setStatus(401);
                                                        respuesta.setContentType(
                                                                        "application/json");
                                                        respuesta.getWriter().write(
                                                                        """
                                                                                        {
                                                                                            "mensaje" : "Debes iniciar sesión"
                                                                                        }
                                                                                        """);
                                                })

                                                .accessDeniedHandler(
                                                                (solicitud, respuesta, excepcion) -> {
                                                                        respuesta.setStatus(403);
                                                                        respuesta.setContentType(
                                                                                        "application/json");
                                                                        respuesta.getWriter().write(
                                                                                        """
                                                                                                                {
                                                                                                                    "mensaje" : "No tienes permisos para acceder a este recurso"}
                                                                                                        """);
                                                                })

                                );
                return http.build();
        }

        @Bean
        public CorsConfigurationSource fuenteDeConfiguracionSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(
                                List.of(
                                                "http://localhost:5173",
                                                "https://ecorruta.com.mx"));

                configuration.setAllowedMethods(
                                List.of(
                                                "GET",
                                                "POST",
                                                "PUT",
                                                "PATCH",
                                                "DELETE",
                                                "OPTIONS"));

                configuration.setAllowedHeaders(
                                List.of("*"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();

                fuente.registerCorsConfiguration("/**", configuration);
                return fuente;
        }

        @Bean
        public PasswordEncoder codifiEficadorDeContraseña() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager administradorAuthenticacion(AuthenticationConfiguration configuracion)
                        throws Exception {
                return configuracion.getAuthenticationManager();
        }

}
