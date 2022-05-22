package com.tfg.mentoring.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.tfg.mentoring.model.auxiliar.requests.UserAux;

@Entity
@Table(name="mentorizados")
public class Mentorizado {
	
	@Id
	@Column(insertable = false,updatable = false)
	private String correo;
	@OneToOne(cascade = CascadeType.ALL, optional=false, fetch = FetchType.EAGER)
	@MapsId
	private Usuario usuario;
	@Column(name = "nombre", nullable=false)
	private String nombre;
	@Column(name = "papellido")
	private String papellido;
	@Column(name = "sapellido")
	private String sapellido;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="nivelestudios", nullable=false)
	private NivelEstudios nivelEstudios;
	@Column(name = "telefono")
	private String telefono;
	@Column(name = "descripcion", length = 500)
	private String descripcion;
	@Column(name = "linkedin")
	private String linkedin;
	@Column(name="feliminacion")
	private Date feliminacion;
	@Column(name="fregistro")
	private Date fregistro;
	@Column(name="fnacimiento")
	private Date fnacimiento;
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name="area_mentorizado", joinColumns = { @JoinColumn(name = "correo") }, inverseJoinColumns = { @JoinColumn(name = "area") })
	private List<AreaConocimiento> areas = new ArrayList<>();
	//Aqui usamos mejor un string en vez de una referencia porque no van a estar todas las instituciones en la base de datos
	/*@Column(name="institucion")
	private String institucion;*/
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name="institucion", referencedColumnName = "nombre")
	private Institucion institucion;
	
	public Mentorizado(Usuario usuario, String nombre, String papellido, String sapellido, NivelEstudios nivelEstudios,
			String telefono, String descripcion, String linkedin, Date fregistro, Date fnacimiento, Institucion institucion ) {
		super();
		this.usuario = usuario;
		this.nombre = nombre;
		this.papellido = papellido;
		this.sapellido = sapellido;
		this.nivelEstudios = nivelEstudios;
		this.telefono = telefono;
		this.descripcion = descripcion;
		this.linkedin = linkedin;
		this.fregistro = fregistro;
		this.fnacimiento = fnacimiento;
		this.institucion = institucion;
	}
	
	public Mentorizado(Usuario usuario, UserAux useraux, Institucion institucion){
		super();
		this.usuario=usuario;
		this.nombre=useraux.getNombre();
		this.papellido=useraux.getPapellido();
		this.sapellido=useraux.getSapellido();
		this.nivelEstudios = new NivelEstudios(useraux.getNivelEstudios());
		this.telefono=useraux.getTelefono();
		this.descripcion=useraux.getDescripcion();
		this.feliminacion=null;
		this.fregistro= new Date();
		if(useraux.getFnacimiento() == null || useraux.getFnacimiento().equals("")) {
			this.fnacimiento=null;
		}
		else {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				this.fnacimiento=format.parse(useraux.getFnacimiento());
			}catch (ParseException e) {
				System.out.println(e.getMessage());
				this.fnacimiento=null;
			}
		}
		this.linkedin=useraux.getLinkedin();
		this.institucion=institucion;		
				
	}

	public Mentorizado() {
		super();
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

	public String getPapellido() {
		return papellido;
	}

	public void setPapellido(String papellido) {
		this.papellido = papellido;
	}

	public String getSapellido() {
		return sapellido;
	}

	public void setSapellido(String sapellido) {
		this.sapellido = sapellido;
	}

	public NivelEstudios getNivelEstudios() {
		return nivelEstudios;
	}

	public void setNivelEstudios(NivelEstudios nivelEstudios) {
		this.nivelEstudios = nivelEstudios;
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

	public Date getFeliminacion() {
		return feliminacion;
	}

	public void setFeliminacion(Date feliminacion) {
		this.feliminacion = feliminacion;
	}

	public Date getFregistro() {
		return fregistro;
	}

	public void setFregistro(Date fregistro) {
		this.fregistro = fregistro;
	}


	public Date getFnacimiento() {
		return fnacimiento;
	}

	public void setFnacimiento(Date fnacimiento) {
		this.fnacimiento = fnacimiento;
	}

	public List<AreaConocimiento> getAreas() {
		return areas;
	}

	public void setAreas(List<AreaConocimiento> areas) {
		this.areas = areas;
	}
	
	public String getCorreo() {
		return correo;
	}
	
	

	public Institucion getInstitucion() {
		return institucion;
	}

	public void setInstitucion(Institucion institucion) {
		this.institucion = institucion;
	}

	@Override
	public String toString() {
		return "Mentorizado [correo=" + correo + ", usuario=" + usuario + ", nombre=" + nombre + ", papellido="
				+ papellido + ", sapellido=" + sapellido + ", nivelEstudios=" + nivelEstudios + ", telefono=" + telefono
				+ ", descripcion=" + descripcion + ", linkedin=" + linkedin + ", feliminacion=" + feliminacion
				+ ", fregistro=" + fregistro + ", fnacimiento=" + fnacimiento + ", areas=" + areas + ", institucion="
				+ institucion + "]";
	}

	

	
	
	
}
