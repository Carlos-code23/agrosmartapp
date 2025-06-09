package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Parcela;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario; // ¡Importa Usuario!
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.ParcelaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ParcelaService {

    private final ParcelaRepository parcelaRepository;

    public ParcelaService(ParcelaRepository parcelaRepository) {
        this.parcelaRepository = parcelaRepository;
    }

    public Parcela saveParcela(Parcela parcela) {
        return parcelaRepository.save(parcela);
    }

    @Transactional(readOnly = true)
    public Optional<Parcela> getParcelaById(Long id) {
        return parcelaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Parcela> getAllParcelas() {
        return parcelaRepository.findAll();
    }

    /**
     * Obtiene las parcelas de un usuario específico.
     * @param usuario El objeto Usuario.
     * @return Una lista de parcelas del usuario.
     */
    @Transactional(readOnly = true)
    public List<Parcela> findByUsuario(Usuario usuario) { // ¡Cambiado! Ahora recibe un objeto Usuario
        return parcelaRepository.findByUsuario(usuario);
    }

    /**
     * Obtiene una parcela por su ID y el usuario al que pertenece.
     * Es crucial para la seguridad, asegurando que un usuario solo acceda a sus parcelas.
     * @param id El ID de la parcela.
     * @param usuario El usuario al que debe pertenecer la parcela.
     * @return Un Optional que contiene la parcela si se encuentra y pertenece al usuario, o vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Parcela> getParcelaByIdAndUsuario(Long id, Usuario usuario) {
        return parcelaRepository.findByIdAndUsuario(id, usuario);
    }

    public Parcela updateParcela(Long id, Parcela parcelaDetails) {
        // En una refactorización previa, sugerí consolidar save/update.
        // Si mantienes un update separado, asegúrate de que el usuario se setee.
        Parcela parcela = parcelaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parcela no encontrada con ID: " + id));

        parcela.setNombre(parcelaDetails.getNombre());
        parcela.setUbicacion(parcelaDetails.getUbicacion());
        parcela.setTamano(parcelaDetails.getTamano());
        parcela.setUnidadMedida(parcelaDetails.getUnidadMedida());
        parcela.setDescripcion(parcelaDetails.getDescripcion());
        // El usuario ya debería estar seteado en 'parcela' y no debería cambiarse aquí.

        return parcelaRepository.save(parcela);
    }

    /**
     * Elimina una parcela por su ID, verificando que pertenece al usuario.
     * @param id El ID de la parcela a eliminar.
     * @param currentUser El usuario que intenta eliminar la parcela.
     * @throws IllegalArgumentException si la parcela no es encontrada.
     * @throws SecurityException si el usuario no tiene permiso para eliminar la parcela.
     */
    public void deleteParcela(Long id, Usuario currentUser) {
        Parcela parcela = parcelaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parcela no encontrada con ID: " + id));

        if (!parcela.getUsuario().getId().equals(currentUser.getId())) {
            throw new SecurityException("No tiene permiso para eliminar esta parcela.");
        }
        parcelaRepository.deleteById(id);
    }
}