package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.EtapaCultivoService;
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
@RequestMapping("/etapas") // Mapeo para el CRUD de Etapas de Cultivo
public class EtapaCultivoWebController {

    private final EtapaCultivoService etapaCultivoService;
    private final UsuarioService usuarioService;

    public EtapaCultivoWebController(EtapaCultivoService etapaCultivoService, UsuarioService usuarioService) {
        this.etapaCultivoService = etapaCultivoService;
        this.usuarioService = usuarioService;
    }

    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en la base de datos."));
    }

    // --- LISTAR ETAPAS DE CULTIVO DEL USUARIO ---
    @GetMapping
    public String listEtapas(Model model) {
        Usuario currentUser = getAuthenticatedUser();
        List<EtapaCultivo> etapas = etapaCultivoService.findByUsuario(currentUser);
        model.addAttribute("etapas", etapas);
        return "etapas_cultivo/list-etapas"; // Vista para listar etapas
    }

    // --- MOSTRAR FORMULARIO DE CREACIÓN/EDICIÓN DE ETAPA ---
    @GetMapping({"/new", "/edit/{id}"})
    public String showForm(@PathVariable(required = false) Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        if (id != null) {
            Optional<EtapaCultivo> etapaOptional = etapaCultivoService.getEtapaCultivoByIdAndUsuario(id, currentUser);
            if (etapaOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Etapa de cultivo no encontrada o no autorizada.");
                return "redirect:/etapas";
            }
            model.addAttribute("etapaCultivo", etapaOptional.get());
        } else {
            model.addAttribute("etapaCultivo", new EtapaCultivo());
        }
        return "etapas_cultivo/etapa-form"; // Vista del formulario
    }

    // --- PROCESAR FORMULARIO (GUARDAR/ACTUALIZAR ETAPA) ---
    @PostMapping
    public String saveEtapa(@Valid @ModelAttribute("etapaCultivo") EtapaCultivo etapaCultivo,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        Usuario currentUser = getAuthenticatedUser();
        etapaCultivo.setUsuario(currentUser); // Asegura que la etapa se asocie al usuario actual

        if (result.hasErrors()) {
            return "etapas_cultivo/etapa-form";
        }

        try {
            etapaCultivoService.saveEtapaCultivo(etapaCultivo);
            redirectAttributes.addFlashAttribute("successMessage", "Etapa de cultivo guardada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar la etapa de cultivo: " + e.getMessage());
            return "etapas_cultivo/etapa-form";
        }
        return "redirect:/etapas";
    }

    // --- ELIMINAR ETAPA DE CULTIVO ---
    @PostMapping("/delete/{id}")
    public String deleteEtapa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        try {
            etapaCultivoService.deleteEtapaCultivo(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Etapa de cultivo eliminada exitosamente!");
        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la etapa de cultivo: " + e.getMessage());
        }
        return "redirect:/etapas";
    }
}