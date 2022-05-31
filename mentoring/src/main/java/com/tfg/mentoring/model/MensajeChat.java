package com.tfg.mentoring.model;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tfg.mentoring.model.Ids.MensajeChatId;
import com.tfg.mentoring.model.auxiliar.enums.EstadoMensaje;

@Entity
@Table(name = "mensajes")
public class MensajeChat {

	@EmbeddedId
	private MensajeChatId id;
	@Column(name="contenido", length = 300)
	private String contenido;
	@Column(name="estado")
	private EstadoMensaje estado;
	
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name="id_sala", referencedColumnName = "id_sala", insertable = false, updatable = false)
	private SalaChat sala;
	@Column(name="detexto")
	private boolean deTexto;//Indica si el mensaje contiene texto (true) o un fichero (false)
	
	
	public MensajeChat(String contenido, SalaChat s, boolean deMentor, boolean deTexto) {
		super();
		this.contenido = contenido;
		this.id = new MensajeChatId(s.getId_sala(), deMentor); 
		this.estado = EstadoMensaje.ENVIADO;
		this.deTexto = deTexto;
	}

	public MensajeChat() {
		super();
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	public MensajeChatId getId() {
		return id;
	}
	
	

	public EstadoMensaje getEstado() {
		return estado;
	}

	public void setEstado(EstadoMensaje estado) {
		this.estado = estado;
	}
	
	

	public SalaChat getSala() {
		return sala;
	}
	
	

	public boolean isDeTexto() {
		return deTexto;
	}

	public void setDeTexto(boolean deTexto) {
		this.deTexto = deTexto;
	}

	@Override
	public String toString() {
		return "MensajeChat [id=" + id + ", contenido=" + contenido + ", estado=" + estado + ", sala=" + sala
				+ ", deTexto=" + deTexto + "]";
	}

	

	
	

	
	
	
	
	
}
