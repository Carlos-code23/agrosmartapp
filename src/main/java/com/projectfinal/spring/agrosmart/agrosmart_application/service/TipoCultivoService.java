package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.TipoCultivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TipoCultivoService {

    private final TipoCultivoRepository tipoCultivoRepository;

    public TipoCultivoService(TipoCultivoRepository tipoCultivoRepository) {
        this.tipoCultivoRepository = tipoCultivoRepository;
    }

    /**
     * Guarda un nuevo tipo de cultivo.
     * @param tipoCultivo El objeto TipoCultivo a guardar.
     * @return El tipo de cultivo guardado.
     */
    public TipoCultivo saveTipoCultivo(TipoCultivo tipoCultivo) {
        // Podrías añadir validación aquí para que el nombre sea único,
        // aunque la restricción 'unique=true' en la entidad ya lo maneja a nivel de DB.
        return tipoCultivoRepository.save(tipoCultivo);
    }

    /**
     * Obtiene un tipo de cultivo por su ID.
     * @param id El ID del tipo de cultivo.
     * @return Un Optional que contiene el tipo de cultivo si se encuentra, o vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<TipoCultivo> getTipoCultivoById(Long id) {
        return tipoCultivoRepository.findById(id);
    }

    /**
     * Obtiene todos los tipos de cultivo.
     * @return Una lista de todos los tipos de cultivo.
     */
    @Transactional(readOnly = true)
    public List<TipoCultivo> getAllTiposCultivo() {
        return tipoCultivoRepository.findAll();
    }

    /**
     * Obtiene un tipo de cultivo por su nombre.
     * @param nombre El nombre del tipo de cultivo.
     * @return Un Optional que contiene el tipo de cultivo si se encuentra, o vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<TipoCultivo> getTipoCultivoByNombre(String nombre) {
        return tipoCultivoRepository.findByNombre(nombre);
    }

    /**
     * Actualiza un tipo de cultivo existente.
     * @param id El ID del tipo de cultivo a actualizar.
     * @param tipoCultivoDetails Los detalles actualizados del tipo de cultivo.
     * @return El tipo de cultivo actualizado.
     * @throws RuntimeException si el tipo de cultivo no es encontrado.
     */
    public TipoCultivo updateTipoCultivo(Long id, TipoCultivo tipoCultivoDetails) {
        TipoCultivo tipoCultivo = tipoCultivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de cultivo no encontrado con ID: " + id));

        tipoCultivo.setNombre(tipoCultivoDetails.getNombre());
        tipoCultivo.setDescripcion(tipoCultivoDetails.getDescripcion());
        tipoCultivo.setDensidadSiembraRecomendadaPorHa(tipoCultivoDetails.getDensidadSiembraRecomendadaPorHa());
        tipoCultivo.setDuracionDiasEstimada(tipoCultivoDetails.getDuracionDiasEstimada());
        tipoCultivo.setDistanciaSurco(tipoCultivoDetails.getDistanciaSurco()); // Nuevo campo
        tipoCultivo.setDistanciaPlanta(tipoCultivoDetails.getDistanciaPlanta()); // Nuevo campo

        return tipoCultivoRepository.save(tipoCultivo);
    }

    /**
     * Elimina un tipo de cultivo por su ID.
     * @param id El ID del tipo de cultivo a eliminar.
     */
    public void deleteTipoCultivo(Long id) {
        tipoCultivoRepository.deleteById(id);
    }
}