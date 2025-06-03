package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Insumo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.InsumoPlaneacion;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.InsumoPlaneacionService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.InsumoService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.PlaneacionCultivoService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
// Mapeo base para todas las operaciones de InsumoPlaneacion, anidado bajo PlaneacionCultivo
@RequestMapping("/planeaciones/{planeacionId}/insumos")
public class InsumoPlaneacionWebController {

    private final InsumoPlaneacionService insumoPlaneacionService;
    private final PlaneacionCultivoService planeacionCultivoService;
    private final InsumoService insumoService;
    private final UsuarioService usuarioService;

    public InsumoPlaneacionWebController(InsumoPlaneacionService insumoPlaneacionService,
                                        PlaneacionCultivoService planeacionCultivoService,
                                        InsumoService insumoService,
                                        UsuarioService usuarioService) {
        this.insumoPlaneacionService = insumoPlaneacionService;
        this.planeacionCultivoService = planeacionCultivoService;
        this.insumoService = insumoService;
        this.usuarioService = usuarioService;
    }

    // Método de ayuda para obtener el usuario autenticado
    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // El nombre de usuario (email)
        return usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en la base de datos."));
    }

    // Método de ayuda para obtener una PlaneacionCultivo y verificar si pertenece al usuario autenticado
    private PlaneacionCultivo getPlaneacionIfAuthorized(Long planeacionId) {
        Usuario currentUser = getAuthenticatedUser();
        return planeacionCultivoService.getPlaneacionCultivoById(planeacionId)
                // Filtra para asegurar que la planeación pertenece al usuario actual
                .filter(p -> p.getUsuario().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Planeación no encontrada o no autorizado para acceder."));
    }

    // --- LISTAR INSUMOS ASOCIADOS A UNA PLANEACIÓN ESPECÍFICA ---
    // GET /planeaciones/{planeacionId}/insumos
    @GetMapping
    public String listInsumosByPlaneacion(@PathVariable Long planeacionId, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Se valida y obtiene la planeación para asegurar que pertenece al usuario
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            
            // Se obtienen los insumos de planeación asociados a esta planeación y al usuario
            List<InsumoPlaneacion> insumosPlaneacion = insumoPlaneacionService.getInsumosByPlaneacionIdAndUser(planeacionId, getAuthenticatedUser());
            
            // Se vuelve a cargar la planeación para obtener el estimacionCosto más reciente
            planeacion = planeacionCultivoService.getPlaneacionCultivoByIdAndUsuario(planeacionId, getAuthenticatedUser())
                                                .orElseThrow(() -> new IllegalStateException("Error al cargar planeación para mostrar costo después de operación."));


            model.addAttribute("planeacion", planeacion); // Pasar la planeación (ahora actualizada con el costo)
            model.addAttribute("insumosPlaneacion", insumosPlaneacion);
            model.addAttribute("estimacionCostoPlaneacion", planeacion.getEstimacionCosto()); // Pasar el costo total estimado de la planeación
            return "insumos_planeacion/list-insumos-planeacion"; // Ruta de la vista
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) { // Añadido IllegalStateException
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones"; // Redirigir si no está autorizado o no encuentra la planeación
        }
    }

    // --- MOSTRAR FORMULARIO PARA AÑADIR UN INSUMO A UNA PLANEACIÓN ---
    // GET /planeaciones/{planeacionId}/insumos/new
    @GetMapping("/new")
    public String showAddInsumoForm(@PathVariable Long planeacionId, Model model, RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            InsumoPlaneacion insumoPlaneacion = new InsumoPlaneacion();
            insumoPlaneacion.setPlaneacion(planeacion); // Pre-seleccionar la planeación en el formulario
            
            model.addAttribute("planeacion", planeacion);
            model.addAttribute("insumoPlaneacion", insumoPlaneacion);
            // Solo mostrar insumos que pertenecen al usuario autenticado para la selección
            model.addAttribute("allInsumos", insumoService.findByUsuario(getAuthenticatedUser()));
            return "insumos_planeacion/insumo-planeacion-form"; // Ruta de la vista del formulario
        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones";
        }
    }

    // --- PROCESAR EL FORMULARIO (CREAR O ACTUALIZAR UN INSUMO EN UNA PLANEACIÓN) ---
    // POST /planeaciones/{planeacionId}/insumos (Para creación)
    // POST /planeaciones/{planeacionId}/insumos/edit/{insumoPlaneacionId} (Para actualización)
    // Este método maneja tanto la creación como la actualización, basándose en si insumoPlaneacion.getId() es nulo o no.
    @PostMapping({"", "/edit/{insumoPlaneacionId}"}) // Mapping para ambos casos
    public String saveOrUpdateInsumoPlaneacion(@PathVariable Long planeacionId,
                                               @PathVariable(required = false) Long insumoPlaneacionId, // Puede ser nulo para creación
                                               @Valid @ModelAttribute("insumoPlaneacion") InsumoPlaneacion insumoPlaneacion,
                                               BindingResult result,
                                               Model model,
                                               RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        PlaneacionCultivo planeacion = null;

        try {
            planeacion = getPlaneacionIfAuthorized(planeacionId);
            
            // Si es una actualización, asignar el ID del path variable al objeto.
            // Si es creación, insumoPlaneacion.getId() será nulo.
            if (insumoPlaneacionId != null) {
                insumoPlaneacion.setId(insumoPlaneacionId);
            }

            // Asegurarse de que el objeto PlaneacionCultivo completo está asociado
            insumoPlaneacion.setPlaneacion(planeacion);

            // Validar que se seleccionó un insumo y que pertenece al usuario
            if (insumoPlaneacion.getInsumo() == null || insumoPlaneacion.getInsumo().getId() == null) {
                result.rejectValue("insumo", "null.insumo", "Debe seleccionar un insumo.");
            } else {
                Optional<Insumo> selectedInsumoOpt = insumoService.getInsumoById(insumoPlaneacion.getInsumo().getId());
                if (selectedInsumoOpt.isEmpty() || !selectedInsumoOpt.get().getUsuario().getId().equals(currentUser.getId())) {
                    result.rejectValue("insumo", "invalid.insumo", "El insumo seleccionado no es válido o no le pertenece.");
                } else {
                    insumoPlaneacion.setInsumo(selectedInsumoOpt.get()); // Asignar el objeto Insumo completo
                }
            }
            
            // Si hay errores de validación, regresar al formulario
            if (result.hasErrors()) {
                model.addAttribute("planeacion", planeacion);
                model.addAttribute("allInsumos", insumoService.findByUsuario(currentUser));
                return "insumos_planeacion/insumo-planeacion-form";
            }

            String message;
            if (insumoPlaneacion.getId() == null) { // Caso de creación
                insumoPlaneacionService.saveInsumoPlaneacion(insumoPlaneacion, currentUser);
                message = "Insumo añadido a la planeación exitosamente!";
            } else { // Caso de actualización
                // Antes de actualizar, verificar que el InsumoPlaneacion existente es del usuario y de la planeación correcta
                Optional<InsumoPlaneacion> existingIp = insumoPlaneacionService.findByIdAndPlaneacionOwnedByUser(insumoPlaneacion.getId(), currentUser);
                if (existingIp.isEmpty()) {
                     redirectAttributes.addFlashAttribute("errorMessage", "Asignación de insumo no encontrada o no autorizada para actualizar.");
                     return "redirect:/planeaciones/" + planeacionId + "/insumos";
                }
                insumoPlaneacionService.saveInsumoPlaneacion(insumoPlaneacion, currentUser); // El método save maneja la actualización si el ID está presente
                message = "Insumo en planeación actualizado exitosamente!";
            }
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/planeaciones/" + planeacionId + "/insumos";

        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones"; // Redirigir a planeaciones si hay error de autorización o no se encuentra la planeación
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el insumo en la planeación: " + e.getMessage());
            // Si hay un error general, volver al formulario y recargar las listas de selección
            if (planeacion != null) {
                model.addAttribute("planeacion", planeacion);
            }
            model.addAttribute("allInsumos", insumoService.findByUsuario(currentUser));
            return "insumos_planeacion/insumo-planeacion-form";
        }
    }
    
    // --- MOSTRAR FORMULARIO PARA EDITAR UN INSUMO EN UNA PLANEACIÓN ESPECÍFICA ---
    // GET /planeaciones/{planeacionId}/insumos/edit/{insumoPlaneacionId}
    @GetMapping("/edit/{insumoPlaneacionId}")
    public String showEditInsumoPlaneacionForm(@PathVariable Long planeacionId,
                                               @PathVariable Long insumoPlaneacionId,
                                               Model model,
                                               RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId); // Validar la planeación
            // Buscar el InsumoPlaneacion, asegurando que pertenece al usuario
            Optional<InsumoPlaneacion> insumoPlaneacionOptional = insumoPlaneacionService.findByIdAndPlaneacionOwnedByUser(insumoPlaneacionId, currentUser);

            if (insumoPlaneacionOptional.isEmpty() || !insumoPlaneacionOptional.get().getPlaneacion().getId().equals(planeacionId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Asociación insumo-planeación no encontrada o no autorizada.");
                return "redirect:/planeaciones/" + planeacionId + "/insumos";
            }

            model.addAttribute("planeacion", planeacion);
            model.addAttribute("insumoPlaneacion", insumoPlaneacionOptional.get());
            model.addAttribute("allInsumos", insumoService.findByUsuario(currentUser)); // Solo insumos del usuario
            return "insumos_planeacion/insumo-planeacion-form";
        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones";
        }
    }

    // --- ELIMINAR UN INSUMO DE UNA PLANEACIÓN ESPECÍFICA ---
    // POST /planeaciones/{planeacionId}/insumos/delete/{insumoPlaneacionId}
    @PostMapping("/delete/{insumoPlaneacionId}")
    public String deleteInsumoPlaneacion(@PathVariable Long planeacionId,
                                         @PathVariable Long insumoPlaneacionId,
                                         RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        try {
            // La validación de permisos se hace dentro del servicio
            insumoPlaneacionService.deleteInsumoPlaneacion(insumoPlaneacionId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Insumo eliminado de la planeación exitosamente!");
        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el insumo de la planeación: " + e.getMessage());
        }
        // Siempre redirigir de vuelta a la lista de insumos de la misma planeación
        return "redirect:/planeaciones/" + planeacionId + "/insumos";
    }
}