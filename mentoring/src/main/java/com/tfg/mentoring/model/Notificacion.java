package com.tfg.mentoring.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tfg.mentoring.model.auxiliar.enums.EstadosNotificacion;
import com.tfg.mentoring.model.auxiliar.enums.MotivosNotificacion;



@Entity
@Table(name = "notificaciones")
public class Notificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(optional=false, fetch = FetchType.EAGER)
	@JoinColumn(name="id_user")
	private Usuario usuario;
	
	@Column(name="titulo")
	private String titulo;
	@Column(name="descripcion")
	private String descripcion;
	@Column(name="estado")
	private EstadosNotificacion estado;
	@Column(name="fechaenv")
	private Date fechaenv;
	@Column(name="motivo")
	private MotivosNotificacion motivo;
	
	public Notificacion(Usuario usuario, String titulo, String descripcion, MotivosNotificacion motivo) {
		super();
		this.usuario = usuario;
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.fechaenv = new Date();
		this.estado = EstadosNotificacion.ENTREGADA;
		this.motivo=motivo;
	}

	public Notificacion() {
		super();
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
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

	public EstadosNotificacion getEstado() {
		return estado;
	}

	public void setEstado(EstadosNotificacion estado) {
		this.estado = estado;
	}

	public long getId() {
		return id;
	}

	public Date getFechaenv() {
		return fechaenv;
	}

	public MotivosNotificacion getMotivo() {
		return motivo;
	}

	public void setMotivo(MotivosNotificacion motivo) {
		this.motivo = motivo;
	}

	@Override
	public String toString() {
		return "Notificacion [id=" + id + ", usuario=" + usuario + ", titulo=" + titulo + ", descripcion=" + descripcion
				+ ", estado=" + estado + ", fechaenv=" + fechaenv + ", motivo=" + motivo + "]";
	}

	
	
	
	
	
	
	
	
}
