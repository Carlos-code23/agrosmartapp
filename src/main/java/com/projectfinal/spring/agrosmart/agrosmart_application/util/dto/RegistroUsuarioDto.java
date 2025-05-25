package com.projectfinal.spring.agrosmart.agrosmart_application.util.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsuarioDto {

    @NotBlank(message = "El nombre no puede estar vacío.")
    @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres.")
    private String nombre;

    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "Debe ser un formato de email válido.")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password;

    @NotBlank(message = "Debe confirmar la contraseña.")
    private String confirmPassword;

    // Puedes añadir otros campos si es necesario (ej. roles si los manejas en el registro)
}