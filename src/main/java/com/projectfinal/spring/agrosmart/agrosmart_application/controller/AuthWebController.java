package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.service.UsuarioService;
import com.projectfinal.spring.agrosmart.agrosmart_application.util.dto.RegistroUsuarioDto;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth") // Todas las rutas en este controlador comenzarán con /auth
public class AuthWebController {

    private final UsuarioService usuarioService;

    public AuthWebController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Muestra el formulario de registro
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("usuario", new RegistroUsuarioDto()); // Añade un objeto vacío para el formulario
        return "auth/register"; // Resuelve a src/main/resources/templates/auth/register.html
    }

    // Procesa el envío del formulario de registro
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("usuario") RegistroUsuarioDto registroDto,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        // Validar si las contraseñas coinciden
        if (!registroDto.getPassword().equals(registroDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch", "Las contraseñas no coinciden.");
        }

        // Validar si el email ya existe
        if (usuarioService.findByEmail(registroDto.getEmail()).isPresent()) {
            result.rejectValue("email", "email.duplicate", "Ya existe un usuario con este email.");
        }

        // Si hay errores de validación, vuelve al formulario
        if (result.hasErrors()) {
            return "auth/register";
        }

        // Crear objeto Usuario desde el DTO
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(registroDto.getNombre());
        nuevoUsuario.setEmail(registroDto.getEmail());
        nuevoUsuario.setPassword(registroDto.getPassword()); // La contraseña se encriptará en el servicio

        try {
            usuarioService.saveUsuario(nuevoUsuario);
            redirectAttributes.addFlashAttribute("successMessage", "¡Registro exitoso! Por favor, inicia sesión.");
            return "redirect:/auth/login"; // Redirige a la página de login
        } catch (Exception e) {
            // Manejo de errores genéricos (ej. problemas de base de datos)
            redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar el usuario: " + e.getMessage());
            return "redirect:/auth/register"; // Vuelve al formulario de registro con error
        }
    }

    // Muestra el formulario de inicio de sesión
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login"; // Resuelve a src/main/resources/templates/auth/login.html
    }
}