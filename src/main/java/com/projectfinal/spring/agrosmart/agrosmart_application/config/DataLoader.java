package com.projectfinal.spring.agrosmart.agrosmart_application.config;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.EtapaCultivoRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder; // Para encriptar la contraseña del usuario por defecto

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DataLoader implements CommandLineRunner {

    private final EtapaCultivoRepository etapaCultivoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // Inyectar PasswordEncoder

    public DataLoader(EtapaCultivoRepository etapaCultivoRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.etapaCultivoRepository = etapaCultivoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional // Asegura que la operación sea transaccional
    public void run(String... args) throws Exception {
        // Asegúrate de tener al menos un usuario en la base de datos para asignar las etapas.
        // Si no tienes un mecanismo de registro de usuarios, puedes crear uno por defecto aquí.
        // Esto es para propósitos de demostración/desarrollo.
        Optional<Usuario> defaultUserOptional = usuarioRepository.findByEmail("admin@agrosmart.com");
        Usuario defaultUser;

        if (defaultUserOptional.isEmpty()) {
            defaultUser = new Usuario();
            defaultUser.setNombre("Admin");
            defaultUser.setEmail("admin@agrosmart.com");
            defaultUser.setPassword(passwordEncoder.encode("password")); // ¡Encripta la contraseña!
            defaultUser.setCreatedAt(LocalDateTime.now());
            usuarioRepository.save(defaultUser);
            System.out.println("Usuario por defecto 'admin@agrosmart.com' creado.");
        } else {
            defaultUser = defaultUserOptional.get();
        }

        // Definir las etapas por defecto
        String[] defaultEtapaNombres = {"Siembra", "Mantenimiento", "Cosecha"};
        String[] defaultEtapaDescripciones = {
            "Inicio del ciclo de cultivo, preparación del terreno y plantación.",
            "Cuidados continuos del cultivo: riego, fertilización, control de plagas.",
            "Recolección de la producción agrícola."
        };
        Integer[] defaultDuracionDias = {30, 90, 15}; // Duraciones de ejemplo

        for (int i = 0; i < defaultEtapaNombres.length; i++) {
            String nombreEtapa = defaultEtapaNombres[i];
            String descripcionEtapa = defaultEtapaDescripciones[i];
            Integer duracion = defaultDuracionDias[i];

            // Verificar si la etapa ya existe para este usuario
            Optional<EtapaCultivo> existingEtapa = etapaCultivoRepository.findByNombreAndUsuario(nombreEtapa, defaultUser);
            if (existingEtapa.isEmpty()) {
                EtapaCultivo etapa = new EtapaCultivo(nombreEtapa, descripcionEtapa, duracion, defaultUser);
                etapaCultivoRepository.save(etapa);
                System.out.println("Etapa por defecto '" + nombreEtapa + "' creada para el usuario: " + defaultUser.getEmail());
            }
        }
    }
}
