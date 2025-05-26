package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Parcela;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.ParcelaService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.PlaneacionCultivoService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.TipoCultivoService;
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
@RequestMapping("/planeaciones")
public class PlaneacionCultivoWebController {

    private final PlaneacionCultivoService planeacionCultivoService;
    private final ParcelaService parcelaService;
    private final TipoCultivoService tipoCultivoService;
    private final UsuarioService usuarioService;

    public PlaneacionCultivoWebController(PlaneacionCultivoService planeacionCultivoService,
                                        ParcelaService parcelaService,
                                        TipoCultivoService tipoCultivoService,
                                        UsuarioService usuarioService) {
        this.planeacionCultivoService = planeacionCultivoService;
        this.parcelaService = parcelaService;
        this.tipoCultivoService = tipoCultivoService;
        this.usuarioService = usuarioService;
    }

    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en la base de datos."));
    }

    // Mostrar todas las planeaciones de cultivo del usuario autenticado
    @GetMapping
    public String listPlaneaciones(Model model) {
        Usuario currentUser = getAuthenticatedUser();
        List<PlaneacionCultivo> planeaciones = planeacionCultivoService.getPlaneacionesByUsuarioId(currentUser.getId());
        model.addAttribute("planeaciones", planeaciones);
        return "planeaciones/list-planeaciones"; // src/main/resources/templates/planeaciones/list-planeaciones.html
    }

    // Mostrar formulario para crear una nueva planeación
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Usuario currentUser = getAuthenticatedUser();
        model.addAttribute("planeacion", new PlaneacionCultivo());
        model.addAttribute("parcelas", parcelaService.getParcelasByUsuarioId(currentUser.getId()));
        model.addAttribute("tiposCultivo", tipoCultivoService.getAllTiposCultivo());
        return "planeaciones/planeacion-form"; // src/main/resources/templates/planeaciones/planeacion-form.html
    }

    // Procesar el formulario de creación de planeación
    @PostMapping
    public String savePlaneacion(@Valid @ModelAttribute("planeacion") PlaneacionCultivo planeacion,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        // Si la parcela o tipoCultivo no están bien asociados desde el formulario, podrían ser null
        // Esto podría necesitar un DTO específico para validar IDs antes de convertirlos a objetos.
        // Por ahora, asumimos que los IDs vienen y los servicios los encuentran.
        if (planeacion.getParcela() == null || planeacion.getParcela().getId() == null) {
            result.rejectValue("parcela", "null.parcela", "Debe seleccionar una parcela.");
        }
        if (planeacion.getTipoCultivo() == null || planeacion.getTipoCultivo().getId() == null) {
            result.rejectValue("tipoCultivo", "null.tipoCultivo", "Debe seleccionar un tipo de cultivo.");
        }

        if (result.hasErrors()) {
            Usuario currentUser = getAuthenticatedUser();
            model.addAttribute("parcelas", parcelaService.getParcelasByUsuarioId(currentUser.getId()));
            model.addAttribute("tiposCultivo", tipoCultivoService.getAllTiposCultivo());
            return "planeaciones/planeacion-form";
        }

        // Asignar el usuario autenticado a la planeación
        Usuario currentUser = getAuthenticatedUser();
        planeacion.setUsuario(currentUser);

        try {
            planeacionCultivoService.savePlaneacionCultivo(planeacion); // El servicio calcula numeroSemillas
            redirectAttributes.addFlashAttribute("successMessage", "Planeación de cultivo guardada exitosamente!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones/new"; // Vuelve al formulario si hay error de lógica
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar la planeación: " + e.getMessage());
        }

        return "redirect:/planeaciones";
    }

    // Mostrar formulario para editar una planeación existente
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        Optional<PlaneacionCultivo> planeacionOptional = planeacionCultivoService.getPlaneacionCultivoById(id);

        if (planeacionOptional.isEmpty() || !planeacionOptional.get().getUsuario().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Planeación no encontrada o no tienes permiso para editarla.");
            return "redirect:/planeaciones";
        }

        model.addAttribute("planeacion", planeacionOptional.get());
        model.addAttribute("parcelas", parcelaService.getParcelasByUsuarioId(currentUser.getId()));
        model.addAttribute("tiposCultivo", tipoCultivoService.getAllTiposCultivo());
        return "planeaciones/planeacion-form";
    }

    // Procesar el formulario de actualización de planeación
    @PostMapping("/update/{id}")
    public String updatePlaneacion(@PathVariable Long id,
                                   @Valid @ModelAttribute("planeacion") PlaneacionCultivo planeacionDetails,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        if (planeacionDetails.getParcela() == null || planeacionDetails.getParcela().getId() == null) {
            result.rejectValue("parcela", "null.parcela", "Debe seleccionar una parcela.");
        }
        if (planeacionDetails.getTipoCultivo() == null || planeacionDetails.getTipoCultivo().getId() == null) {
            result.rejectValue("tipoCultivo", "null.tipoCultivo", "Debe seleccionar un tipo de cultivo.");
        }

        if (result.hasErrors()) {
            Usuario currentUser = getAuthenticatedUser();
            model.addAttribute("parcelas", parcelaService.getParcelasByUsuarioId(currentUser.getId()));
            model.addAttribute("tiposCultivo", tipoCultivoService.getAllTiposCultivo());
            planeacionDetails.setId(id); // Asegura que el ID se mantenga
            return "planeaciones/planeacion-form";
        }

        Usuario currentUser = getAuthenticatedUser();
        Optional<PlaneacionCultivo> existingPlaneacion = planeacionCultivoService.getPlaneacionCultivoById(id);
        if (existingPlaneacion.isEmpty() || !existingPlaneacion.get().getUsuario().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Planeación no encontrada o no tienes permiso para actualizarla.");
            return "redirect:/planeaciones";
        }

        // Asegurar que el usuario no se sobrescriba.
        planeacionDetails.setUsuario(existingPlaneacion.get().getUsuario());
        
        try {
            planeacionCultivoService.updatePlaneacionCultivo(id, planeacionDetails);
            redirectAttributes.addFlashAttribute("successMessage", "Planeación de cultivo actualizada exitosamente!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones/edit/" + id; // Vuelve al formulario de edición si hay error de lógica
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar la planeación: " + e.getMessage());
        }

        return "redirect:/planeaciones";
    }

    // Eliminar una planeación
    @PostMapping("/delete/{id}")
    public String deletePlaneacion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        Optional<PlaneacionCultivo> planeacionOptional = planeacionCultivoService.getPlaneacionCultivoById(id);

        if (planeacionOptional.isEmpty() || !planeacionOptional.get().getUsuario().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Planeación no encontrada o no tienes permiso para eliminarla.");
            return "redirect:/planeaciones";
        }

        try {
            planeacionCultivoService.deletePlaneacionCultivo(id);
            redirectAttributes.addFlashAttribute("successMessage", "Planeación de cultivo eliminada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la planeación: " + e.getMessage());
        }
        return "redirect:/planeaciones";
    }
}
