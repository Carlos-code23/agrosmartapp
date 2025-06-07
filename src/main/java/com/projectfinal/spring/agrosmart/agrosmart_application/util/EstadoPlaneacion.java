package com.projectfinal.spring.agrosmart.agrosmart_application.util;

public enum EstadoPlaneacion {
    PENDIENTE("Pendiente"),
    EN_CURSO("En Curso"),
    COMPLETADO("Completado");

    private final String displayValue;

    EstadoPlaneacion(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
