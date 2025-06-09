package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo; 
import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;  
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; 
import java.util.List;
import java.util.Optional;

@Service
@Transactional 
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EtapaCultivoService etapaCultivoService; 
    private final TipoCultivoService tipoCultivoService;   

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          EtapaCultivoService etapaCultivoService,
                          TipoCultivoService tipoCultivoService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.etapaCultivoService = etapaCultivoService;
        this.tipoCultivoService = tipoCultivoService;
    }

    public Usuario saveUsuario(Usuario usuario) {

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        Usuario savedUser = usuarioRepository.save(usuario);

        // --- crear Etapas de Cultivo por defecto para el nuevo usuario ---
        EtapaCultivo etapaSiembra = new EtapaCultivo("Siembra", "Inicio del ciclo de cultivo, preparación del terreno y plantación.", 30, savedUser);
        etapaCultivoService.saveEtapaCultivo(etapaSiembra);

        EtapaCultivo etapaMantenimiento = new EtapaCultivo("Mantenimiento", "Cuidados continuos del cultivo: riego, fertilización, control de plagas.", 90, savedUser);
        etapaCultivoService.saveEtapaCultivo(etapaMantenimiento);

        EtapaCultivo etapaCosecha = new EtapaCultivo("Cosecha", "Recolección de la producción agrícola.", 15, savedUser);
        etapaCultivoService.saveEtapaCultivo(etapaCosecha);


        // --- crear Tipos de Cultivo por defecto para el nuevo usuario ---
        TipoCultivo tipoCafe = new TipoCultivo(
            "Café",
            "Cultivo de café, ideal para regiones tropicales.",
            new BigDecimal("10000.00"),
            365,
            new BigDecimal("1.5"),
            new BigDecimal("1.0"),
            savedUser
        );
        tipoCultivoService.saveTipoCultivo(tipoCafe);

        TipoCultivo tipoMaiz = new TipoCultivo(
            "Maíz",
            "Cultivo de maíz, base alimenticia.",
            new BigDecimal("80000.00"),
            120,
            new BigDecimal("0.8"),
            new BigDecimal("0.3"),
            savedUser
        );
        tipoCultivoService.saveTipoCultivo(tipoMaiz);

        TipoCultivo tipoCacao = new TipoCultivo(
            "Cacao",
            "cultivo de cacao, produccion de chocolate",
            new BigDecimal("1000.00"),
            182,
            new BigDecimal("3.0"),
            new BigDecimal("3.0"),
            savedUser
        );
        tipoCultivoService.saveTipoCultivo(tipoCacao);


        return savedUser; 
    }

    public Usuario updateUsuario(Long id, Usuario usuarioDetails) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setNombre(usuarioDetails.getNombre());
        usuario.setEmail(usuarioDetails.getEmail());
        
        return usuarioRepository.save(usuario);
    }

    public void updatePassword(Long userId, String newPassword) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}