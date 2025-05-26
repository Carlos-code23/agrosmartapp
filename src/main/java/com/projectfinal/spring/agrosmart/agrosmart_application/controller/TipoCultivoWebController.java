package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.TipoCultivoService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tipos-cultivo")
public class TipoCultivoWebController {

    private final TipoCultivoService tipoCultivoService;

    public TipoCultivoWebController(TipoCultivoService tipoCultivoService) {
        this.tipoCultivoService = tipoCultivoService;
    }

    // Mostrar todos los tipos de cultivo
    @GetMapping
    public String listTiposCultivo(Model model) {
        List<TipoCultivo> tiposCultivo = tipoCultivoService.getAllTiposCultivo();
        model.addAttribute("tiposCultivo", tiposCultivo);
        return "tipos_cultivo/list-tipos"; // src/main/resources/templates/tipos_cultivo/list-tipos.html
    }

    // Mostrar formulario para crear un nuevo tipo de cultivo
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("tipoCultivo", new TipoCultivo());
        return "tipos_cultivo/tipo-form"; // src/main/resources/templates/tipos_cultivo/tipo-form.html
    }

    // Procesar el formulario de creación de tipo de cultivo
    @PostMapping
    public String saveTipoCultivo(@Valid @ModelAttribute("tipoCultivo") TipoCultivo tipoCultivo,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "tipos_cultivo/tipo-form";
        }
        try {
            tipoCultivoService.saveTipoCultivo(tipoCultivo);
            redirectAttributes.addFlashAttribute("successMessage", "Tipo de cultivo guardado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el tipo de cultivo: " + e.getMessage());
        }
        return "redirect:/tipos-cultivo";
    }

    // Mostrar formulario para editar un tipo de cultivo existente
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<TipoCultivo> tipoCultivoOptional = tipoCultivoService.getTipoCultivoById(id);
        if (tipoCultivoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tipo de cultivo no encontrado.");
            return "redirect:/tipos-cultivo";
        }
        model.addAttribute("tipoCultivo", tipoCultivoOptional.get());
        return "tipos_cultivo/tipo-form";
    }

    // Procesar el formulario de actualización de tipo de cultivo
    @PostMapping("/update/{id}")
    public String updateTipoCultivo(@PathVariable Long id,
                                    @Valid @ModelAttribute("tipoCultivo") TipoCultivo tipoCultivoDetails,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            tipoCultivoDetails.setId(id); // Asegura que el ID se mantenga para la vista del formulario
            return "tipos_cultivo/tipo-form";
        }
        try {
            tipoCultivoService.updateTipoCultivo(id, tipoCultivoDetails);
            redirectAttributes.addFlashAttribute("successMessage", "Tipo de cultivo actualizado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el tipo de cultivo: " + e.getMessage());
        }
        return "redirect:/tipos-cultivo";
    }

    // Eliminar un tipo de cultivo
    @PostMapping("/delete/{id}")
    public String deleteTipoCultivo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tipoCultivoService.deleteTipoCultivo(id);
            redirectAttributes.addFlashAttribute("successMessage", "Tipo de cultivo eliminado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el tipo de cultivo: " + e.getMessage());
        }
        return "redirect:/tipos-cultivo";
    }
}
