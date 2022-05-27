package com.tfg.mentoring.model.Ids;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class FicheroId implements Serializable{
	
	private static final long serialVersionUID = 1L;
	@Column(name="usuario")
	private String usuario;
	@Column(name="nombre", length = 250)
	private String nombre;
	
	public FicheroId() {
		
	}
	
	public FicheroId(String usuario, String nombre) {
		this.usuario=usuario;
		this.nombre=nombre;
	}

	public String getUsuario() {
		return usuario;
	}

	public String getNombre() {
		return nombre;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		FicheroId that = (FicheroId) o;
		return Objects.equals(usuario, that.usuario) && Objects.equals(nombre, that.nombre);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(usuario, nombre);
	}

	@Override
	public String toString() {
		return "FicheroId [usuario=" + usuario + ", nombre=" + nombre + "]";
	}
	
	


}
