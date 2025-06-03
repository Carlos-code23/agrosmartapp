package com.projectfinal.spring.agrosmart.agrosmart_application.util;

public enum UnidadMedida {
    KG("Kilogramos"),
    L("Litros"),
    UNIDAD("Unidad(es)"),
    M2("Metros Cuadrados"),
    M3("Metros Cúbicos"),
    GR("Gramos"),
    ML("Mililitros"),
    SACO("Saco(s)"),
    BOTELLA("Botella(s)"),
    CANECAS("Caneca(s)"),
    BULTO("Bulto(s)");

    private final String descripcion;

    UnidadMedida(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
