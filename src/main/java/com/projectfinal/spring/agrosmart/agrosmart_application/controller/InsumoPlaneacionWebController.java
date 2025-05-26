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
@RequestMapping("/planeaciones/{planeacionId}/insumos")
public class InsumoPlaneacionWebController {

    private final InsumoPlaneacionService insumoPlaneacionService;
    private final PlaneacionCultivoService planeacionCultivoService;
    private final InsumoService insumoService;
    private final UsuarioService usuarioService; // Para verificar permisos

    public InsumoPlaneacionWebController(InsumoPlaneacionService insumoPlaneacionService,
                                         PlaneacionCultivoService planeacionCultivoService,
                                         InsumoService insumoService,
                                         UsuarioService usuarioService) {
        this.insumoPlaneacionService = insumoPlaneacionService;
        this.planeacionCultivoService = planeacionCultivoService;
        this.insumoService = insumoService;
        this.usuarioService = usuarioService;
    }

    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en la base de datos."));
    }

    // Método de utilidad para verificar que la planeación pertenezca al usuario autenticado
    private PlaneacionCultivo getPlaneacionIfAuthorized(Long planeacionId) {
        Usuario currentUser = getAuthenticatedUser();
        return planeacionCultivoService.getPlaneacionCultivoById(planeacionId)
                .filter(p -> p.getUsuario().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Planeación no encontrada o no autorizado."));
    }

    // Listar insumos asociados a una planeación específica
    @GetMapping
    public String listInsumosByPlaneacion(@PathVariable Long planeacionId, Model model, RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            List<InsumoPlaneacion> insumosPlaneacion = insumoPlaneacionService.getInsumosByPlaneacionId(planeacionId);
            model.addAttribute("planeacion", planeacion);
            model.addAttribute("insumosPlaneacion", insumosPlaneacion);
            return "insumos_planeacion/list-insumos-planeacion"; // src/main/resources/templates/insumos_planeacion/list-insumos-planeacion.html
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones";
        }
    }

    // Mostrar formulario para añadir un insumo a una planeación
    @GetMapping("/new")
    public String showAddInsumoForm(@PathVariable Long planeacionId, Model model, RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            model.addAttribute("planeacion", planeacion);
            model.addAttribute("insumoPlaneacion", new InsumoPlaneacion());
            model.addAttribute("allInsumos", insumoService.getAllInsumos()); // Lista de todos los insumos disponibles
            return "insumos_planeacion/insumo-planeacion-form"; // src/main/resources/templates/insumos_planeacion/insumo-planeacion-form.html
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones";
        }
    }

    // Procesar el formulario para añadir/actualizar un insumo en una planeación
    @PostMapping
    public String saveInsumoPlaneacion(@PathVariable Long planeacionId,
                                       @Valid @ModelAttribute("insumoPlaneacion") InsumoPlaneacion insumoPlaneacion,
                                       BindingResult result,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);

            // Validar que se seleccionó un insumo
            if (insumoPlaneacion.getInsumo() == null || insumoPlaneacion.getInsumo().getId() == null) {
                result.rejectValue("insumo", "null.insumo", "Debe seleccionar un insumo.");
            }

            if (result.hasErrors()) {
                model.addAttribute("planeacion", planeacion);
                model.addAttribute("allInsumos", insumoService.getAllInsumos());
                return "insumos_planeacion/insumo-planeacion-form";
            }

            // Asegurarse de que los objetos completos se asocian
            insumoPlaneacion.setPlaneacion(planeacion);
            Optional<Insumo> insumoOpt = insumoService.getInsumoById(insumoPlaneacion.getInsumo().getId());
            if (insumoOpt.isEmpty()) {
                result.rejectValue("insumo", "notFound.insumo", "El insumo seleccionado no es válido.");
                model.addAttribute("planeacion", planeacion);
                model.addAttribute("allInsumos", insumoService.getAllInsumos());
                return "insumos_planeacion/insumo-planeacion-form";
            }
            insumoPlaneacion.setInsumo(insumoOpt.get());

            // Si es una actualización, buscar el existente
            if (insumoPlaneacion.getId() != null) {
                Optional<InsumoPlaneacion> existing = insumoPlaneacionService.getInsumoPlaneacionById(insumoPlaneacion.getId());
                if (existing.isPresent() && existing.get().getPlaneacion().getId().equals(planeacionId)) {
                    insumoPlaneacionService.updateInsumoPlaneacion(insumoPlaneacion.getId(), insumoPlaneacion);
                    redirectAttributes.addFlashAttribute("successMessage", "Insumo en planeación actualizado exitosamente!");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Insumo en planeación no encontrado o no autorizado.");
                }
            } else {
                insumoPlaneacionService.saveInsumoPlaneacion(insumoPlaneacion);
                redirectAttributes.addFlashAttribute("successMessage", "Insumo añadido a la planeación exitosamente!");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el insumo en la planeación: " + e.getMessage());
        }
        return "redirect:/planeaciones/" + planeacionId + "/insumos";
    }

    // Mostrar formulario para editar un insumo asociado a una planeación
    @GetMapping("/edit/{insumoPlaneacionId}")
    public String showEditInsumoPlaneacionForm(@PathVariable Long planeacionId,
                                               @PathVariable Long insumoPlaneacionId,
                                               Model model,
                                               RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            Optional<InsumoPlaneacion> insumoPlaneacionOptional = insumoPlaneacionService.getInsumoPlaneacionById(insumoPlaneacionId);

            if (insumoPlaneacionOptional.isEmpty() || !insumoPlaneacionOptional.get().getPlaneacion().getId().equals(planeacionId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Asociación insumo-planeación no encontrada o no autorizada.");
                return "redirect:/planeaciones/" + planeacionId + "/insumos";
            }

            model.addAttribute("planeacion", planeacion);
            model.addAttribute("insumoPlaneacion", insumoPlaneacionOptional.get());
            model.addAttribute("allInsumos", insumoService.getAllInsumos());
            return "insumos_planeacion/insumo-planeacion-form";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones";
        }
    }

    // Eliminar un insumo de una planeación
    @PostMapping("/delete/{insumoPlaneacionId}")
    public String deleteInsumoPlaneacion(@PathVariable Long planeacionId,
                                         @PathVariable Long insumoPlaneacionId,
                                         RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId); // Verifica autorización
            Optional<InsumoPlaneacion> insumoPlaneacionOptional = insumoPlaneacionService.getInsumoPlaneacionById(insumoPlaneacionId);

            if (insumoPlaneacionOptional.isEmpty() || !insumoPlaneacionOptional.get().getPlaneacion().getId().equals(planeacion.getId())) { 
            redirectAttributes.addFlashAttribute("errorMessage", "Asociación insumo-planeación no encontrada o no autorizada para eliminar.");
            return "redirect:/planeaciones/" + planeacionId + "/insumos";
            }

            insumoPlaneacionService.deleteInsumoPlaneacion(insumoPlaneacionId);
            redirectAttributes.addFlashAttribute("successMessage", "Insumo eliminado de la planeación exitosamente!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el insumo de la planeación: " + e.getMessage());
        }
        return "redirect:/planeaciones/" + planeacionId + "/insumos";
    }
}
