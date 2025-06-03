package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.InsumoPlaneacion;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Insumo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario; // Necesario para verificaciones de seguridad
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.InsumoPlaneacionRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.InsumoRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.PlaneacionCultivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.math.RoundingMode; // Para redondear BigDecimal

@Service
@Transactional // Asegura que las operaciones sean atómicas
public class InsumoPlaneacionService {

    private final InsumoPlaneacionRepository insumoPlaneacionRepository;
    private final PlaneacionCultivoRepository planeacionCultivoRepository;
    private final InsumoRepository insumoRepository;

    public InsumoPlaneacionService(InsumoPlaneacionRepository insumoPlaneacionRepository,
                                   PlaneacionCultivoRepository planeacionCultivoRepository,
                                   InsumoRepository insumoRepository) {
        this.insumoPlaneacionRepository = insumoPlaneacionRepository;
        this.planeacionCultivoRepository = planeacionCultivoRepository;
        this.insumoRepository = insumoRepository;
    }

    /**
     * Guarda un insumo en una planeación de cultivo (crea o actualiza),
     * calculando el costo total y actualizando la estimación de costo de la planeación.
     * Incluye validaciones de seguridad basadas en el usuario.
     * @param insumoPlaneacion El objeto InsumoPlaneacion a guardar/actualizar.
     * @param currentUser El usuario autenticado para verificaciones de seguridad.
     * @return El insumoPlaneacion guardado.
     */
    public InsumoPlaneacion saveInsumoPlaneacion(InsumoPlaneacion insumoPlaneacion, Usuario currentUser) {
        // 1. Obtener y validar las relaciones completas (PlaneacionCultivo e Insumo)
        // Se carga la planeación para asegurar que existe y está gestionada por JPA
        PlaneacionCultivo planeacion = planeacionCultivoRepository.findById(insumoPlaneacion.getPlaneacion().getId())
                .orElseThrow(() -> new IllegalArgumentException("La Planeación de Cultivo no fue encontrada."));
        // Se carga el insumo para asegurar que existe y está gestionado por JPA
        Insumo insumo = insumoRepository.findById(insumoPlaneacion.getInsumo().getId())
                .orElseThrow(() -> new IllegalArgumentException("El Insumo no fue encontrado."));

        // **Verificaciones de seguridad cruciales**:
        // Asegurar que la planeación pertenece al usuario actual
        if (!planeacion.getUsuario().getId().equals(currentUser.getId())) {
            throw new SecurityException("No tiene permiso para modificar insumos en esta planeación.");
        }
        // Asegurar que el insumo pertenece al usuario actual
        if (!insumo.getUsuario().getId().equals(currentUser.getId())) {
            throw new SecurityException("No tiene permiso para usar este insumo.");
        }

        // Asignar los objetos completos de Planeacion e Insumo al InsumoPlaneacion
        insumoPlaneacion.setPlaneacion(planeacion);
        insumoPlaneacion.setInsumo(insumo);

        // 2. CALCULAR totalInsumo: precioUnitario * cantidad
        BigDecimal cantidad = insumoPlaneacion.getCantidad();
        BigDecimal precioUnitario = insumo.getPrecioUnitario();

        // Validaciones adicionales para los valores numéricos
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad del insumo debe ser un valor numérico positivo.");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
             throw new IllegalArgumentException("El precio unitario del insumo no es válido.");
        }

        // Multiplicar y redondear a 2 decimales para el valor monetario
        BigDecimal totalInsumoCalculado = cantidad.multiply(precioUnitario)
                                                        .setScale(2, RoundingMode.HALF_UP); // Redondeo para moneda
        insumoPlaneacion.setTotalInsumo(totalInsumoCalculado);

        // 3. Guardar el InsumoPlaneacion en la base de datos
        InsumoPlaneacion savedInsumoPlaneacion = insumoPlaneacionRepository.save(insumoPlaneacion);

        // 4. Recalcular y actualizar estimacionCosto en PlaneacionCultivo
        recalculateEstimacionCosto(planeacion); // Llama al método de ayuda
        
        return savedInsumoPlaneacion;
    }

    @Transactional(readOnly = true)
    public Optional<InsumoPlaneacion> getInsumoPlaneacionById(Long id) {
        return insumoPlaneacionRepository.findById(id);
    }

    /**
     * Obtiene la lista de InsumoPlaneacion para una PlaneacionCultivo específica,
     * verificando que la planeación pertenezca al usuario autenticado.
     * @param planeacionId El ID de la planeación.
     * @param currentUser El usuario autenticado.
     * @return Una lista de InsumoPlaneacion.
     * @throws IllegalArgumentException si la planeación no se encuentra.
     * @throws SecurityException si el usuario no tiene permiso.
     */
    @Transactional(readOnly = true)
    public List<InsumoPlaneacion> getInsumosByPlaneacionIdAndUser(Long planeacionId, Usuario currentUser) {
        PlaneacionCultivo planeacion = planeacionCultivoRepository.findById(planeacionId)
                .orElseThrow(() -> new IllegalArgumentException("Planeación no encontrada."));
        
        // Verificación de seguridad
        if (!planeacion.getUsuario().getId().equals(currentUser.getId())) {
            throw new SecurityException("No tiene permiso para ver los insumos de esta planeación.");
        }
        // Utiliza el método del repositorio que busca por la entidad PlaneacionCultivo
        return insumoPlaneacionRepository.findByPlaneacion(planeacion);
    }

    /**
     * Busca un InsumoPlaneacion por su ID, asegurando que pertenezca a una PlaneacionCultivo
     * que es propiedad del usuario actual. Esto es crucial para operaciones de edición/eliminación.
     * @param insumoPlaneacionId ID del InsumoPlaneacion.
     * @param currentUser El usuario autenticado.
     * @return Un Optional que contiene el InsumoPlaneacion si se encuentra y está autorizado.
     */
    @Transactional(readOnly = true)
    public Optional<InsumoPlaneacion> findByIdAndPlaneacionOwnedByUser(Long insumoPlaneacionId, Usuario currentUser) {
        return insumoPlaneacionRepository.findById(insumoPlaneacionId)
                // Filtra el resultado para asegurar que la planeación asociada pertenece al usuario actual
                .filter(ip -> ip.getPlaneacion().getUsuario().getId().equals(currentUser.getId()));
    }

    /**
     * Elimina un InsumoPlaneacion por su ID, con verificación de permisos de usuario,
     * y luego recalcula la estimación de costo de la PlaneacionCultivo asociada.
     * @param id El ID del InsumoPlaneacion a eliminar.
     * @param currentUser El usuario autenticado.
     * @throws IllegalArgumentException si el InsumoPlaneacion no se encuentra.
     * @throws SecurityException si el usuario no tiene permiso para eliminarlo.
     */
    public void deleteInsumoPlaneacion(Long id, Usuario currentUser) {
        InsumoPlaneacion insumoPlaneacion = insumoPlaneacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("InsumoPlaneacion no encontrada."));

        // Verificación de seguridad: Asegurar que el insumo planeado pertenece a una planeación del usuario actual
        if (!insumoPlaneacion.getPlaneacion().getUsuario().getId().equals(currentUser.getId())) {
            throw new SecurityException("No tiene permiso para eliminar este insumo de la planeación.");
        }

        // Obtener la referencia a la PlaneacionCultivo antes de eliminar el InsumoPlaneacion.
        // Esto es crucial porque después de la eliminación, 'insumoPlaneacion.getPlaneacion()' podría ser nulo o un objeto desasociado.
        PlaneacionCultivo associatedPlaneacion = insumoPlaneacion.getPlaneacion();

        insumoPlaneacionRepository.deleteById(id);

        // Recalcular estimacionCosto para la PlaneacionCultivo asociada después de la eliminación
        recalculateEstimacionCosto(associatedPlaneacion);
    }

    /**
     * Método privado de ayuda para recalcular el estimacionCosto de una PlaneacionCultivo.
     * Suma todos los 'totalInsumo' de sus InsumoPlaneacion asociados.
     * @param planeacion La PlaneacionCultivo para la que se recalculará el costo.
     */
    private void recalculateEstimacionCosto(PlaneacionCultivo planeacion) {
        // Volver a cargar la planeación desde la DB para asegurar que esté actualizada y gestionada
        // Esto es importante si el 'planeacion' que se pasa no está en el contexto de persistencia actual
        PlaneacionCultivo updatedPlaneacion = planeacionCultivoRepository.findById(planeacion.getId())
                                            .orElseThrow(() -> new IllegalStateException("La Planeación no fue encontrada al intentar recalcular su costo."));

        // Obtener todos los InsumoPlaneacion actualmente asociados a esta planeación.
        // Se usa findByPlaneacion para asegurarse de obtener la lista más reciente.
        List<InsumoPlaneacion> insumosAsociados = insumoPlaneacionRepository.findByPlaneacion(updatedPlaneacion);

        // Sumar los valores de 'totalInsumo'
        BigDecimal totalEstimacionCosto = BigDecimal.ZERO;
        for (InsumoPlaneacion ip : insumosAsociados) {
            if (ip.getTotalInsumo() != null) { // Asegurarse de que el campo no sea nulo antes de sumar
                totalEstimacionCosto = totalEstimacionCosto.add(ip.getTotalInsumo());
            }
        }
        
        // Actualizar el campo 'estimacionCosto' de la PlaneacionCultivo y guardarla
        updatedPlaneacion.setEstimacionCosto(totalEstimacionCosto.setScale(2, RoundingMode.HALF_UP));
        planeacionCultivoRepository.save(updatedPlaneacion);
    }
}