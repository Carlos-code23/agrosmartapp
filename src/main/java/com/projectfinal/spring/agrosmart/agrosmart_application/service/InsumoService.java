package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Insumo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.InsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InsumoService {

    private final InsumoRepository insumoRepository;

    public InsumoService(InsumoRepository insumoRepository) {
        this.insumoRepository = insumoRepository;
    }

    public Insumo saveInsumo(Insumo insumo) {
        // Este método save() de JpaRepository maneja tanto la creación (id nulo)
        // como la actualización (id existente).
        return insumoRepository.save(insumo);
    }

    @Transactional(readOnly = true)
    public Optional<Insumo> getInsumoById(Long id) {
        return insumoRepository.findById(id);
    }

    // Nuevo método: Obtener insumo por ID y usuario (para seguridad)
    @Transactional(readOnly = true)
    public Optional<Insumo> getInsumoByIdAndUsuario(Long id, Usuario usuario) {
        return insumoRepository.findByIdAndUsuario(id, usuario);
    }

    @Transactional(readOnly = true)
    public List<Insumo> getAllInsumos() {
        return insumoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Insumo> getInsumoByNombre(String nombre) {
        return insumoRepository.findByNombre(nombre);
    }

    public Insumo updateInsumo(Long id, Insumo insumoDetails) {
        // Se recomienda usar findByIdAndUsuario aquí también para asegurar el permiso
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + id));

        // Actualizar todos los campos necesarios.
        // Asegúrate de que el usuario no se sobrescriba si no lo envías en insumoDetails
        // insumo.setUsuario(insumoDetails.getUsuario()); // <-- esto no debería ser necesario si el usuario ya está configurado
        insumo.setNombre(insumoDetails.getNombre());
        insumo.setTipo(insumoDetails.getTipo()); // <--- ¡Correcto!
        insumo.setProveedor(insumoDetails.getProveedor()); // <--- Añadir este si no estaba
        insumo.setUnidadMedida(insumoDetails.getUnidadMedida());
        insumo.setPrecioUnitario(insumoDetails.getPrecioUnitario());
        insumo.setDescripcion(insumoDetails.getDescripcion());

        return insumoRepository.save(insumo);
    }

    // Método para encontrar todos los insumos de un usuario específico
    // Este método es muy útil y ya lo estás usando en el controlador.
    @Transactional(readOnly = true) // Añadir readOnly = true
    public List<Insumo> findByUsuario(Usuario usuario) {
        return insumoRepository.findByUsuario(usuario);
    }

    // Método para encontrar un insumo por su ID (retorna Optional<Insumo>)
    // Duplicado con getInsumoById, puedes elegir cual usar
    @Transactional(readOnly = true) // Añadir readOnly = true
    public Optional<Insumo> findById(Long id) {
        return insumoRepository.findById(id);
    }

    // Método para encontrar un insumo por ID y usuario (más seguro para la verificación)
    // ¡Este es el que recomiendo usar en el controlador para seguridad!
    @Transactional(readOnly = true) // Añadir readOnly = true
    public Optional<Insumo> findByIdAndUsuario(Long id, Usuario usuario) {
        return insumoRepository.findByIdAndUsuario(id, usuario);
    }

    // Nuevo método: Eliminar insumo por ID y usuario (para seguridad)
    @Transactional
    public void deleteInsumoByIdAndUsuario(Long id, Usuario usuario) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado con ID: " + id));
        
        if (!insumo.getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("No tiene permiso para eliminar este insumo.");
        }
        insumoRepository.delete(insumo);
    }

    public void deleteInsumo(Long id) {
        insumoRepository.deleteById(id);
    }
}