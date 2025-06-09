package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo; 
import com.projectfinal.spring.agrosmart.agrosmart_application.model.InsumoPlaneacion; 
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Parcela;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario; 
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.ParcelaRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.PlaneacionCultivoRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.TipoCultivoRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.EtapaCultivoRepository; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlaneacionCultivoService {

    private final PlaneacionCultivoRepository planeacionCultivoRepository;
    private final ParcelaRepository parcelaRepository;
    private final TipoCultivoRepository tipoCultivoRepository;
    private final EtapaCultivoRepository etapaCultivoRepository; 

    public PlaneacionCultivoService(PlaneacionCultivoRepository planeacionCultivoRepository,
                                    ParcelaRepository parcelaRepository,
                                    TipoCultivoRepository tipoCultivoRepository,
                                    EtapaCultivoRepository etapaCultivoRepository) { 
        this.planeacionCultivoRepository = planeacionCultivoRepository;
        this.parcelaRepository = parcelaRepository;
        this.tipoCultivoRepository = tipoCultivoRepository;
        this.etapaCultivoRepository = etapaCultivoRepository; 
    }

    /**
     * Guarda una nueva planeación de cultivo o actualiza una existente.
     * @param planeacionCultivo El objeto PlaneacionCultivo a guardar (con Parcela, TipoCultivo y EtapaCultivo asociados, y Usuario).
     * @return La planeación de cultivo guardada.
     */
    public PlaneacionCultivo savePlaneacionCultivo(PlaneacionCultivo planeacionCultivo) {
        if (planeacionCultivo.getParcela() != null && planeacionCultivo.getParcela().getId() != null) {
            Parcela parcela = parcelaRepository.findById(planeacionCultivo.getParcela().getId())
                                            .orElseThrow(() -> new RuntimeException("Parcela no encontrada con ID: " + planeacionCultivo.getParcela().getId()));
            planeacionCultivo.setParcela(parcela);
        }

        if (planeacionCultivo.getTipoCultivo() != null && planeacionCultivo.getTipoCultivo().getId() != null) {
            TipoCultivo tipoCultivo = tipoCultivoRepository.findById(planeacionCultivo.getTipoCultivo().getId())
                                                        .orElseThrow(() -> new RuntimeException("Tipo de cultivo no encontrado con ID: " + planeacionCultivo.getTipoCultivo().getId()));
            planeacionCultivo.setTipoCultivo(tipoCultivo);
        }

        // Manejo de la etapa de cultivo
        if (planeacionCultivo.getEtapaCultivo() != null && planeacionCultivo.getEtapaCultivo().getId() != null) {
            EtapaCultivo etapaCultivo = etapaCultivoRepository.findById(planeacionCultivo.getEtapaCultivo().getId())
                                                            .orElseThrow(() -> new RuntimeException("Etapa de cultivo no encontrada con ID: " + planeacionCultivo.getEtapaCultivo().getId()));
            planeacionCultivo.setEtapaCultivo(etapaCultivo);
        } else {
             throw new IllegalArgumentException("Debe seleccionar una etapa de cultivo.");
        }


        if (planeacionCultivo.getParcela() != null && planeacionCultivo.getTipoCultivo() != null) {
            BigDecimal areaEnM2 = convertAreaToM2(planeacionCultivo.getParcela().getTamano(), planeacionCultivo.getParcela().getUnidadMedida());
            BigDecimal distanciaSurco = planeacionCultivo.getTipoCultivo().getDistanciaSurco();
            BigDecimal distanciaPlanta = planeacionCultivo.getTipoCultivo().getDistanciaPlanta();

            if (distanciaSurco == null || distanciaPlanta == null ||
                distanciaSurco.compareTo(BigDecimal.ZERO) == 0 ||
                distanciaPlanta.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("Las distancias de surco y planta deben estar definidas y ser mayores que cero para el tipo de cultivo seleccionado: " + planeacionCultivo.getTipoCultivo().getNombre());
            }

            BigDecimal espacioPorPlanta = distanciaSurco.multiply(distanciaPlanta);
            BigDecimal numeroSemillasCalculado = areaEnM2.divide(espacioPorPlanta, 0, RoundingMode.HALF_UP);
            planeacionCultivo.setNumeroSemillas(numeroSemillasCalculado); // Convertir a Double
        }

        return planeacionCultivoRepository.save(planeacionCultivo);
    }

    @Transactional(readOnly = true)
    public Optional<PlaneacionCultivo> getPlaneacionCultivoById(Long id) {
        return planeacionCultivoRepository.findById(id);
    }

    /**
     * Obtiene una planeación de cultivo por su ID y el usuario al que pertenece.
     * @param id El ID de la planeación.
     * @param usuario El usuario al que debe pertenecer la planeación.
     * @return Un Optional que contiene la planeación si se encuentra y pertenece al usuario, o vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<PlaneacionCultivo> getPlaneacionCultivoByIdAndUsuario(Long id, Usuario usuario) {
        return planeacionCultivoRepository.findByIdAndUsuario(id, usuario);
    }

    @Transactional(readOnly = true)
    public List<PlaneacionCultivo> getAllPlaneacionesCultivo() {
        return planeacionCultivoRepository.findAll();
    }

    /**
     * Obtiene las planeaciones de cultivo de un usuario específico.
     * @param usuario El objeto Usuario.
     * @return Una lista de planeaciones de cultivo del usuario.
     */
    @Transactional(readOnly = true)
    public List<PlaneacionCultivo> findByUsuario(Usuario usuario) { // ¡Cambiado! Ahora recibe un objeto Usuario
        return planeacionCultivoRepository.findByUsuario(usuario);
    }

    /**
     * Elimina una planeación de cultivo por su ID, verificando que pertenece al usuario.
     * @param id El ID de la planeación a eliminar.
     * @param currentUser El usuario que intenta eliminar la planeación.
     * @throws IllegalArgumentException si la planeación no es encontrada.
     * @throws SecurityException si el usuario no tiene permiso para eliminar la planeación.
     */
    public void deletePlaneacionCultivo(Long id, Usuario currentUser) { // ¡Cambiado! Ahora recibe un objeto Usuario
        PlaneacionCultivo planeacion = planeacionCultivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planeación de cultivo no encontrada con ID: " + id));

        if (!planeacion.getUsuario().getId().equals(currentUser.getId())) {
            throw new SecurityException("No tiene permiso para eliminar esta planeación de cultivo.");
        }
        planeacionCultivoRepository.deleteById(id);
    }

    private BigDecimal convertAreaToM2(BigDecimal tamano, String unidadMedida) {
        if ("hectareas".equalsIgnoreCase(unidadMedida)) {
            return tamano.multiply(new BigDecimal("10000")); // 1 hectárea = 10,000 m2
        } else if ("m2".equalsIgnoreCase(unidadMedida)) {
            return tamano;
        } else {
            throw new IllegalArgumentException("Unidad de medida de parcela no soportada: " + unidadMedida);
        }
    }
}