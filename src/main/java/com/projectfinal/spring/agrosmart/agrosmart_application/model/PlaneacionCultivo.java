package com.projectfinal.spring.agrosmart.agrosmart_application.model;

import com.projectfinal.spring.agrosmart.agrosmart_application.util.EstadoPlaneacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
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

    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "etapa_cultivo_id", nullable = false) 
    private EtapaCultivo etapaCultivo; 

    @Column(nullable = false) 
    private String nombre;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio; 

    @Column(name = "fecha_fin_estimada")
    private LocalDate fechaFinEstimada; 

    @Column(name = "numero_semillas", precision = 15, scale = 2)
    private BigDecimal numeroSemillas; 

    @Column(name = "estimacion_costo", precision = 10, scale = 2)
    private BigDecimal estimacionCosto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING) 
    @Column(nullable = false) 
    private EstadoPlaneacion estado; 

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "planeacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InsumoPlaneacion> insumosPlaneacion;
    

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        
        if (this.estado == null) { 
            this.estado = EstadoPlaneacion.PENDIENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}