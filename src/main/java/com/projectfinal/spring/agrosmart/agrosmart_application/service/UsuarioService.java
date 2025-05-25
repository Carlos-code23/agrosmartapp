package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // Importar PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // Inyectar PasswordEncoder

    // Modificar el constructor para inyectar PasswordEncoder
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Guarda un nuevo usuario en la base de datos.
     * Encripta la contraseña antes de guardar.
     * @param usuario El objeto Usuario a guardar.
     * @return El usuario guardado.
     */
    public Usuario saveUsuario(Usuario usuario) {
        // Encriptar la contraseña antes de guardarla
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    // ... (Métodos getUsuarioById, getAllUsuarios, deleteUsuario, findByEmail son los mismos)

    /**
     * Actualiza un usuario existente.
     * NOTA: Para la contraseña, es mejor un método separado para cambiarla.
     * @param id El ID del usuario a actualizar.
     * @param usuarioDetails Los detalles actualizados del usuario.
     * @return El usuario actualizado.
     * @throws RuntimeException si el usuario no es encontrado.
     */
    public Usuario updateUsuario(Long id, Usuario usuarioDetails) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setNombre(usuarioDetails.getNombre());
        usuario.setEmail(usuarioDetails.getEmail());
        // No se debe actualizar la contraseña aquí si no es un cambio de contraseña explícito.
        // Si se actualizara aquí, la contraseña que llega en usuarioDetails ya debería estar encriptada,
        // o se debería re-encriptar si viene en texto plano (lo cual no es buena práctica para updates).
        // Mejor un método dedicado para cambiar contraseña.

        return usuarioRepository.save(usuario);
    }

    // Nuevo método para actualizar solo la contraseña de un usuario
    public void updatePassword(Long userId, String newPassword) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}