package com.projectfinal.spring.agrosmart.agrosmart_application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Permite acceso sin autenticación a la página de bienvenida, registro y archivos estáticos
                .requestMatchers("/", "/index", "/auth/register", "/auth/login", "/css/**", "/js/**", "/img/**").permitAll()
                // Todas las demás solicitudes requieren autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login") // Especifica la URL de tu página de login
                .defaultSuccessUrl("/dashboard", true) // Redirige al dashboard después del login exitoso
                .failureUrl("/auth/login?error") // Redirige a la página de login con un parámetro de error si falla
                .permitAll() // Permite que todos accedan a la página de login
            )
            .logout(logout -> logout
                .logoutUrl("/logout") 
                .logoutSuccessUrl("/auth/login?logout") // Redirige después de cerrar sesión
                .invalidateHttpSession(true) // Invalida la sesión HTTP
                .clearAuthentication(true) // Limpia la autenticación
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/error/403")
            )
            .csrf(csrf -> csrf.disable()); // TODO: Para desarrollo, deshabilitar CSRF. HABILITAR en producción.

        return http.build();
    }
}