package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario; 
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
import java.math.BigDecimal; 

@Controller
@RequestMapping("/tipos-cultivo")
public class TipoCultivoWebController {

    private final TipoCultivoService tipoCultivoService;
    private final UsuarioService usuarioService; 

    public TipoCultivoWebController(TipoCultivoService tipoCultivoService, UsuarioService usuarioService) { // Modificar constructor
        this.tipoCultivoService = tipoCultivoService;
        this.usuarioService = usuarioService;
    }

    // Método auxiliar para obtener el usuario autenticado 
    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en la base de datos."));
    }

    // Mostrar tipos de cultivo DEL USUARIO AUTENTICADO
    @GetMapping
    public String listTiposCultivo(Model model) {
        Usuario currentUser = getAuthenticatedUser();
        List<TipoCultivo> tiposCultivo = tipoCultivoService.findByUsuario(currentUser); // Obtener solo los del usuario
        model.addAttribute("tiposCultivo", tiposCultivo);
        return "tipos_cultivo/list-tipos";
    }

    // Mostrar formulario para crear un nuevo tipo de cultivo
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("tipoCultivo", new TipoCultivo());
        return "tipos_cultivo/tipo-form";
    }

    // Procesar el formulario de creación de tipo de cultivo
    @PostMapping
    public String saveTipoCultivo(@Valid @ModelAttribute("tipoCultivo") TipoCultivo tipoCultivo,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser(); // Obtener el usuario autenticado
        tipoCultivo.setUsuario(currentUser); // ASOCIAR EL TIPO DE CULTIVO AL USUARIO ACTUAL

        if (result.hasErrors()) {
            return "tipos_cultivo/tipo-form";
        }
        try {
            if (tipoCultivoService.getTipoCultivoByNombreAndUsuario(tipoCultivo.getNombre(), currentUser).isPresent()) {
                result.rejectValue("nombre", "duplicate.nombre", "Ya existe un tipo de cultivo con este nombre para tu usuario.");
                return "tipos_cultivo/tipo-form";
            }
            tipoCultivoService.saveTipoCultivo(tipoCultivo);
            redirectAttributes.addFlashAttribute("successMessage", "Tipo de cultivo guardado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el tipo de cultivo: " + e.getMessage());
            // En caso de error, si el formulario no se re-renderiza con el modelo, es posible que el error de DB
            // por unicidad muestre un mensaje genérico. La validación de arriba ayuda con eso.
        }
        return "redirect:/tipos-cultivo";
    }

    // Mostrar formulario para editar un tipo de cultivo existente
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser(); // Obtener el usuario autenticado
        Optional<TipoCultivo> tipoCultivoOptional = tipoCultivoService.getTipoCultivoByIdAndUsuario(id, currentUser); // Buscar por ID y usuario
        if (tipoCultivoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tipo de cultivo no encontrado o no autorizado.");
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
        Usuario currentUser = getAuthenticatedUser(); // Obtener el usuario autenticado

        if (result.hasErrors()) {
            tipoCultivoDetails.setId(id); // Asegura que el ID se mantenga para la vista del formulario
            return "tipos_cultivo/tipo-form";
        }
        try {
            
            Optional<TipoCultivo> existingTipoCultivoOptional = tipoCultivoService.getTipoCultivoByIdAndUsuario(id, currentUser);
            if (existingTipoCultivoOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tipo de cultivo no encontrado o no autorizado.");
                return "redirect:/tipos-cultivo";
            }
            TipoCultivo existingTipoCultivo = existingTipoCultivoOptional.get();

            // Opcional: Validar unicidad de nombre si el nombre cambió y es de otro tipo de cultivo del mismo usuario
            Optional<TipoCultivo> tipoCultivoByNombreAndUser = tipoCultivoService.getTipoCultivoByNombreAndUsuario(tipoCultivoDetails.getNombre(), currentUser);
            if (tipoCultivoByNombreAndUser.isPresent() && !tipoCultivoByNombreAndUser.get().getId().equals(id)) {
                result.rejectValue("nombre", "duplicate.nombre", "Ya existe otro tipo de cultivo con este nombre para tu usuario.");
                tipoCultivoDetails.setId(id); // Para que el formulario se re-renderice correctamente
                return "tipos_cultivo/tipo-form";
            }

            // Actualizar solo los campos que vienen del formulario
            existingTipoCultivo.setNombre(tipoCultivoDetails.getNombre());
            existingTipoCultivo.setDescripcion(tipoCultivoDetails.getDescripcion());
            existingTipoCultivo.setDensidadSiembraRecomendadaPorHa(tipoCultivoDetails.getDensidadSiembraRecomendadaPorHa());
            existingTipoCultivo.setDuracionDiasEstimada(tipoCultivoDetails.getDuracionDiasEstimada());
            existingTipoCultivo.setDistanciaSurco(tipoCultivoDetails.getDistanciaSurco());
            existingTipoCultivo.setDistanciaPlanta(tipoCultivoDetails.getDistanciaPlanta());

            tipoCultivoService.saveTipoCultivo(existingTipoCultivo); // Usar save para actualizar
            redirectAttributes.addFlashAttribute("successMessage", "Tipo de cultivo actualizado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el tipo de cultivo: " + e.getMessage());
        }
        return "redirect:/tipos-cultivo";
    }

    // Eliminar un tipo de cultivo
    @PostMapping("/delete/{id}")
    public String deleteTipoCultivo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser(); // Obtener el usuario autenticado
        try {
            // Verificar que el tipo de cultivo a eliminar pertenece al usuario
            Optional<TipoCultivo> tipoCultivoOptional = tipoCultivoService.getTipoCultivoByIdAndUsuario(id, currentUser);
            if (tipoCultivoOptional.isEmpty()) {
                throw new SecurityException("No autorizado para eliminar este tipo de cultivo.");
            }
            tipoCultivoService.deleteTipoCultivo(id);
            redirectAttributes.addFlashAttribute("successMessage", "Tipo de cultivo eliminado exitosamente!");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el tipo de cultivo: " + e.getMessage());
        }
        return "redirect:/tipos-cultivo";
    }
}