package com.tfg.mentoring.model.auxiliar.requests;


//Clase auxiliar que almacena los mensajes que envia el frontend al backend para ser enviados a otro usuario
public class MensajeReenvio {
	private String contenido;
	private String emisor;
	private String receptor;
	private boolean deMentor;
	private long id;
	
	public MensajeReenvio(String contenido, String emisor, String receptor, long id, boolean deMentor) {
		super();
		this.contenido = contenido;
		this.emisor = emisor;
		this.receptor = receptor;
		this.id = id;
		this.deMentor = deMentor;
	}

	public MensajeReenvio() {
		super();
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	

	public String getEmisor() {
		return emisor;
	}

	public void setEmisor(String emisor) {
		this.emisor = emisor;
	}

	public String getReceptor() {
		return receptor;
	}

	public void setReceptor(String receptor) {
		this.receptor = receptor;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isDeMentor() {
		return deMentor;
	}

	public void setDeMentor(boolean deMentor) {
		this.deMentor = deMentor;
	}

	@Override
	public String toString() {
		return "MensajeReenvio [contenido=" + contenido + ", emisor=" + emisor + ", receptor=" + receptor
				+ ", deMentor=" + deMentor + ", id=" + id + "]";
	}

	

	

	


	

	
	
}
