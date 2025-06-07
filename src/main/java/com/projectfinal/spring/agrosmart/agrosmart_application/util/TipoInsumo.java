package com.projectfinal.spring.agrosmart.agrosmart_application.util;

public enum TipoInsumo {
    SEMILLAS("Semillas"),
    FERTILIZANTES("Fertilizantes"),
    PESTICIDAS("Pesticidas"),
    HERBICIDAS("Herbicidas");

    private final String displayValue;

    TipoInsumo(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}