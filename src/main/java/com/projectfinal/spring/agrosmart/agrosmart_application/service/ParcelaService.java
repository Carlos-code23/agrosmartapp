package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Parcela;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.ParcelaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ParcelaService {

    private final ParcelaRepository parcelaRepository;
    // Podrías inyectar UsuarioRepository si necesitas validar que el usuario exista
    // private final UsuarioRepository usuarioRepository;

    public ParcelaService(ParcelaRepository parcelaRepository) {
        this.parcelaRepository = parcelaRepository;
        // this.usuarioRepository = usuarioRepository;
    }

    /**
     * Guarda una nueva parcela en la base de datos.
     * @param parcela El objeto Parcela a guardar.
     * @return La parcela guardada.
     */
    public Parcela saveParcela(Parcela parcela) {
        // Aquí podrías añadir validaciones de negocio, por ejemplo:
        // - Que el usuario asociado a la parcela exista.
        // if (parcela.getUsuario() != null && parcela.getUsuario().getId() != null) {
        //     usuarioRepository.findById(parcela.getUsuario().getId())
        //             .orElseThrow(() -> new RuntimeException("Usuario asociado a la parcela no encontrado."));
        // }
        return parcelaRepository.save(parcela);
    }

    /**
     * Obtiene una parcela por su ID.
     * @param id El ID de la parcela.
     * @return Un Optional que contiene la parcela si se encuentra, o vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Parcela> getParcelaById(Long id) {
        return parcelaRepository.findById(id);
    }

    /**
     * Obtiene todas las parcelas.
     * @return Una lista de todas las parcelas.
     */
    @Transactional(readOnly = true)
    public List<Parcela> getAllParcelas() {
        return parcelaRepository.findAll();
    }

    /**
     * Obtiene las parcelas de un usuario específico.
     * @param usuarioId El ID del usuario.
     * @return Una lista de parcelas del usuario.
     */
    @Transactional(readOnly = true)
    public List<Parcela> getParcelasByUsuarioId(Long usuarioId) {
        return parcelaRepository.findByUsuarioId(usuarioId);
    }


    /**
     * Actualiza una parcela existente.
     * @param id El ID de la parcela a actualizar.
     * @param parcelaDetails Los detalles actualizados de la parcela.
     * @return La parcela actualizada.
     * @throws RuntimeException si la parcela no es encontrada.
     */
    public Parcela updateParcela(Long id, Parcela parcelaDetails) {
        Parcela parcela = parcelaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parcela no encontrada con ID: " + id));

        parcela.setNombre(parcelaDetails.getNombre());
        parcela.setUbicacion(parcelaDetails.getUbicacion());
        parcela.setTamano(parcelaDetails.getTamano());
        parcela.setUnidadMedida(parcelaDetails.getUnidadMedida());
        parcela.setDescripcion(parcelaDetails.getDescripcion());
        // No actualizamos usuario_id directamente aquí, si quieres cambiar el dueño, sería otro método.

        return parcelaRepository.save(parcela);
    }

    /**
     * Elimina una parcela por su ID.
     * @param id El ID de la parcela a eliminar.
     */
    public void deleteParcela(Long id) {
        parcelaRepository.deleteById(id);
    }
}