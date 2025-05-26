package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.EtapaCultivoService;
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
@RequestMapping("/etapas")
public class EtapaCultivoWebController {

    private final EtapaCultivoService etapaCultivoService;
    private final TipoCultivoService tipoCultivoService; // Para el dropdown de TipoCultivo

    public EtapaCultivoWebController(EtapaCultivoService etapaCultivoService, TipoCultivoService tipoCultivoService) {
        this.etapaCultivoService = etapaCultivoService;
        this.tipoCultivoService = tipoCultivoService;
    }

    // Mostrar todas las etapas de cultivo
    @GetMapping
    public String listEtapasCultivo(Model model) {
        List<EtapaCultivo> etapasCultivo = etapaCultivoService.getAllEtapasCultivo();
        model.addAttribute("etapasCultivo", etapasCultivo);
        return "etapas/list-etapas"; // src/main/resources/templates/etapas/list-etapas.html
    }

    // Mostrar formulario para crear una nueva etapa
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("etapaCultivo", new EtapaCultivo());
        model.addAttribute("tiposCultivo", tipoCultivoService.getAllTiposCultivo()); // Pasa los tipos de cultivo
        return "etapas/etapa-form"; // src/main/resources/templates/etapas/etapa-form.html
    }

    // Procesar el formulario de creación de etapa
    @PostMapping
    public String saveEtapaCultivo(@Valid @ModelAttribute("etapaCultivo") EtapaCultivo etapaCultivo,
                                   BindingResult result,
                                   Model model, // Necesario para recargar el dropdown en caso de error
                                   RedirectAttributes redirectAttributes) {

        // Validar que el tipoCultivo no sea nulo si viene de un select
        if (etapaCultivo.getTipoCultivo() == null || etapaCultivo.getTipoCultivo().getId() == null) {
            result.rejectValue("tipoCultivo", "null.tipoCultivo", "Debe seleccionar un tipo de cultivo.");
        }

        if (result.hasErrors()) {
            model.addAttribute("tiposCultivo", tipoCultivoService.getAllTiposCultivo());
            return "etapas/etapa-form";
        }
        try {
            // Asegúrate de que el objeto TipoCultivo esté completamente cargado si solo viene con ID
            if (etapaCultivo.getTipoCultivo() != null && etapaCultivo.getTipoCultivo().getId() != null) {
                Optional<TipoCultivo> tc = tipoCultivoService.getTipoCultivoById(etapaCultivo.getTipoCultivo().getId());
                if (tc.isPresent()) {
                    etapaCultivo.setTipoCultivo(tc.get());
                } else {
                    result.rejectValue("tipoCultivo", "notFound.tipoCultivo", "El tipo de cultivo seleccionado no es válido.");
                    model.addAttribute("tiposCultivo", tipoCultivoService.getAllTiposCultivo());
                    return "etapas/etapa-form";
                }
            }

            etapaCultivoService.saveEtapaCultivo(etapaCultivo);
            redirectAttributes.addFlashAttribute("successMessage", "Etapa de cultivo guardada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar la etapa de cultivo: " + e.getMessage());
        }
        return "redirect:/etapas";
    }

    // Mostrar formulario para editar una etapa existente
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<EtapaCultivo> etapaCultivoOptional = etapaCultivoService.getEtapaCultivoById(id);
        if (etapaCultivoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Etapa de cultivo no encontrada.");
            return "redirect:/etapas";
        }
        model.addAttribute("etapaCultivo", etapaCultivoOptional.get());
        model.addAttribute("tiposCultivo", tipoCultivoService.getAllTiposCultivo()); // Pasa los tipos de cultivo
        return "etapas/etapa-form";
    }

    // Procesar el formulario de actualización de etapa
    @PostMapping("/update/{id}")
    public String updateEtapaCultivo(@PathVariable Long id,
                                     @Valid @ModelAttribute("etapaCultivo") EtapaCultivo etapaCultivoDetails,
                                     BindingResult result,
                                     Model model, // Necesario para recargar el dropdown en caso de error
                                     RedirectAttributes redirectAttributes) {

        if (etapaCultivoDetails.getTipoCultivo() == null || etapaCultivoDetails.getTipoCultivo().getId() == null) {
            result.rejectValue("tipoCultivo", "null.tipoCultivo", "Debe seleccionar un tipo de cultivo.");
        }

        if (result.hasErrors()) {
            model.addAttribute("tiposCultivo", tipoCultivoService.getAllTiposCultivo());
            etapaCultivoDetails.setId(id); // Asegura que el ID se mantenga
            return "etapas/etapa-form";
        }

        try {
            // Asegúrate de que el objeto TipoCultivo esté completamente cargado
            if (etapaCultivoDetails.getTipoCultivo() != null && etapaCultivoDetails.getTipoCultivo().getId() != null) {
                Optional<TipoCultivo> tc = tipoCultivoService.getTipoCultivoById(etapaCultivoDetails.getTipoCultivo().getId());
                if (tc.isPresent()) {
                    etapaCultivoDetails.setTipoCultivo(tc.get());
                } else {
                    result.rejectValue("tipoCultivo", "notFound.tipoCultivo", "El tipo de cultivo seleccionado no es válido.");
                    model.addAttribute("tiposCultivo", tipoCultivoService.getAllTiposCultivo());
                    etapaCultivoDetails.setId(id);
                    return "etapas/etapa-form";
                }
            }
            etapaCultivoService.updateEtapaCultivo(id, etapaCultivoDetails);
            redirectAttributes.addFlashAttribute("successMessage", "Etapa de cultivo actualizada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar la etapa de cultivo: " + e.getMessage());
        }
        return "redirect:/etapas";
    }

    // Eliminar una etapa de cultivo
    @PostMapping("/delete/{id}")
    public String deleteEtapaCultivo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            etapaCultivoService.deleteEtapaCultivo(id);
            redirectAttributes.addFlashAttribute("successMessage", "Etapa de cultivo eliminada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la etapa de cultivo: " + e.getMessage());
        }
        return "redirect:/etapas";
    }
}