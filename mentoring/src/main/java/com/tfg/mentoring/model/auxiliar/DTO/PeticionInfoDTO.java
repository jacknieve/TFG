package com.tfg.mentoring.model.auxiliar.DTO;

import java.util.ArrayList;
import java.util.List;

import com.tfg.mentoring.model.AreaConocimiento;

public class PeticionInfoDTO {
	
	private String nivelEstudiosNivelestudios;
	private String telefono;
	private String descripcion;
	private String linkedin;
	private List<AreaConocimiento> areas = new ArrayList<>();
	private String institucionNombre;
	private int edad;
	private String correo;
	private String foto;
	
	public PeticionInfoDTO(String nivelEstudiosNivelestudios, String telefono, String descripcion, String linkedin,
			List<AreaConocimiento> areas, String institucionNombre, int edad, String correo, String foto) {
		super();
		this.nivelEstudiosNivelestudios = nivelEstudiosNivelestudios;
		this.telefono = telefono;
		this.descripcion = descripcion;
		this.linkedin = linkedin;
		this.areas = areas;
		this.institucionNombre = institucionNombre;
		this.edad = edad;
		this.correo = correo;
		this.foto = foto;
	}

	public PeticionInfoDTO() {
		super();
	}

	public String getNivelEstudiosNivelestudios() {
		return nivelEstudiosNivelestudios;
	}

	public void setNivelEstudiosNivelestudios(String nivelEstudiosNivelestudios) {
		this.nivelEstudiosNivelestudios = nivelEstudiosNivelestudios;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getLinkedin() {
		return linkedin;
	}

	public void setLinkedin(String linkedin) {
		this.linkedin = linkedin;
	}

	public List<AreaConocimiento> getAreas() {
		return areas;
	}

	public void setAreas(List<AreaConocimiento> areas) {
		this.areas = areas;
	}

	public String getInstitucionNombre() {
		return institucionNombre;
	}

	public void setInstitucionNombre(String institucionNombre) {
		this.institucionNombre = institucionNombre;
	}

	public int getEdad() {
		return edad;
	}

	public void setEdad(int edad) {
		this.edad = edad;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	@Override
	public String toString() {
		return "PeticionInfoDTO [nivelEstudiosNivelestudios=" + nivelEstudiosNivelestudios + ", telefono=" + telefono
				+ ", descripcion=" + descripcion + ", linkedin=" + linkedin + ", areas=" + areas
				+ ", institucionNombre=" + institucionNombre + ", edad=" + edad + ", correo=" + correo + ", foto="
				+ foto + "]";
	}
	
	

}
