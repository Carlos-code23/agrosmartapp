package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Siembra;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.PlaneacionCultivoService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.SiembraService;
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
@RequestMapping("/planeaciones/{planeacionId}/siembras")
public class SiembraWebController {

    private final SiembraService siembraService;
    private final PlaneacionCultivoService planeacionCultivoService;
    private final UsuarioService usuarioService;

    public SiembraWebController(SiembraService siembraService,
                                PlaneacionCultivoService planeacionCultivoService,
                                UsuarioService usuarioService) {
        this.siembraService = siembraService;
        this.planeacionCultivoService = planeacionCultivoService;
        this.usuarioService = usuarioService;
    }

    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en la base de datos."));
    }

    private PlaneacionCultivo getPlaneacionIfAuthorized(Long planeacionId) {
        Usuario currentUser = getAuthenticatedUser();
        return planeacionCultivoService.getPlaneacionCultivoById(planeacionId)
                .filter(p -> p.getUsuario().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Planeación no encontrada o no autorizado."));
    }

    // Listar siembras para una planeación específica
    @GetMapping
    public String listSiembrasByPlaneacion(@PathVariable Long planeacionId, Model model, RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            List<Siembra> siembras = siembraService.getSiembrasByPlaneacionId(planeacionId);
            model.addAttribute("planeacion", planeacion);
            model.addAttribute("siembras", siembras);
            return "siembras/list-siembras"; // src/main/resources/templates/siembras/list-siembras.html
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones";
        }
    }

    // Mostrar formulario para añadir una siembra a una planeación
    @GetMapping("/new")
    public String showAddSiembraForm(@PathVariable Long planeacionId, Model model, RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            model.addAttribute("planeacion", planeacion);
            model.addAttribute("siembra", new Siembra());
            return "siembras/siembra-form"; // src/main/resources/templates/siembras/siembra-form.html
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones";
        }
    }

    // Procesar el formulario para añadir/actualizar una siembra
    @PostMapping
    public String saveSiembra(@PathVariable Long planeacionId,
                              @Valid @ModelAttribute("siembra") Siembra siembra,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);

            if (result.hasErrors()) {
                model.addAttribute("planeacion", planeacion);
                return "siembras/siembra-form";
            }

            // Asegurarse de que el objeto PlaneacionCultivo completo se asocia
            siembra.setPlaneacion(planeacion);

            // Si es una actualización, buscar el existente
            if (siembra.getId() != null) {
                Optional<Siembra> existing = siembraService.getSiembraById(siembra.getId());
                if (existing.isPresent() && existing.get().getPlaneacion().getId().equals(planeacionId)) {
                    siembraService.updateSiembra(siembra.getId(), siembra);
                    redirectAttributes.addFlashAttribute("successMessage", "Siembra actualizada exitosamente!");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Siembra no encontrada o no autorizada.");
                }
            } else {
                siembraService.saveSiembra(siembra);
                redirectAttributes.addFlashAttribute("successMessage", "Siembra añadida exitosamente!");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar la siembra: " + e.getMessage());
        }
        return "redirect:/planeaciones/" + planeacionId + "/siembras";
    }

    // Mostrar formulario para editar una siembra
    @GetMapping("/edit/{siembraId}")
    public String showEditSiembraForm(@PathVariable Long planeacionId,
                                      @PathVariable Long siembraId,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            Optional<Siembra> siembraOptional = siembraService.getSiembraById(siembraId);

            if (siembraOptional.isEmpty() || !siembraOptional.get().getPlaneacion().getId().equals(planeacionId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Siembra no encontrada o no autorizada.");
                return "redirect:/planeaciones/" + planeacionId + "/siembras";
            }

            model.addAttribute("planeacion", planeacion);
            model.addAttribute("siembra", siembraOptional.get());
            return "siembras/siembra-form";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones";
        }
    }

    // Eliminar una siembra
    @PostMapping("/delete/{siembraId}")
    public String deleteSiembra(@PathVariable Long planeacionId,
                                @PathVariable Long siembraId,
                                RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            Optional<Siembra> siembraOptional = siembraService.getSiembraById(siembraId);

            if (siembraOptional.isEmpty() || !siembraOptional.get().getPlaneacion().getId().equals(planeacion.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Siembra no encontrada o no autorizada para eliminar.");
                return "redirect:/planeaciones/" + planeacionId + "/siembras";
            }

            siembraService.deleteSiembra(siembraId);
            redirectAttributes.addFlashAttribute("successMessage", "Siembra eliminada exitosamente!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la siembra: " + e.getMessage());
        }
        return "redirect:/planeaciones/" + planeacionId + "/siembras";
    }
}
