package com.example.prototipoRegistro.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
//https://www.adictosaltrabajo.com/2020/04/02/hibernate-onetoone-onetomany-manytoone-y-manytomany/
@Entity
@Table(name = "notificaciones")
public class Notificacion {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	//@ManyToOne()
	//@JoinColumn(name="id_user")
	//private Usuario usuario;
	@Column(name="id_user")
	private long usuario;
	@Column(name="descripcion")
	private String descripcion;
	@Column(name="estado")
	private EstadosNotificacion estado;
	@Column(name="fechaenv")
	private Date fechaenv;
	@Column(name="fechaeliminacion")
	private Date fechaeliminacion;
	
	public Notificacion() {
		
	}
	
	/*public Notificacion(Usuario usuario, String descripcion, EstadosNotificacion estado) {
		this.usuario=usuario;
		this.descripcion=descripcion;
		this.estado=estado;
	}*/
	
	public Notificacion(String descripcion, EstadosNotificacion estado, long usuario) {
		this.descripcion=descripcion;
		this.estado=estado;
		this.usuario=usuario;
		fechaenv = new Date();
		fechaeliminacion = null;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	/*public Usuario getUsuario() {
		return usuario;
	}*/
	public long getUsuario() {
		return usuario;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public EstadosNotificacion getEstado() {
		return estado;
	}
	public void setEstado(EstadosNotificacion estado) {
		this.estado = estado;
	}

	public Date getFechaeliminacion() {
		return fechaeliminacion;
	}

	public void setFechaeliminacion(Date fechaeliminacion) {
		this.fechaeliminacion = fechaeliminacion;
	}

	public Date getFechaenv() {
		return fechaenv;
	}

	@Override
	public String toString() {
		return "Id: "+this.id+", Decripcion: "+this.descripcion+", Usuario: "+this.usuario+", Estado: "
				+this.estado+", Eliminacion: "+this.fechaeliminacion+", Envio: "+this.fechaenv;
	}
	
	
	
	
	

}
