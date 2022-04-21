package com.tfg.mentoring.model.auxiliar;

import java.util.Date;

import com.tfg.mentoring.model.Notificacion;

//Esta clase sirve para almacenar las notificaciones que recibiran los usuarios con los datos necesarios
public class NotificacionUser {

	private long id;
	private String titulo;
	private String descripcion;
	private Date fechaenv;
	private boolean nueva;
	
	public NotificacionUser(long id, String titulo, String descripcion, Date fechaenv, boolean nueva) {
		super();
		this.id = id;
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.fechaenv = fechaenv;
		this.nueva = nueva;
	}
	
	public NotificacionUser(Notificacion n) {
		super();
		this.id = n.getId();
		this.titulo = n.getTitulo();
		this.descripcion = n.getDescripcion();
		this.fechaenv = n.getFechaenv();
		if(n.getEstado() == EstadosNotificacion.ENTREGADA) this.nueva = true;
		else this.nueva=false;
		
	}

	public NotificacionUser() {
		super();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Date getFechaenv() {
		return fechaenv;
	}

	public void setFechaenv(Date fechaenv) {
		this.fechaenv = fechaenv;
	}

	public boolean isNueva() {
		return nueva;
	}

	public void setNueva(boolean nueva) {
		this.nueva = nueva;
	}

	@Override
	public String toString() {
		return "NotificacionUser [id=" + id + ", titulo=" + titulo + ", descripcion=" + descripcion + ", fechaenv="
				+ fechaenv + ", nueva=" + nueva + "]";
	}
	
	
}
