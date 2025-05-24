package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Para manejo de transacciones
import java.util.List;
import java.util.Optional;

@Service // Marca esta clase como un servicio de Spring
@Transactional // Todos los métodos de esta clase serán transaccionales por defecto
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // Inyección de dependencias por constructor (forma recomendada)
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Guarda un nuevo usuario en la base de datos.
     * Aquí se podría añadir lógica de negocio como la encriptación de la contraseña.
     * @param usuario El objeto Usuario a guardar.
     * @return El usuario guardado.
     */
    public Usuario saveUsuario(Usuario usuario) {
        // TODO: En una fase posterior, aquí se debe encriptar la contraseña
        // usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id El ID del usuario.
     * @return Un Optional que contiene el usuario si se encuentra, o vacío si no.
     */
    @Transactional(readOnly = true) // Este método solo lee, puede ser más eficiente
    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Obtiene todos los usuarios.
     * @return Una lista de todos los usuarios.
     */
    @Transactional(readOnly = true)
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Actualiza un usuario existente.
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
        // TODO: Considerar si la contraseña se actualiza aquí o en un método separado
        // usuario.setPassword(usuarioDetails.getPassword()); // Si no se encripta de nuevo, cuidado!

        return usuarioRepository.save(usuario);
    }

    /**
     * Elimina un usuario por su ID.
     * @param id El ID del usuario a eliminar.
     */
    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    /**
     * Busca un usuario por su dirección de email.
     * @param email La dirección de email a buscar.
     * @return Un Optional que contiene el usuario si se encuentra, o vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}