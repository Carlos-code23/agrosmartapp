package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.EtapaCultivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EtapaCultivoService {

    private final EtapaCultivoRepository etapaCultivoRepository;

    public EtapaCultivoService(EtapaCultivoRepository etapaCultivoRepository) {
        this.etapaCultivoRepository = etapaCultivoRepository;
    }

    @Transactional
    public EtapaCultivo saveEtapaCultivo(EtapaCultivo etapaCultivo) {
        return etapaCultivoRepository.save(etapaCultivo);
    }

    @Transactional(readOnly = true)
    public Optional<EtapaCultivo> getEtapaCultivoById(Long id) {
        return etapaCultivoRepository.findById(id);
    }

    // Método para obtener etapas por usuario (todas las que le pertenecen)
    @Transactional(readOnly = true)
    public List<EtapaCultivo> findByUsuario(Usuario usuario) {
        return etapaCultivoRepository.findByUsuario(usuario);
    }

    // Método seguro para obtener una etapa por ID y usuario
    @Transactional(readOnly = true)
    public Optional<EtapaCultivo> getEtapaCultivoByIdAndUsuario(Long id, Usuario usuario) {
        return etapaCultivoRepository.findByIdAndUsuario(id, usuario);
    }

    @Transactional
    public void deleteEtapaCultivo(Long id, Usuario currentUser) {
        EtapaCultivo etapa = etapaCultivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Etapa de cultivo no encontrada con ID: " + id));

        if (!etapa.getUsuario().getId().equals(currentUser.getId())) {
            throw new SecurityException("No tiene permiso para eliminar esta etapa de cultivo.");
        }
        etapaCultivoRepository.deleteById(id);
    }

    // Método para encontrar una etapa por nombre y usuario
    @Transactional(readOnly = true)
    public Optional<EtapaCultivo> findByNombreAndUsuario(String nombre, Usuario usuario) {
        return etapaCultivoRepository.findByNombreAndUsuario(nombre, usuario);
    }
}