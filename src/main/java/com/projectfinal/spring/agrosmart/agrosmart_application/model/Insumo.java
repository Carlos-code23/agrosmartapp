package com.projectfinal.spring.agrosmart.agrosmart_application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "insumos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(length = 100)
    private String tipo; // Ej. 'Fertilizante', 'Semilla'

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida; // Ej. 'kg', 'litros'

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Relaciones: Un Insumo puede estar en muchas InsumosPlaneacion
    @OneToMany(mappedBy = "insumo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<InsumoPlaneacion> insumosPlaneacion;
}