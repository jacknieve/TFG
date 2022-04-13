package com.tfg.mentoring.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="instituciones")
public class Institucion {

	@Id
	@Column(name="correo")
	private String correo;
	@OneToOne(cascade = CascadeType.ALL, optional=false, fetch = FetchType.EAGER)
	@MapsId
	@JoinColumn(name="correo")
	private Usuario usuario;
	@Column(name = "nombre", nullable=false)
	private String nombre;
	@Column(name = "logo")
	private String logo; //Ruta hasta el fichero que contiene el logo
	@Column(name = "color")
	private String color; //Codigo de color que quieren en la interfaz
	@Column(name = "webpage")
	private String webpage; //Pagina web de la institucion
	@Column(name = "direccion")
	private String direccion; //Direccion fisica de la sede de la institucion
	@Column(name = "visibilidad")
	private boolean visibilidad; //Si desea aparecer en la lista de instituciones contratantes
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name="area_mentorizado", joinColumns = { @JoinColumn(name = "correo") }, inverseJoinColumns = { @JoinColumn(name = "extension") })
	private List<Extension> extensiones = new ArrayList<>();
	
	
	
	public Institucion() {
		super();
	}

	public Institucion(Usuario usuario, String nombre, String logo, String color, String webpage, String direccion,
			boolean visibilidad) {
		super();
		this.usuario = usuario;
		this.nombre = nombre;
		this.logo = logo;
		this.color = color;
		this.webpage = webpage;
		this.direccion = direccion;
		this.visibilidad = visibilidad;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getWebpage() {
		return webpage;
	}

	public void setWebpage(String webpage) {
		this.webpage = webpage;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public boolean isVisibilidad() {
		return visibilidad;
	}

	public void setVisibilidad(boolean visibilidad) {
		this.visibilidad = visibilidad;
	}

	public List<Extension> getExtensiones() {
		return extensiones;
	}

	public void setExtensiones(List<Extension> extensiones) {
		this.extensiones = extensiones;
	}

	@Override
	public String toString() {
		return "Institucion [usuario=" + usuario + ", nombre=" + nombre + ", logo=" + logo + ", color=" + color
				+ ", webpage=" + webpage + ", direccion=" + direccion + ", visibilidad=" + visibilidad
				+ ", extensiones=" + extensiones + "]";
	}
	
	
	
	

}
