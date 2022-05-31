package com.tfg.mentoring.model.auxiliar.DTO;

import java.text.SimpleDateFormat;

import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.Peticion;
import com.tfg.mentoring.model.auxiliar.enums.EstadosPeticion;

public class PeticionDTO {

	private String nombre;
	private String motivo;
	private boolean nueva;
	private String fechaenv;
	private PeticionInfoDTO info;
	
	public PeticionDTO(String nombre, String motivo, boolean nueva, String fechaenv, PeticionInfoDTO info) {
		super();
		this.nombre = nombre;
		this.motivo = motivo;
		this.nueva = nueva;
		this.fechaenv = fechaenv;
		this.info = info;
	}
	
	public PeticionDTO(Peticion p, PeticionInfoDTO info) {
		super();
		Mentorizado m = p.getMentorizado();
		this.nombre = m.getNombre()+" "+m.getPapellido()+" "+m.getSapellido();
		this.motivo = p.getMotivo();
		if(p.getEstado() == EstadosPeticion.ENVIADA) this.nueva = true;
		else this.nueva=false;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.fechaenv = df.format(p.getId().getCreadaEn());
		this.info = info;
	}

	public PeticionDTO() {
		super();
	}


	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public boolean isNueva() {
		return nueva;
	}

	public void setNueva(boolean nueva) {
		this.nueva = nueva;
	}

	public String getFechaenv() {
		return fechaenv;
	}

	public void setFechaenv(String fechaenv) {
		this.fechaenv = fechaenv;
	}
	
	
	

	public PeticionInfoDTO getInfo() {
		return info;
	}

	public void setInfo(PeticionInfoDTO info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "PeticionDTO [nombre=" + nombre + ", motivo=" + motivo + ", nueva=" + nueva + ", fechaenv=" + fechaenv
				+ ", info=" + info + "]";
	}

	

	

	
	
	
}
