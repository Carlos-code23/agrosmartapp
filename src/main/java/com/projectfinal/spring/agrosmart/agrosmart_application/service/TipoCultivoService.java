package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.TipoCultivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TipoCultivoService {

    private final TipoCultivoRepository tipoCultivoRepository;

    public TipoCultivoService(TipoCultivoRepository tipoCultivoRepository) {
        this.tipoCultivoRepository = tipoCultivoRepository;
    }

    public TipoCultivo saveTipoCultivo(TipoCultivo tipoCultivo) {
        return tipoCultivoRepository.save(tipoCultivo);
    }

    @Transactional(readOnly = true)
    public Optional<TipoCultivo> getTipoCultivoById(Long id) {
        return tipoCultivoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<TipoCultivo> findByUsuario(Usuario usuario) {
        return tipoCultivoRepository.findByUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public Optional<TipoCultivo> getTipoCultivoByIdAndUsuario(Long id, Usuario usuario) {
        return tipoCultivoRepository.findByIdAndUsuario(id, usuario);
    }


    @Transactional(readOnly = true)
    public List<TipoCultivo> getAllTiposCultivo() {
        return tipoCultivoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<TipoCultivo> getTipoCultivoByNombre(String nombre) {
        return tipoCultivoRepository.findByNombre(nombre);
    }

    @Transactional(readOnly = true)
    public Optional<TipoCultivo> getTipoCultivoByNombreAndUsuario(String nombre, Usuario usuario) {
        return tipoCultivoRepository.findByNombreAndUsuario(nombre, usuario);
    }

    public TipoCultivo updateTipoCultivo(Long id, TipoCultivo tipoCultivoDetails) {
        TipoCultivo tipoCultivo = tipoCultivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de cultivo no encontrado con ID: " + id));

        tipoCultivo.setNombre(tipoCultivoDetails.getNombre());
        tipoCultivo.setDescripcion(tipoCultivoDetails.getDescripcion());
        tipoCultivo.setDensidadSiembraRecomendadaPorHa(tipoCultivoDetails.getDensidadSiembraRecomendadaPorHa());
        tipoCultivo.setDuracionDiasEstimada(tipoCultivoDetails.getDuracionDiasEstimada());
        tipoCultivo.setDistanciaSurco(tipoCultivoDetails.getDistanciaSurco());
        tipoCultivo.setDistanciaPlanta(tipoCultivoDetails.getDistanciaPlanta());

        return tipoCultivoRepository.save(tipoCultivo);
    }

    public void deleteTipoCultivo(Long id) {
        tipoCultivoRepository.deleteById(id);
    }
}