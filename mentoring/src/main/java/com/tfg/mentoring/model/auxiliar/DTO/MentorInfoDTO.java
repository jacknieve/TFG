package com.tfg.mentoring.model.auxiliar.DTO;

import java.util.ArrayList;
import java.util.List;

import com.tfg.mentoring.model.AreaConocimiento;

public class MentorInfoDTO {

	
	private String linkedin;
	private int edad;
	private String descripcion;
	private List<AreaConocimiento> areas = new ArrayList<>();
	
	public MentorInfoDTO(String linkedin, int edad, String descripcion, List<AreaConocimiento> areas) {
		super();
		this.linkedin = linkedin;
		this.edad = edad;
		this.descripcion = descripcion;
		this.areas = areas;
	}

	public MentorInfoDTO() {
		super();
	}

	public String getLinkedin() {
		return linkedin;
	}

	public void setLinkedin(String linkedin) {
		this.linkedin = linkedin;
	}

	public int getEdad() {
		return edad;
	}

	public void setEdad(int edad) {
		this.edad = edad;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public List<AreaConocimiento> getAreas() {
		return areas;
	}

	public void setAreas(List<AreaConocimiento> areas) {
		this.areas = areas;
	}

	@Override
	public String toString() {
		return "MentorInfoDTO [linkedin=" + linkedin + ", edad=" + edad + ", descripcion=" + descripcion + ", areas="
				+ areas + "]";
	}
	
	
	
	
}
