package com.tfg.mentoring.model.auxiliar.DTO;

import java.text.SimpleDateFormat;

import com.tfg.mentoring.model.Notificacion;
import com.tfg.mentoring.model.auxiliar.CuerpoMensaje;
import com.tfg.mentoring.model.auxiliar.enums.EstadosNotificacion;
import com.tfg.mentoring.model.auxiliar.enums.MotivosNotificacion;

//Esta clase sirve para almacenar las notificaciones que recibiran los usuarios con los datos necesarios
public class NotificacionDTO implements CuerpoMensaje{

	private long id;
	private String titulo;
	private String descripcion;
	private String fechaenv;
	private boolean nueva;
	private MotivosNotificacion motivo;
	
	public NotificacionDTO(long id, String titulo, String descripcion, String fechaenv, boolean nueva, MotivosNotificacion motivo) {
		super();
		this.id = id;
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.fechaenv = fechaenv;
		this.nueva = nueva;
		this.motivo=motivo;
	}
	
	public NotificacionDTO(Notificacion n) {
		super();
		this.id = n.getId();
		this.titulo = n.getTitulo();
		this.descripcion = n.getDescripcion();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.fechaenv = df.format(n.getFechaenv());
		if(n.getEstado() == EstadosNotificacion.ENTREGADA) this.nueva = true;
		else this.nueva=false;
		this.motivo = n.getMotivo();
		
	}

	public NotificacionDTO() {
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

	public String getFechaenv() {
		return fechaenv;
	}

	public void setFechaenv(String fechaenv) {
		this.fechaenv = fechaenv;
	}

	public boolean isNueva() {
		return nueva;
	}

	public void setNueva(boolean nueva) {
		this.nueva = nueva;
	}
	
	

	public MotivosNotificacion getMotivo() {
		return motivo;
	}

	public void setMotivo(MotivosNotificacion motivo) {
		this.motivo = motivo;
	}

	@Override
	public String toString() {
		return "NotificacionDTO [id=" + id + ", titulo=" + titulo + ", descripcion=" + descripcion + ", fechaenv="
				+ fechaenv + ", nueva=" + nueva + ", motivo=" + motivo + "]";
	}

	
	
	
}
