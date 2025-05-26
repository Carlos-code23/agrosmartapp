package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.SeguimientoEtapa;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.EtapaCultivoService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.PlaneacionCultivoService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.SeguimientoEtapaService;
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
@RequestMapping("/planeaciones/{planeacionId}/seguimientos")
public class SeguimientoEtapaWebController {

    private final SeguimientoEtapaService seguimientoEtapaService;
    private final PlaneacionCultivoService planeacionCultivoService;
    private final EtapaCultivoService etapaCultivoService;
    private final UsuarioService usuarioService;

    public SeguimientoEtapaWebController(SeguimientoEtapaService seguimientoEtapaService,
                                         PlaneacionCultivoService planeacionCultivoService,
                                         EtapaCultivoService etapaCultivoService,
                                         UsuarioService usuarioService) {
        this.seguimientoEtapaService = seguimientoEtapaService;
        this.planeacionCultivoService = planeacionCultivoService;
        this.etapaCultivoService = etapaCultivoService;
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

    // Listar seguimientos para una planeación específica
    @GetMapping
    public String listSeguimientosByPlaneacion(@PathVariable Long planeacionId, Model model, RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            List<SeguimientoEtapa> seguimientos = seguimientoEtapaService.getSeguimientosByPlaneacionId(planeacionId);
            model.addAttribute("planeacion", planeacion);
            model.addAttribute("seguimientos", seguimientos);
            return "seguimientos_etapa/list-seguimientos-etapa"; // src/main/resources/templates/seguimientos_etapa/list-seguimientos-etapa.html
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones";
        }
    }

    // Mostrar formulario para añadir un seguimiento a una etapa en una planeación
    @GetMapping("/new")
    public String showAddSeguimientoForm(@PathVariable Long planeacionId, Model model, RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            model.addAttribute("planeacion", planeacion);
            model.addAttribute("seguimientoEtapa", new SeguimientoEtapa());
            // Obtener solo las etapas asociadas al tipo de cultivo de la planeación
            List<EtapaCultivo> etapasDisponibles = etapaCultivoService.getEtapasByTipoCultivoId(planeacion.getTipoCultivo().getId());
            model.addAttribute("etapasCultivo", etapasDisponibles);
            return "seguimientos_etapa/seguimiento-etapa-form"; // src/main/resources/templates/seguimientos_etapa/seguimiento-etapa-form.html
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones";
        }
    }

    // Procesar el formulario para añadir/actualizar un seguimiento
    @PostMapping
    public String saveSeguimientoEtapa(@PathVariable Long planeacionId,
                                       @Valid @ModelAttribute("seguimientoEtapa") SeguimientoEtapa seguimientoEtapa,
                                       BindingResult result,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);

            // Validar que se seleccionó una etapa
            if (seguimientoEtapa.getEtapa() == null || seguimientoEtapa.getEtapa().getId() == null) {
                result.rejectValue("etapa", "null.etapa", "Debe seleccionar una etapa de cultivo.");
            }

            if (result.hasErrors()) {
                model.addAttribute("planeacion", planeacion);
                model.addAttribute("etapasCultivo", etapaCultivoService.getEtapasByTipoCultivoId(planeacion.getTipoCultivo().getId()));
                return "seguimientos_etapa/seguimiento-etapa-form";
            }

            // Asegurarse de que los objetos completos se asocian
            seguimientoEtapa.setPlaneacion(planeacion);
            Optional<EtapaCultivo> etapaOpt = etapaCultivoService.getEtapaCultivoById(seguimientoEtapa.getEtapa().getId());
            if (etapaOpt.isEmpty()) {
                result.rejectValue("etapa", "notFound.etapa", "La etapa de cultivo seleccionada no es válida.");
                model.addAttribute("planeacion", planeacion);
                model.addAttribute("etapasCultivo", etapaCultivoService.getEtapasByTipoCultivoId(planeacion.getTipoCultivo().getId()));
                return "seguimientos_etapa/seguimiento-etapa-form";
            }
            seguimientoEtapa.setEtapa(etapaOpt.get());

            // Si es una actualización, buscar el existente
            if (seguimientoEtapa.getId() != null) {
                Optional<SeguimientoEtapa> existing = seguimientoEtapaService.getSeguimientoEtapaById(seguimientoEtapa.getId());
                if (existing.isPresent() && existing.get().getPlaneacion().getId().equals(planeacionId)) {
                    seguimientoEtapaService.updateSeguimientoEtapa(seguimientoEtapa.getId(), seguimientoEtapa);
                    redirectAttributes.addFlashAttribute("successMessage", "Seguimiento de etapa actualizado exitosamente!");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Seguimiento de etapa no encontrado o no autorizado.");
                }
            } else {
                seguimientoEtapaService.saveSeguimientoEtapa(seguimientoEtapa);
                redirectAttributes.addFlashAttribute("successMessage", "Seguimiento de etapa añadido exitosamente!");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el seguimiento de etapa: " + e.getMessage());
        }
        return "redirect:/planeaciones/" + planeacionId + "/seguimientos";
    }

    // Mostrar formulario para editar un seguimiento
    @GetMapping("/edit/{seguimientoId}")
    public String showEditSeguimientoForm(@PathVariable Long planeacionId,
                                          @PathVariable Long seguimientoId,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            Optional<SeguimientoEtapa> seguimientoOptional = seguimientoEtapaService.getSeguimientoEtapaById(seguimientoId);

            if (seguimientoOptional.isEmpty() || !seguimientoOptional.get().getPlaneacion().getId().equals(planeacionId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Seguimiento de etapa no encontrado o no autorizado.");
                return "redirect:/planeaciones/" + planeacionId + "/seguimientos";
            }

            model.addAttribute("planeacion", planeacion);
            model.addAttribute("seguimientoEtapa", seguimientoOptional.get());
            model.addAttribute("etapasCultivo", etapaCultivoService.getEtapasByTipoCultivoId(planeacion.getTipoCultivo().getId()));
            return "seguimientos_etapa/seguimiento-etapa-form";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/planeaciones";
        }
    }

    // Eliminar un seguimiento
    @PostMapping("/delete/{seguimientoId}")
    public String deleteSeguimiento(@PathVariable Long planeacionId,
                                    @PathVariable Long seguimientoId,
                                    RedirectAttributes redirectAttributes) {
        try {
            PlaneacionCultivo planeacion = getPlaneacionIfAuthorized(planeacionId);
            Optional<SeguimientoEtapa> seguimientoOptional = seguimientoEtapaService.getSeguimientoEtapaById(seguimientoId);

            if (seguimientoOptional.isEmpty() || !seguimientoOptional.get().getPlaneacion().getId().equals(planeacion.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Seguimiento de etapa no encontrado o no autorizado para eliminar.");
                return "redirect:/planeaciones/" + planeacionId + "/seguimientos";
            }

            seguimientoEtapaService.deleteSeguimientoEtapa(seguimientoId);
            redirectAttributes.addFlashAttribute("successMessage", "Seguimiento de etapa eliminado exitosamente!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el seguimiento de etapa: " + e.getMessage());
        }
        return "redirect:/planeaciones/" + planeacionId + "/seguimientos";
    }
}