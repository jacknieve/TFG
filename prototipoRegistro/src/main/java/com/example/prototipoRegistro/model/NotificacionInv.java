package com.example.prototipoRegistro.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "notificaciones")
public class NotificacionInv {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@ManyToOne(cascade = CascadeType.ALL, optional=false, fetch = FetchType.EAGER)
	@JoinColumn(name="id_user")
	private UsuarioInv usuario;
	/*@Column(name="id_user")
	private long usuario;*/
	@Column(name="descripcion")
	private String descripcion;
	@Column(name="estado")
	private EstadosNotificacion estado;
	@Column(name="fechaenv")
	private Date fechaenv;
	@Column(name="fechaeliminacion")
	private Date fechaeliminacion;
	
	public NotificacionInv() {
		
	}
	
	/*public Notificacion(Usuario usuario, String descripcion, EstadosNotificacion estado) {
		this.usuario=usuario;
		this.descripcion=descripcion;
		this.estado=estado;
	}*/
	
	public NotificacionInv(String descripcion, EstadosNotificacion estado, UsuarioInv usuario) {
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
	public UsuarioInv getUsuario() {
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
