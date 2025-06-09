package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections; // Para roles, si no tienes roles definidos aún

@Service // Marca esta clase como un servicio de Spring
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository; 

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carga los detalles del usuario por su nombre de usuario (en este caso, el email).
     * @param email El email del usuario que intenta autenticarse.
     * @return Un objeto UserDetails que representa al usuario.
     * @throws UsernameNotFoundException Si el usuario no es encontrado.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Construir el objeto UserDetails que Spring Security necesita.
        // por ahora un  rol básico. Más adelante en un mayor avance del proyecto, se pueden implementar roles dinámicos.
        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // Asigna un rol por defecto
        );
    }
}
