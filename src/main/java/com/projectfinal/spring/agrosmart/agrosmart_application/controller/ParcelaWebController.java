package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Parcela;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.ParcelaService;
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
@RequestMapping("/parcelas")
public class ParcelaWebController {

    private final ParcelaService parcelaService;
    private final UsuarioService usuarioService; // Para obtener el usuario autenticado

    public ParcelaWebController(ParcelaService parcelaService, UsuarioService usuarioService) {
        this.parcelaService = parcelaService;
        this.usuarioService = usuarioService;
    }

    // Obtener el usuario autenticado para asociar la parcela
    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // El email del usuario autenticado
        return usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en la base de datos."));
    }

    // Mostrar todas las parcelas del usuario autenticado
    @GetMapping
    public String listParcelas(Model model) {
        Usuario currentUser = getAuthenticatedUser();
        List<Parcela> parcelas = parcelaService.getParcelasByUsuarioId(currentUser.getId());
        model.addAttribute("parcelas", parcelas);
        model.addAttribute("currentUser", currentUser); // Para mostrar el nombre del usuario
        return "parcelas/list-parcelas"; // src/main/resources/templates/parcelas/list-parcelas.html
    }

    // Mostrar formulario para crear una nueva parcela
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("parcela", new Parcela());
        // No necesitamos pasar el usuario aquí, lo asignaremos en el POST
        return "parcelas/parcela-form"; // src/main/resources/templates/parcelas/parcela-form.html
    }

    // Procesar el formulario de creación de parcela
    @PostMapping
    public String saveParcela(@Valid @ModelAttribute("parcela") Parcela parcela,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "parcelas/parcela-form";
        }

        // Asignar el usuario autenticado a la parcela antes de guardar
        Usuario currentUser = getAuthenticatedUser();
        parcela.setUsuario(currentUser);

        try {
            parcelaService.saveParcela(parcela);
            redirectAttributes.addFlashAttribute("successMessage", "Parcela guardada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar la parcela: " + e.getMessage());
        }

        return "redirect:/parcelas";
    }

    // Mostrar formulario para editar una parcela existente
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        Optional<Parcela> parcelaOptional = parcelaService.getParcelaById(id);

        if (parcelaOptional.isEmpty() || !parcelaOptional.get().getUsuario().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Parcela no encontrada o no tienes permiso para editarla.");
            return "redirect:/parcelas";
        }

        model.addAttribute("parcela", parcelaOptional.get());
        return "parcelas/parcela-form";
    }

    // Procesar el formulario de actualización de parcela
    @PostMapping("/update/{id}")
    public String updateParcela(@PathVariable Long id,
                                @Valid @ModelAttribute("parcela") Parcela parcelaDetails,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // Asegúrate de que el ID siga presente para el formulario
            parcelaDetails.setId(id);
            return "parcelas/parcela-form";
        }

        Usuario currentUser = getAuthenticatedUser();
        // Validar que la parcela pertenezca al usuario autenticado antes de actualizar
        Optional<Parcela> existingParcela = parcelaService.getParcelaById(id);
        if (existingParcela.isEmpty() || !existingParcela.get().getUsuario().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Parcela no encontrada o no tienes permiso para actualizarla.");
            return "redirect:/parcelas";
        }

        try {
            // Asegurar que el usuario no se sobrescriba (ya está seteado en existingParcela)
            parcelaDetails.setUsuario(existingParcela.get().getUsuario());
            parcelaService.updateParcela(id, parcelaDetails);
            redirectAttributes.addFlashAttribute("successMessage", "Parcela actualizada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar la parcela: " + e.getMessage());
        }

        return "redirect:/parcelas";
    }

    // Eliminar una parcela
    @PostMapping("/delete/{id}")
    public String deleteParcela(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        Optional<Parcela> parcelaOptional = parcelaService.getParcelaById(id);

        if (parcelaOptional.isEmpty() || !parcelaOptional.get().getUsuario().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Parcela no encontrada o no tienes permiso para eliminarla.");
            return "redirect:/parcelas";
        }

        try {
            parcelaService.deleteParcela(id);
            redirectAttributes.addFlashAttribute("successMessage", "Parcela eliminada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la parcela: " + e.getMessage());
        }
        return "redirect:/parcelas";
    }
}