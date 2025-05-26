package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Insumo;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.InsumoService;

import jakarta.validation.Valid;
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

    public InsumoWebController(InsumoService insumoService) {
        this.insumoService = insumoService;
    }

    // Mostrar todos los insumos
    @GetMapping
    public String listInsumos(Model model) {
        List<Insumo> insumos = insumoService.getAllInsumos();
        model.addAttribute("insumos", insumos);
        return "insumos/list-insumos"; // src/main/resources/templates/insumos/list-insumos.html
    }

    // Mostrar formulario para crear un nuevo insumo
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("insumo", new Insumo());
        return "insumos/insumo-form"; // src/main/resources/templates/insumos/insumo-form.html
    }

    // Procesar el formulario de creación de insumo
    @PostMapping
    public String saveInsumo(@Valid @ModelAttribute("insumo") Insumo insumo,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "insumos/insumo-form";
        }
        try {
            insumoService.saveInsumo(insumo);
            redirectAttributes.addFlashAttribute("successMessage", "Insumo guardado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el insumo: " + e.getMessage());
        }
        return "redirect:/insumos";
    }

    // Mostrar formulario para editar un insumo existente
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Insumo> insumoOptional = insumoService.getInsumoById(id);
        if (insumoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Insumo no encontrado.");
            return "redirect:/insumos";
        }
        model.addAttribute("insumo", insumoOptional.get());
        return "insumos/insumo-form";
    }

    // Procesar el formulario de actualización de insumo
    @PostMapping("/update/{id}")
    public String updateInsumo(@PathVariable Long id,
                               @Valid @ModelAttribute("insumo") Insumo insumoDetails,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            insumoDetails.setId(id); // Asegura que el ID se mantenga para la vista del formulario
            return "insumos/insumo-form";
        }
        try {
            insumoService.updateInsumo(id, insumoDetails);
            redirectAttributes.addFlashAttribute("successMessage", "Insumo actualizado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el insumo: " + e.getMessage());
        }
        return "redirect:/insumos";
    }

    // Eliminar un insumo
    @PostMapping("/delete/{id}")
    public String deleteInsumo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            insumoService.deleteInsumo(id);
            redirectAttributes.addFlashAttribute("successMessage", "Insumo eliminado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el insumo: " + e.getMessage());
        }
        return "redirect:/insumos";
    }
}