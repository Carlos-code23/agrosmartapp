package com.projectfinal.spring.agrosmart.agrosmart_application.controller; // Ajusta este paquete si es diferente

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Insumo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.InsumoService;
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
@RequestMapping("/insumos")
public class InsumoWebController {

    private final InsumoService insumoService;
    private final UsuarioService usuarioService;

    public InsumoWebController(InsumoService insumoService, UsuarioService usuarioService) {
        this.insumoService = insumoService;
        this.usuarioService = usuarioService;
    }

    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return usuarioService.findByEmail(username)
                          .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
    }

    // --- LISTAR INSUMOS ---
    @GetMapping
    public String listInsumos(Model model) {
        Usuario currentUser = getAuthenticatedUser();
        List<Insumo> insumos = insumoService.findByUsuario(currentUser);
        model.addAttribute("insumos", insumos);
        return "insumos/list-insumos";
    }

    // --- MOSTRAR FORMULARIO DE CREACIÓN ---
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("insumo", new Insumo());
        return "insumos/insumo-form";
    }

    // --- GUARDAR NUEVO INSUMO ---
    @PostMapping
    public String saveInsumo(@Valid @ModelAttribute("insumo") Insumo insumo,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (result.hasErrors()) {
            return "insumos/insumo-form";
        }

        try {
            Usuario currentUser = getAuthenticatedUser();
            insumo.setUsuario(currentUser);

            insumoService.saveInsumo(insumo);
            redirectAttributes.addFlashAttribute("successMessage", "Insumo guardado exitosamente!");
            return "redirect:/insumos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el insumo: " + e.getMessage());
            model.addAttribute("insumo", insumo);
            return "insumos/insumo-form";
        }
    }

    // --- MOSTRAR FORMULARIO DE EDICIÓN ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        // Usamos findByIdAndUsuario para asegurar que el insumo pertenece al usuario
        Optional<Insumo> insumoOptional = insumoService.findByIdAndUsuario(id, currentUser);

        if (insumoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Insumo no encontrado o no autorizado.");
            return "redirect:/insumos";
        }

        model.addAttribute("insumo", insumoOptional.get());
        return "insumos/insumo-form";
    }

    // --- ACTUALIZAR INSUMO ---
    @PostMapping("/update/{id}")
    public String updateInsumo(@PathVariable Long id,
                               @Valid @ModelAttribute("insumo") Insumo insumo,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (result.hasErrors()) {
            insumo.setId(id); // Asegura que el ID se mantenga para la vista
            return "insumos/insumo-form";
        }

        try {
            Usuario currentUser = getAuthenticatedUser();
            // Primero, verifica si el insumo existe y pertenece al usuario actual
            Optional<Insumo> existingInsumoOptional = insumoService.findByIdAndUsuario(id, currentUser);

            if (existingInsumoOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Insumo no encontrado o no autorizado.");
                return "redirect:/insumos";
            }

            // Si llegamos aquí, el insumo existe y pertenece al usuario.
            // Asignamos el ID y el usuario al objeto 'insumo' que viene del formulario.
            insumo.setId(id);
            insumo.setUsuario(currentUser);

            insumoService.saveInsumo(insumo); // Save para actualizar
            redirectAttributes.addFlashAttribute("successMessage", "Insumo actualizado exitosamente!");
            return "redirect:/insumos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el insumo: " + e.getMessage());
            insumo.setId(id); // Asegura que el ID se mantenga para la vista en caso de error
            return "insumos/insumo-form";
        }
    }

    // --- ELIMINAR INSUMO ---
    @PostMapping("/delete/{id}")
    public String deleteInsumo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        // Usamos findByIdAndUsuario para asegurar que el insumo pertenece al usuario antes de eliminar
        Optional<Insumo> insumoOptional = insumoService.findByIdAndUsuario(id, currentUser);

        if (insumoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Insumo no encontrado o no autorizado para eliminar.");
            return "redirect:/insumos";
        }

        try {
            insumoService.deleteInsumo(id);
            redirectAttributes.addFlashAttribute("successMessage", "Insumo eliminado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el insumo: " + e.getMessage());
        }
        return "redirect:/insumos";
    }
}