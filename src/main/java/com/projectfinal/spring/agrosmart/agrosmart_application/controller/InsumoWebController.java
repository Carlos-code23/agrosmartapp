package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Insumo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.InsumoService;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.UsuarioService;
import com.projectfinal.spring.agrosmart.agrosmart_application.util.UnidadMedida; // ¡Importa la nueva clase!
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays; // Necesario para Arrays.asList()
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        String userEmail = authentication.getName();
        return usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en la base de datos."));
    }

    // Listar todos los insumos del usuario autenticado
    @GetMapping
    public String listInsumos(Model model) {
        Usuario currentUser = getAuthenticatedUser();
        List<Insumo> insumos = insumoService.findByUsuario(currentUser); // Obtiene solo los insumos del usuario
        model.addAttribute("insumos", insumos);
        return "insumos/list-insumos";
    }

    // Mostrar formulario para añadir un nuevo insumo
    @GetMapping("/new")
    public String showAddInsumoForm(Model model) {
        model.addAttribute("insumo", new Insumo());
        // Añadir las unidades de medida sugeridas al modelo
        model.addAttribute("unidadesMedida", Arrays.asList(UnidadMedida.values()));
        return "insumos/insumo-form";
    }

    // Mostrar formulario para editar un insumo existente
    @GetMapping("/edit/{id}")
    public String showEditInsumoForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        // Buscar el insumo y asegurarse de que pertenece al usuario
        Optional<Insumo> insumoOptional = insumoService.getInsumoByIdAndUsuario(id, currentUser);

        if (insumoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Insumo no encontrado o no autorizado.");
            return "redirect:/insumos";
        }
        model.addAttribute("insumo", insumoOptional.get());
        // Añadir las unidades de medida sugeridas al modelo
        model.addAttribute("unidadesMedida", Arrays.asList(UnidadMedida.values()));
        return "insumos/insumo-form";
    }

    // Procesar el formulario de guardado (creación o actualización)
    @PostMapping
    public String saveInsumo(@Valid @ModelAttribute("insumo") Insumo insumo,
                         BindingResult result,
                         @RequestParam(value = "unidadMedidaSelect", required = false) String unidadMedidaSelect,
                         @RequestParam(value = "unidadMedidaCustom", required = false) String unidadMedidaCustom,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        Usuario currentUser = getAuthenticatedUser();
        insumo.setUsuario(currentUser);

        boolean unidadMedidaSet = false; // Bandera para saber si se asignó la unidad de medida

        if ("OTRO".equals(unidadMedidaSelect) && unidadMedidaCustom != null && !unidadMedidaCustom.trim().isEmpty()) {
            insumo.setUnidadMedida(unidadMedidaCustom.trim());
            unidadMedidaSet = true;
        } else if (unidadMedidaSelect != null && !unidadMedidaSelect.isEmpty() && !"OTRO".equals(unidadMedidaSelect)) {
            try {
                UnidadMedida selectedEnum = UnidadMedida.valueOf(unidadMedidaSelect); // Convierte "L" a UnidadMedida.L
                insumo.setUnidadMedida(selectedEnum.getDescripcion()); // Guarda "Litros"
                unidadMedidaSet = true;
            } catch (IllegalArgumentException e) {
                // Esto maneja si el valor del select no es un Enum válido
                result.rejectValue("unidadMedida", "invalid.unidadMedida", "La unidad de medida seleccionada no es válida.");
            }
        }   

        // Validación manual: Si la unidad de medida no fue establecida por las condiciones anteriores
        if (!unidadMedidaSet || insumo.getUnidadMedida().trim().isEmpty()) {
            result.rejectValue("unidadMedida", "required.unidadMedida", "La unidad de medida es obligatoria.");
        }


        // Ahora, si hay errores de validación (incluyendo el nuestro si lo añadimos)
        if (result.hasErrors()) {
            model.addAttribute("unidadesMedida", Arrays.asList(UnidadMedida.values()));
            // Para mantener la selección del usuario si hay errores
            model.addAttribute("selectedUnidadMedida", unidadMedidaSelect);
            model.addAttribute("customUnidadMedidaValue", unidadMedidaCustom);
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

    // Eliminar un insumo
    @PostMapping("/delete/{id}")
    public String deleteInsumo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario currentUser = getAuthenticatedUser();
        try {
            // Eliminar el insumo solo si pertenece al usuario autenticado
            insumoService.deleteInsumoByIdAndUsuario(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Insumo eliminado exitosamente!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el insumo: " + e.getMessage());
        }
        return "redirect:/insumos";
    }
}