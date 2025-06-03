package com.projectfinal.spring.agrosmart.agrosmart_application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "etapas_cultivo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EtapaCultivo {

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

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    // Constructor para las etapas predefinidas, sin ID inicial
    public EtapaCultivo(String nombre, String descripcion, Integer duracionDias, Usuario usuario) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionDias = duracionDias;
        this.usuario = usuario;
    }

}