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
    private final UsuarioService usuarioService;

    public ParcelaWebController(ParcelaService parcelaService, UsuarioService usuarioService) {
        this.parcelaService = parcelaService;
        this.usuarioService = usuarioService;
    }

    // Obtener el usuario autenticado para asociar la parcela y para verificaciones de seguridad
    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); 
        return usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en la base de datos.")); // Cambiado a IllegalStateException
    }

    // Mostrar todas las parcelas del usuario autenticado
    @GetMapping
    public String listParcelas(Model model) {
        Usuario currentUser = getAuthenticatedUser();
        List<Parcela> parcelas = parcelaService.findByUsuario(currentUser);
        model.addAttribute("parcelas", parcelas);
        model.addAttribute("currentUser", currentUser);
        return "parcelas/list-parcelas";
    }

    // Mostrar formulario para crear una nueva parcela o editar una existente
    @GetMapping({"/new", "/edit/{id}"}) // Fusionamos los métodos de creación y edición
    public String showForm(@PathVariable(required = false) Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        Parcela parcela;

        if (id != null) {
            Optional<Parcela> parcelaOptional = parcelaService.getParcelaByIdAndUsuario(id, currentUser);
            if (parcelaOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Parcela no encontrada o no tienes permiso para editarla.");
                return "redirect:/parcelas";
            }
            parcela = parcelaOptional.get();
        } else {
            parcela = new Parcela();
        }
        model.addAttribute("parcela", parcela);
        return "parcelas/parcela-form";
    }

    // Procesar el formulario (guardar o actualizar parcela)
    // El método saveParcela en el servicio es capaz de manejar tanto la creación (id nulo)
    // como la actualización (id presente en el objeto Parcela).
    @PostMapping
    public String saveParcela(@Valid @ModelAttribute("parcela") Parcela parcela,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            // Si hay errores, volvemos al formulario.
            // El objeto 'parcela' ya tiene el ID si es una edición.
            return "parcelas/parcela-form";
        }

        Usuario currentUser = getAuthenticatedUser();
        parcela.setUsuario(currentUser); // Asignar el usuario autenticado a la parcela

        try {
            // Antes de guardar, si la parcela tiene un ID, verificamos que pertenezca al usuario.
            // Si es una nueva parcela (ID nulo), no hay verificación de propiedad previa.
            if (parcela.getId() != null) {
                parcelaService.getParcelaByIdAndUsuario(parcela.getId(), currentUser)
                        .orElseThrow(() -> new SecurityException("No tienes permiso para actualizar esta parcela."));
            }

            parcelaService.saveParcela(parcela); // Este método ahora maneja tanto crear como actualizar
            redirectAttributes.addFlashAttribute("successMessage", "Parcela guardada exitosamente!");
        } catch (SecurityException e) { // Capturamos la excepción de seguridad
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/parcelas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar la parcela: " + e.getMessage());
            // Si hay un error de servicio, se podría volver al formulario con el objeto 'parcela'
            // para que los datos no se pierdan, pero por simplicidad se redirige.
            // Para una mejor UX, podrías añadir: model.addAttribute("parcela", parcela); return "parcelas/parcela-form";
            return "redirect:/parcelas/edit/" + parcela.getId(); // Si es una edición y falla
        }

        return "redirect:/parcelas";
    }

    // ELIMINAR una parcela
    @PostMapping("/delete/{id}")
    public String deleteParcela(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        try {
            parcelaService.deleteParcela(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Parcela eliminada exitosamente!");
        } catch (IllegalArgumentException | SecurityException e) { // Capturamos excepciones específicas
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la parcela: " + e.getMessage());
        }
        return "redirect:/parcelas";
    }
}