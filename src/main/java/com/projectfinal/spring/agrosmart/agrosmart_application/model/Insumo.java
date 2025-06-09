package com.projectfinal.spring.agrosmart.agrosmart_application.model; 

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;

import com.projectfinal.spring.agrosmart.agrosmart_application.util.TipoInsumo;

import lombok.Data; 

import java.math.BigDecimal;

@Entity
@Table(name = "insumos")
@Data 
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del insumo es requerido")
    @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres")
    @Column(nullable = false, length = 255)
    private String nombre;

    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private TipoInsumo tipo;

    @Size(max = 255, message = "El proveedor no puede exceder los 255 caracteres")
    @Column(name = "proveedor", length = 255) 
    private String proveedor;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida; 

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor que cero")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario; 

    @Column(columnDefinition = "TEXT")
    private String descripcion; 

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false) 
    private Usuario usuario; 

    // Relaciones: Un Insumo puede estar en muchas InsumosPlaneacion
    @OneToMany(mappedBy = "insumo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<InsumoPlaneacion> insumosPlaneacion = new ArrayList<>();

    public void addInsumoPlaneacion(InsumoPlaneacion insumoPlaneacion) {
        insumosPlaneacion.add(insumoPlaneacion);
        insumoPlaneacion.setInsumo(this);
    }

    public void removeInsumoPlaneacion(InsumoPlaneacion insumoPlaneacion) {
        insumosPlaneacion.remove(insumoPlaneacion);
        insumoPlaneacion.setInsumo(null);
    }
}