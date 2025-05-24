package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Parcela;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.ParcelaRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.PlaneacionCultivoRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.TipoCultivoRepository;
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

    public PlaneacionCultivoService(PlaneacionCultivoRepository planeacionCultivoRepository,
                                  ParcelaRepository parcelaRepository,
                                  TipoCultivoRepository tipoCultivoRepository) {
        this.planeacionCultivoRepository = planeacionCultivoRepository;
        this.parcelaRepository = parcelaRepository;
        this.tipoCultivoRepository = tipoCultivoRepository;
    }

    /**
     * Guarda una nueva planeación de cultivo, calculando la densidad de siembra.
     * @param planeacionCultivo El objeto PlaneacionCultivo a guardar (con Parcela y TipoCultivo asociados).
     * @return La planeación de cultivo guardada.
     */
    public PlaneacionCultivo savePlaneacionCultivo(PlaneacionCultivo planeacionCultivo) {
        // 1. Obtener los objetos completos de Parcela y TipoCultivo desde la BD
        // Esto es crucial porque el objeto "planeacionCultivo" que llega puede tener solo los IDs
        Parcela parcela = parcelaRepository.findById(planeacionCultivo.getParcela().getId())
                                        .orElseThrow(() -> new RuntimeException("Parcela no encontrada con ID: " + planeacionCultivo.getParcela().getId()));
        TipoCultivo tipoCultivo = tipoCultivoRepository.findById(planeacionCultivo.getTipoCultivo().getId())
                                                .orElseThrow(() -> new RuntimeException("Tipo de cultivo no encontrado con ID: " + planeacionCultivo.getTipoCultivo().getId()));

        // Asegurarse de que los objetos completos están asociados antes de guardar
        planeacionCultivo.setParcela(parcela);
        planeacionCultivo.setTipoCultivo(tipoCultivo);
        // planeacionCultivo.setUsuario(usuarioRepository.findById(planeacionCultivo.getUsuario().getId()).orElseThrow(...)); // Si el usuario no se setea directamente en el controlador

        // 2. Realizar el cálculo de la densidad de siembra
        BigDecimal areaEnM2 = convertAreaToM2(parcela.getTamano(), parcela.getUnidadMedida());
        BigDecimal distanciaSurco = tipoCultivo.getDistanciaSurco();
        BigDecimal distanciaPlanta = tipoCultivo.getDistanciaPlanta();

        if (distanciaSurco == null || distanciaPlanta == null ||
            distanciaSurco.compareTo(BigDecimal.ZERO) == 0 ||
            distanciaPlanta.compareTo(BigDecimal.ZERO) == 0) {
            // Podrías lanzar una excepción más específica o un mensaje de error al usuario
            throw new IllegalArgumentException("Las distancias de surco y planta deben estar definidas y ser mayores que cero para el tipo de cultivo seleccionado: " + tipoCultivo.getNombre());
        }

        BigDecimal espacioPorPlanta = distanciaSurco.multiply(distanciaPlanta);

        // La fórmula es areaTotal(m2) / (distanciaSurco * distanciaPlanta)
        // Usamos RoundingMode.HALF_UP para redondear al entero más cercano
        BigDecimal numeroSemillasCalculado = areaEnM2.divide(espacioPorPlanta, 0, RoundingMode.HALF_UP);

        planeacionCultivo.setNumeroSemillas(numeroSemillasCalculado);

        // 3. Guardar la planeación de cultivo
        return planeacionCultivoRepository.save(planeacionCultivo);
    }

    /**
     * Obtiene una planeación de cultivo por su ID.
     * @param id El ID de la planeación.
     * @return Un Optional que contiene la planeación si se encuentra, o vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<PlaneacionCultivo> getPlaneacionCultivoById(Long id) {
        return planeacionCultivoRepository.findById(id);
    }

    /**
     * Obtiene todas las planeaciones de cultivo.
     * @return Una lista de todas las planeaciones de cultivo.
     */
    @Transactional(readOnly = true)
    public List<PlaneacionCultivo> getAllPlaneacionesCultivo() {
        return planeacionCultivoRepository.findAll();
    }

    /**
     * Obtiene las planeaciones de cultivo de un usuario específico.
     * @param usuarioId El ID del usuario.
     * @return Una lista de planeaciones de cultivo del usuario.
     */
    @Transactional(readOnly = true)
    public List<PlaneacionCultivo> getPlaneacionesByUsuarioId(Long usuarioId) {
        return planeacionCultivoRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Actualiza una planeación de cultivo existente.
     * Se recalcula la densidad de siembra si cambian la parcela o el tipo de cultivo.
     * @param id El ID de la planeación a actualizar.
     * @param planeacionCultivoDetails Los detalles actualizados.
     * @return La planeación de cultivo actualizada.
     * @throws RuntimeException si la planeación no es encontrada.
     * @throws IllegalArgumentException si las distancias del tipo de cultivo son inválidas.
     */
    public PlaneacionCultivo updatePlaneacionCultivo(Long id, PlaneacionCultivo planeacionCultivoDetails) {
        PlaneacionCultivo existingPlaneacion = planeacionCultivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Planeación de cultivo no encontrada con ID: " + id));

        // Actualizar campos básicos
        existingPlaneacion.setFechaInicio(planeacionCultivoDetails.getFechaInicio());
        existingPlaneacion.setFechaFinEstimada(planeacionCultivoDetails.getFechaFinEstimada());
        existingPlaneacion.setEstimacionCosto(planeacionCultivoDetails.getEstimacionCosto());
        existingPlaneacion.setDescripcion(planeacionCultivoDetails.getDescripcion());
        existingPlaneacion.setEstado(planeacionCultivoDetails.getEstado());

        // Verificar si la Parcela o el TipoCultivo han cambiado para recalcular la densidad
        boolean needsRecalculation = false;
        if (!existingPlaneacion.getParcela().getId().equals(planeacionCultivoDetails.getParcela().getId())) {
            Parcela nuevaParcela = parcelaRepository.findById(planeacionCultivoDetails.getParcela().getId())
                    .orElseThrow(() -> new RuntimeException("Nueva Parcela no encontrada con ID: " + planeacionCultivoDetails.getParcela().getId()));
            existingPlaneacion.setParcela(nuevaParcela);
            needsRecalculation = true;
        }
        if (!existingPlaneacion.getTipoCultivo().getId().equals(planeacionCultivoDetails.getTipoCultivo().getId())) {
            TipoCultivo nuevoTipoCultivo = tipoCultivoRepository.findById(planeacionCultivoDetails.getTipoCultivo().getId())
                    .orElseThrow(() -> new RuntimeException("Nuevo Tipo de Cultivo no encontrado con ID: " + planeacionCultivoDetails.getTipoCultivo().getId()));
            existingPlaneacion.setTipoCultivo(nuevoTipoCultivo);
            needsRecalculation = true;
        }

        if (needsRecalculation) {
            BigDecimal areaEnM2 = convertAreaToM2(existingPlaneacion.getParcela().getTamano(), existingPlaneacion.getParcela().getUnidadMedida());
            BigDecimal distanciaSurco = existingPlaneacion.getTipoCultivo().getDistanciaSurco();
            BigDecimal distanciaPlanta = existingPlaneacion.getTipoCultivo().getDistanciaPlanta();

            if (distanciaSurco == null || distanciaPlanta == null ||
                distanciaSurco.compareTo(BigDecimal.ZERO) == 0 ||
                distanciaPlanta.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("Las distancias de surco y planta deben estar definidas y ser mayores que cero para el tipo de cultivo seleccionado.");
            }

            BigDecimal espacioPorPlanta = distanciaSurco.multiply(distanciaPlanta);
            BigDecimal numeroSemillasCalculado = areaEnM2.divide(espacioPorPlanta, 0, RoundingMode.HALF_UP);
            existingPlaneacion.setNumeroSemillas(numeroSemillasCalculado);
        }

        return planeacionCultivoRepository.save(existingPlaneacion);
    }


    /**
     * Elimina una planeación de cultivo por su ID.
     * @param id El ID de la planeación a eliminar.
     */
    public void deletePlaneacionCultivo(Long id) {
        planeacionCultivoRepository.deleteById(id);
    }

    /**
     * Método de utilidad para convertir el área de la parcela a metros cuadrados.
     * @param tamano El tamaño de la parcela.
     * @param unidadMedida La unidad de medida ('hectareas' o 'm2').
     * @return El tamaño en metros cuadrados.
     * @throws IllegalArgumentException si la unidad de medida no es soportada.
     */
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
