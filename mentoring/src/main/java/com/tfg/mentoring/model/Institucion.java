package com.tfg.mentoring.model;

import java.io.Serializable;
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


/*La siguiente vez, poner el correo vacio, asi se impide que se intente poner de alguna forma otro con el correo vacio?
 * Para insertar la institucion otros (quizas se podria hacer de manera automatica al iniciar)
 * insert into usuarios (enable, password, rol, unlocked, username, verification_code, notificar_correo)
 values (FALSE, 'nohay', 3, false, 'OtraInstitucion', null, false);
 * insert into instituciones (color, direccion, nombre, visibilidad, webpage, usuario_username, colorbandas, letrasblancas, letrasblancasbandas)
 values (null, null, 'Otra', false, null, 'OtraInstitucion', null, false, false);
*/

@Entity
@Table(name="instituciones")
public class Institucion implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(insertable = false,updatable = false)
	private String correo;
	@OneToOne(cascade = CascadeType.ALL, optional=false, fetch = FetchType.EAGER)
	@MapsId
	private Usuario usuario;
	@Column(name = "nombre", nullable=false, unique = true)
	private String nombre;
	//@Column(name = "logo")
	//private String logo; //Ruta hasta el fichero que contiene el logo
	@Column(name = "color")
	private String color; //Codigo de color que quieren en la interfaz
	@Column(name = "colorbandas")
	private String colorB; //Codigo de color que quieren en la interfaz
	@Column(name = "letrasblancas")
	private boolean letrasBlancas; //Codigo de color que quieren en la interfaz
	@Column(name = "letrasblancasbandas")
	private boolean letrasBB; //Codigo de color que quieren en la interfaz
	@Column(name = "webpage")
	private String webpage; //Pagina web de la institucion
	@Column(name = "direccion")
	private String direccion; //Direccion fisica de la sede de la institucion
	@Column(name = "visibilidad")
	private boolean visibilidad; //Si desea aparecer en la lista de instituciones contratantes
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name="institucion_extension", joinColumns = { @JoinColumn(name = "usuario_institucion") }, inverseJoinColumns = { @JoinColumn(name = "extension") })
	private List<Extension> extensiones = new ArrayList<>();
	
	
	public Institucion() {
		super();
	}
	
	public Institucion(String nombre) {
		super();
		this.nombre=nombre;
		this.usuario = null;
		this.color = null;
		this.webpage = null;
		this.direccion = null;
		this.visibilidad = false;
		
	}

	public Institucion(Usuario usuario, String nombre, String color, String webpage, String direccion,
			boolean visibilidad, String colorB, boolean letrasBlancas, boolean letrasBB) {
		super();
		this.usuario = usuario;
		this.nombre = nombre;
		this.color = color;
		this.webpage = webpage;
		this.direccion = direccion;
		this.visibilidad = visibilidad;
		this.colorB=colorB;
		this.letrasBB=letrasBB;
		this.letrasBlancas=letrasBlancas;
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
	
	

	public String getColorB() {
		return colorB;
	}

	public void setColorB(String colorB) {
		this.colorB = colorB;
	}

	public boolean isLetrasBlancas() {
		return letrasBlancas;
	}

	public void setLetrasBlancas(boolean letrasBlancas) {
		this.letrasBlancas = letrasBlancas;
	}

	public boolean isLetrasBB() {
		return letrasBB;
	}

	public void setLetrasBB(boolean letrasBB) {
		this.letrasBB = letrasBB;
	}

	@Override
	public String toString() {
		return "Institucion [correo=" + correo + ", usuario=" + usuario + ", nombre=" + nombre + ", color=" + color
				+ ", colorB=" + colorB + ", letrasBlancas=" + letrasBlancas + ", letrasBB=" + letrasBB + ", webpage="
				+ webpage + ", direccion=" + direccion + ", visibilidad=" + visibilidad + ", extensiones=" + extensiones
				+ "]";
	}

	

	
	
	
	

}
