package com.projectfinal.spring.agrosmart.agrosmart_application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "tipos_cultivo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoCultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "densidad_siembra_recomendada_por_ha", precision = 10, scale = 2)
    private BigDecimal densidadSiembraRecomendadaPorHa;

    @Column(name = "duracion_dias_estimada")
    private Integer duracionDiasEstimada;

    @Column(name = "distancia_surco", precision = 5, scale = 2) // Distancia entre surcos en metros
    private BigDecimal distanciaSurco;

    @Column(name = "distancia_planta", precision = 5, scale = 2) // Distancia entre plantas en metros
    private BigDecimal distanciaPlanta;

    // Relaciones: Un TipoCultivo puede tener muchas PlaneacionesCultivo y muchas EtapasCultivo
    @OneToMany(mappedBy = "tipoCultivo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PlaneacionCultivo> planeacionesCultivo;

}