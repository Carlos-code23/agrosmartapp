package com.projectfinal.spring.agrosmart.agrosmart_application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "tipos_cultivo",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"nombre", "usuario_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoCultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100) 
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "densidad_siembra_recomendada_por_ha", precision = 10, scale = 2)
    private BigDecimal densidadSiembraRecomendadaPorHa;

    @Column(name = "duracion_dias_estimada")
    private Integer duracionDiasEstimada;

    @Column(name = "distancia_surco", precision = 5, scale = 2)

    private BigDecimal distanciaSurco;

    @Column(name = "distancia_planta", precision = 5, scale = 2)
    private BigDecimal distanciaPlanta;

    @OneToMany(mappedBy = "tipoCultivo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PlaneacionCultivo> planeacionesCultivo;

    public TipoCultivo(String nombre, String descripcion, BigDecimal densidadSiembraRecomendadaPorHa,
                       Integer duracionDiasEstimada, BigDecimal distanciaSurco, BigDecimal distanciaPlanta,
                       Usuario usuario) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.densidadSiembraRecomendadaPorHa = densidadSiembraRecomendadaPorHa;
        this.duracionDiasEstimada = duracionDiasEstimada;
        this.distanciaSurco = distanciaSurco;
        this.distanciaPlanta = distanciaPlanta;
        this.usuario = usuario;
    }
}