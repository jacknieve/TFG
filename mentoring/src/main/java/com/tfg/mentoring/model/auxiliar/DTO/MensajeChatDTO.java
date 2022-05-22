package com.tfg.mentoring.model.auxiliar.DTO;

import com.tfg.mentoring.model.MensajeChat;
import com.tfg.mentoring.model.auxiliar.CuerpoMensaje;

public class MensajeChatDTO implements CuerpoMensaje{

	private String contenido;
	private boolean deMentor;
	private long sala;
	
	public MensajeChatDTO(String contenido, boolean deMentor, long sala) {
		super();
		this.contenido = contenido;
		this.deMentor = deMentor;
		this.sala = sala;
	}
	
	public MensajeChatDTO(MensajeChat m) {
		super();
		this.contenido = m.getContenido();
		this.deMentor = m.getId().isDeMentor();
		this.sala = m.getId().getId();
		
	}


	public MensajeChatDTO() {
		super();
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	public boolean isDeMentor() {
		return deMentor;
	}

	public void setDeMentor(boolean deMentor) {
		this.deMentor = deMentor;
	}

	

	public long getSala() {
		return sala;
	}

	public void setSala(long sala) {
		this.sala = sala;
	}

	@Override
	public String toString() {
		return "MensajeChatDTO [contenido=" + contenido + ", deMentor=" + deMentor + ", sala=" + sala + "]";
	}

	

	
	
	
}
