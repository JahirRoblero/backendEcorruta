
package com.backend.ecorruta.config;

import java.util.List;

import com.backend.ecorruta.security.FiltroJwt;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class ConfiguracionSeguridad {

        private final FiltroJwt filtroJwt;

        @Bean
        public SecurityFilterChain cadenaSeguridad(HttpSecurity http)
                        throws Exception {

                http
                                .csrf(configuracion -> configuracion.disable())

                                .cors(configuracion -> configuracion
                                                .configurationSource(fuenteDeConfiguracionSource()))

                                .sessionManagement(configuracion -> configuracion
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(autorizacion -> autorizacion

                                                .dispatcherTypeMatchers(DispatcherType.ERROR)
                                                .permitAll()

                                                .requestMatchers(HttpMethod.OPTIONS, "/**")
                                                .permitAll()

                                                .requestMatchers(HttpMethod.POST, "/api/auth/login",
                                                                "/api/auth/registro")
                                                .permitAll()

                                                .requestMatchers("/api/recuperacion")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/reportes/imagenes/**")
                                                .permitAll()

                                                .requestMatchers("/api/admin/**")
                                                .hasRole("ADMINISTRADOR")

                                                .anyRequest()
                                                .authenticated())

                                .exceptionHandling(configuracion -> configuracion

                                                .authenticationEntryPoint(
                                                                (solicitud, respuesta, excepcion) -> {
                                                                        respuesta.setStatus(401);
                                                                        respuesta.setContentType("application/json");
                                                                        respuesta.setCharacterEncoding("UTF-8");
                                                                        respuesta.getWriter().write(
                                                                                        """
                                                                                                        {
                                                                                                            "mensaje": "Debes iniciar sesión"
                                                                                                        }
                                                                                                        """);
                                                                })

                                                .accessDeniedHandler(
                                                                (solicitud, respuesta, excepcion) -> {
                                                                        respuesta.setStatus(403);
                                                                        respuesta.setContentType("application/json");
                                                                        respuesta.setCharacterEncoding("UTF-8");
                                                                        respuesta.getWriter().write(
                                                                                        """
                                                                                                        {
                                                                                                            "mensaje": "No tienes permisos para acceder a este recurso"
                                                                                                        }
                                                                                                        """);
                                                                }))

                                .addFilterBefore(
                                                filtroJwt,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public FilterRegistrationBean<FiltroJwt> registroFiltroJwt() {
                FilterRegistrationBean<FiltroJwt> registro = new FilterRegistrationBean<>(filtroJwt);

                registro.setEnabled(false);
                return registro;
        }

        @Bean
        public CorsConfigurationSource fuenteDeConfiguracionSource() {

                CorsConfiguration configuracion = new CorsConfiguration();

                configuracion.setAllowedOrigins(
                                List.of(
                                                "http://localhost:5173",
                                                "https://ecorruta.com.mx"));

                configuracion.setAllowedMethods(
                                List.of(
                                                "GET",
                                                "POST",
                                                "PUT",
                                                "PATCH",
                                                "DELETE",
                                                "OPTIONS"));

                configuracion.setAllowedHeaders(List.of("*"));
                configuracion.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();

                fuente.registerCorsConfiguration("/**", configuracion);

                return fuente;
        }

        @Bean
        public PasswordEncoder codificadorDeContrasena() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager administradorAuthenticacion(
                        AuthenticationConfiguration configuracion) throws Exception {
                return configuracion.getAuthenticationManager();
        }
}
