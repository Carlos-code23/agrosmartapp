package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.UsuarioService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UsuarioService usuarioService;

    public DashboardController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String showDashboard(Model model) {
        // Obtener el usuario autenticado de Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // El nombre de usuario (email)

        // Buscar el usuario completo en tu base de datos
        Optional<Usuario> usuarioOptional = usuarioService.findByEmail(username);

        if (usuarioOptional.isPresent()) {
            model.addAttribute("usuario", usuarioOptional.get());
            // TODO: Aquí puedes añadir más datos al modelo para el dashboard:
            // - Listado de parcelas del usuario: model.addAttribute("parcelas", parcelaService.getParcelasByUsuarioId(usuario.getId()));
            // - Listado de planeaciones del usuario: model.addAttribute("planeaciones", planeacionCultivoService.getPlaneacionesByUsuarioId(usuario.getId()));
        } else {
            // Esto no debería pasar si el usuario está autenticado, pero es un fallback
            model.addAttribute("usuario", new Usuario()); // Objeto vacío para evitar NullPointerException en Thymeleaf
            model.addAttribute("errorMessage", "No se pudo cargar la información del usuario autenticado.");
        }

        return "dashboard"; // Resuelve a src/main/resources/templates/dashboard.html
    }
}