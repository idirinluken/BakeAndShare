package com.BakeAndShare.app.config;

import com.BakeAndShare.app.service.CustomUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;


@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // Configuración de las rutas y permisos
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/usuarios/nuevo", "/css/**").permitAll()  // Permitir acceso al login y registro
                .requestMatchers("/admin/**").hasRole("ADMIN")  // Solo ADMIN puede acceder a /admin/**
                .requestMatchers("/user/**").hasRole("USER")  // Solo USER puede acceder a /user/**
                .anyRequest().authenticated()  // Todo lo demás requiere autenticación
            )
            .formLogin(form -> form
                .loginPage("/")  // Página de login personalizada
                .loginProcessingUrl("/login")  // Procesar el login
                .defaultSuccessUrl("/home", true)  // Redirigir después de login exitoso
                .failureUrl("/?error")  // Redirigir si las credenciales son incorrectas
                .permitAll()  // Permitir acceso a la página de login sin autenticación
            )
            .sessionManagement()
            .invalidSessionUrl("/login")  // Redirige a login si la sesión es inválida
            .maximumSessions(1)  // Limitar el número de sesiones por usuario
            .expiredUrl("/login");  // Redirige a login si la sesión expira


        return http.build();
    }

    // Método para configurar la autenticación usando el UserDetailsService
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(customUserDetailsService)  // Usar CustomUserDetailsService
                .passwordEncoder(passwordEncoder())  // Usar BCryptPasswordEncoder
                .and()
                .build();
    }

    // Configuración de PasswordEncoder (cifrado de contraseñas)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
