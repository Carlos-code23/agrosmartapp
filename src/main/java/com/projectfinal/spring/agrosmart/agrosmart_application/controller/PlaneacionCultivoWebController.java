package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Parcela;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.EtapaCultivoService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.ParcelaService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.PlaneacionCultivoService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.TipoCultivoService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.UsuarioService;
import com.projectfinal.spring.agrosmart.agrosmart_application.util.EstadoPlaneacion; // Importamos el Enum
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
import java.util.Arrays; // Necesario para Arrays.asList si se usa

@Controller
@RequestMapping("/planeaciones")
public class PlaneacionCultivoWebController {

    private final PlaneacionCultivoService planeacionCultivoService;
    private final ParcelaService parcelaService;
    private final TipoCultivoService tipoCultivoService;
    private final UsuarioService usuarioService;
    private final EtapaCultivoService etapaCultivoService; // Inyectar el servicio de etapas

    public PlaneacionCultivoWebController(PlaneacionCultivoService planeacionCultivoService,
                                          ParcelaService parcelaService,
                                          TipoCultivoService tipoCultivoService,
                                          UsuarioService usuarioService,
                                          EtapaCultivoService etapaCultivoService) { // Añadir al constructor
        this.planeacionCultivoService = planeacionCultivoService;
        this.parcelaService = parcelaService;
        this.tipoCultivoService = tipoCultivoService;
        this.usuarioService = usuarioService;
        this.etapaCultivoService = etapaCultivoService; // Asignar
    }

    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en la base de datos."));
    }

    // --- LISTAR PLANEACIONES DE CULTIVO ---
    @GetMapping
    public String listPlaneaciones(Model model) {
        Usuario currentUser = getAuthenticatedUser();
        List<PlaneacionCultivo> planeaciones = planeacionCultivoService.findByUsuario(currentUser);
        model.addAttribute("planeaciones", planeaciones);
        return "planeaciones/list-planeaciones";
    }

    // --- MOSTRAR FORMULARIO DE CREACIÓN/EDICIÓN ---
    @GetMapping({"/new", "/edit/{id}"})
    public String showForm(@PathVariable(required = false) Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();

        // Cargar las parcelas del usuario actual
        List<Parcela> parcelas = parcelaService.findByUsuario(currentUser);
        // Cargar los tipos de cultivo disponibles (asumo que son globales o que también tienes un findByUsuario para ellos)
        List<TipoCultivo> tiposCultivo = tipoCultivoService.getAllTiposCultivo();
        // Cargar las etapas de cultivo disponibles para el usuario actual
        List<EtapaCultivo> etapasCultivo = etapaCultivoService.findByUsuario(currentUser);

        // --- VALIDACIONES PREVIAS PARA EVITAR FORMULARIOS VACÍOS ---
        if (parcelas.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe registrar al menos una parcela antes de crear una planeación.");
            return "redirect:/parcelas/new"; // Redirigir para crear parcela
        }
        if (tiposCultivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No hay tipos de cultivo registrados. Contacte al administrador.");
            return "redirect:/tipos-cultivo"; // O a una página de error
        }
        if (etapasCultivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No hay etapas de cultivo registradas. Contacte al administrador o cree una.");
            return "redirect:/etapas/new"; // Redirigir para crear etapa si no hay
        }
        // --- FIN VALIDACIONES PREVIAS ---

        PlaneacionCultivo planeacionCultivo;
        if (id != null) {
            // Buscar la planeación y asegurarse de que pertenece al usuario
            Optional<PlaneacionCultivo> optionalPlaneacion = planeacionCultivoService.getPlaneacionCultivoByIdAndUsuario(id, currentUser);
            if (optionalPlaneacion.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Planeación de cultivo no encontrada o no autorizada.");
                return "redirect:/planeaciones";
            }
            planeacionCultivo = optionalPlaneacion.get();
        } else {
            planeacionCultivo = new PlaneacionCultivo();
            // Opcional: Si quieres un estado por defecto inicial en el formulario para nuevas planeaciones
            // planeacionCultivo.setEstado(EstadoPlaneacion.PENDIENTE);
        }

        model.addAttribute("planeacionCultivo", planeacionCultivo);
        model.addAttribute("parcelas", parcelas);
        model.addAttribute("tiposCultivo", tiposCultivo);
        model.addAttribute("etapasCultivo", etapasCultivo); // Añadir las etapas al modelo
        model.addAttribute("estadosPlaneacion", EstadoPlaneacion.values()); // <--- ¡NUEVO! Pasar todos los valores del Enum EstadoPlaneacion
        return "planeaciones/planeacion-form";
    }

    // --- PROCESAR FORMULARIO (GUARDAR/ACTUALIZAR PLANEACIÓN) ---
    @PostMapping
    public String savePlaneacion(@Valid @ModelAttribute("planeacionCultivo") PlaneacionCultivo planeacionCultivo,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        Usuario currentUser = getAuthenticatedUser();
        planeacionCultivo.setUsuario(currentUser); // Asociar la planeación al usuario actual

        // Recargar las listas para el formulario en caso de error de validación
        List<Parcela> parcelas = parcelaService.findByUsuario(currentUser);
        List<TipoCultivo> tiposCultivo = tipoCultivoService.getAllTiposCultivo();
        List<EtapaCultivo> etapasCultivo = etapaCultivoService.findByUsuario(currentUser); // Recargar etapas

        // --- VALIDACIONES DE RELACIONES ---
        // Se mueven las validaciones de las relaciones (parcela, tipoCultivo, etapaCultivo)
        // DENTRO del bloque 'if (!result.hasErrors())' para que se ejecuten solo si las validaciones básicas de campos del modelo pasan.
        // Y se incluyen en el bloque 'result.hasErrors()' de retorno si fallan.

        // NOTA: Si el campo 'estado' en PlaneacionCultivo tiene @NotNull o similar,
        // y quieres que el valor por defecto 'PENDIENTE' se establezca SIEMPRE que no venga del formulario,
        // la lógica del @PrePersist es la más robusta. Si lo manejas aquí, asegúrate de que result.hasErrors()
        // no falle por un estado null antes de que lo setees.

        if (!result.hasErrors()) {
            // Asegurarse de que la parcela seleccionada pertenece al usuario
            Optional<Parcela> selectedParcela = parcelas.stream()
                    .filter(p -> p.getId().equals(planeacionCultivo.getParcela().getId()))
                    .findFirst();
            if (selectedParcela.isEmpty()) {
                result.rejectValue("parcela", "invalid.parcela", "La parcela seleccionada no es válida o no le pertenece.");
            } else {
                planeacionCultivo.setParcela(selectedParcela.get());
            }

            // Asegurarse de que el tipo de cultivo seleccionado es válido
            Optional<TipoCultivo> selectedTipoCultivo = tiposCultivo.stream()
                    .filter(tc -> tc.getId().equals(planeacionCultivo.getTipoCultivo().getId()))
                    .findFirst();
            if (selectedTipoCultivo.isEmpty()) {
                result.rejectValue("tipoCultivo", "invalid.tipoCultivo", "El tipo de cultivo seleccionado no es válido.");
            } else {
                planeacionCultivo.setTipoCultivo(selectedTipoCultivo.get());
            }

            // Asegurarse de que la etapa de cultivo seleccionada es válida y pertenece al usuario
            Optional<EtapaCultivo> selectedEtapa = etapasCultivo.stream() // Usar las etapas del usuario
                    .filter(ec -> ec.getId().equals(planeacionCultivo.getEtapaCultivo().getId()))
                    .findFirst();
            if (selectedEtapa.isEmpty()) {
                result.rejectValue("etapaCultivo", "invalid.etapaCultivo", "La etapa de cultivo seleccionada no es válida o no le pertenece.");
            } else {
                planeacionCultivo.setEtapaCultivo(selectedEtapa.get()); // Asignar la entidad completa
            }

            // Ya no necesitas un @RequestParam para el estado si usas th:field="*{estado}"
            // Spring Binder se encarga de mapear automáticamente el string del Enum.
            // Si el campo 'estado' en el modelo puede ser null y el @PrePersist lo maneja,
            // no necesitas establecerlo aquí a menos que quieras una lógica condicional específica
            // basada en la entrada del formulario (ej. un default diferente).
            // Si no se selecciona nada y no hay @PrePersist, `planeacionCultivo.getEstado()` será null.
            // Si tu @PrePersist lo gestiona, esta parte es opcional aquí.
            // if (planeacionCultivo.getEstado() == null) {
            //     planeacionCultivo.setEstado(EstadoPlaneacion.PENDIENTE);
            // }
        }

        if (result.hasErrors()) {
            // Asegúrate de pasar todos los atributos necesarios para renderizar el formulario correctamente
            model.addAttribute("parcelas", parcelas);
            model.addAttribute("tiposCultivo", tiposCultivo);
            model.addAttribute("etapasCultivo", etapasCultivo); // Añadir etapas al modelo en caso de error
            model.addAttribute("estadosPlaneacion", EstadoPlaneacion.values()); // <--- ¡NUEVO! Re-pasar los valores del Enum
            return "planeaciones/planeacion-form";
        }

        try {
            planeacionCultivoService.savePlaneacionCultivo(planeacionCultivo);
            redirectAttributes.addFlashAttribute("successMessage", "Planeación de cultivo guardada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar la planeación de cultivo: " + e.getMessage());
            // Recargar las listas si hay un error de servicio y se vuelve al formulario
            model.addAttribute("parcelas", parcelas);
            model.addAttribute("tiposCultivo", tiposCultivo);
            model.addAttribute("etapasCultivo", etapasCultivo);
            model.addAttribute("estadosPlaneacion", EstadoPlaneacion.values()); // <--- ¡NUEVO! Re-pasar los valores del Enum
            return "planeaciones/planeacion-form";
        }
        return "redirect:/planeaciones";
    }

    // --- ELIMINAR PLANEACIÓN DE CULTIVO ---
    @PostMapping("/delete/{id}")
    public String deletePlaneacion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        try {
            planeacionCultivoService.deletePlaneacionCultivo(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Planeación de cultivo eliminada exitosamente!");
        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la planeación de cultivo: " + e.getMessage());
        }
        return "redirect:/planeaciones";
    }
}