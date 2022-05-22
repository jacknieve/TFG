package com.tfg.mentoring.model.auxiliar.requests;

public class CamposBusqueda {
	private String area;
	private String institucion;
	private int horas;
	
	public CamposBusqueda(String area, String institucion, int horas) {
		super();
		this.area = area;
		this.institucion = institucion;
		this.horas = horas;
	}
	
	public CamposBusqueda() {
		super();
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getInstitucion() {
		return institucion;
	}

	public void setInstitucion(String institucion) {
		this.institucion = institucion;
	}

	public int getHoras() {
		return horas;
	}

	public void setHoras(int horas) {
		this.horas = horas;
	}

	@Override
	public String toString() {
		return "CamposBusqueda [area=" + area + ", institucion=" + institucion + ", horas=" + horas + "]";
	}
	
	
	
}
