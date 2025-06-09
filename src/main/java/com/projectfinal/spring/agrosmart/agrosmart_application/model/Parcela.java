package com.projectfinal.spring.agrosmart.agrosmart_application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parcelas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Parcela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "usuario_id", nullable = false) 
    private Usuario usuario; 

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(length = 255)
    private String ubicacion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tamano;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida;

    @Column(columnDefinition = "TEXT") 
    private String descripcion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "parcela", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<PlaneacionCultivo> planeacionesCultivo;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}