package com.tfg.mentoring.model.auxiliar.enums;

import java.util.HashMap;

//https://codingexplained.com/coding/java/enum-to-integer-and-integer-to-enum
public enum FasesMentorizacion {
	NACIMIENTO(0), COMPROMISO(1), SOSTENIMIENTO(2), FIN(3);
	
	
	private int value;
    private static HashMap<Integer, FasesMentorizacion> map = new HashMap<>();

    private FasesMentorizacion(int value) {
        this.value = value;
    }

    static {
        for (FasesMentorizacion fase : FasesMentorizacion.values()) {
            map.put(fase.value, fase);
        }
    }

    public static FasesMentorizacion valueOf(int fase) {
        return (FasesMentorizacion) map.get(fase);
    }

    public int getValue() {
        return value;
    }
}
