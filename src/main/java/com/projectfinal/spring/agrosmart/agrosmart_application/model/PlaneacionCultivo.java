package com.projectfinal.spring.agrosmart.agrosmart_application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "planeaciones_cultivo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaneacionCultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcela_id", nullable = false)
    private Parcela parcela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_cultivo_id", nullable = false)
    private TipoCultivo tipoCultivo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio; // Mapea a DATE

    @Column(name = "fecha_fin_estimada")
    private LocalDate fechaFinEstimada; // Mapea a DATE

    @Column(name = "numero_semillas", precision = 15, scale = 2)
    private BigDecimal numeroSemillas; // Campo calculado

    @Column(name = "estimacion_costo", precision = 10, scale = 2)
    private BigDecimal estimacionCosto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 50)
    private String estado; // Ej. 'Planificado', 'En Progreso', 'Cosechado'

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relaciones: Una PlaneacionCultivo tiene muchos SeguimientosEtapa, Siembras e InsumosPlaneacion
    @OneToMany(mappedBy = "planeacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<SeguimientoEtapa> seguimientosEtapa;

    @OneToMany(mappedBy = "planeacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Siembra> siembras;

    @OneToMany(mappedBy = "planeacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<InsumoPlaneacion> insumosPlaneacion;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.estado == null || this.estado.isEmpty()) {
            this.estado = "Planificado"; // Valor por defecto
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}