package com.tfg.mentoring.model;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.tfg.mentoring.model.auxiliar.enums.EstadosPeticion;

@Entity(name="Peticion")
@Table(name="peticiones")
public class Peticion {
	@EmbeddedId
	private PeticionId id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("mentor")
	private Mentor mentor;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("mentorizado")
	private Mentorizado mentorizado;
	//Esta es necesaria??? (de quitarla, hay que actualizar las consultas de borrar usuario que la fijan a la actual)
	@Column(name="fecha_resolucion")
	private Date resolucion;
	
	@Column(name="motivo", length = 300)
	private String motivo;
	
	@Column(name="estado")
	private EstadosPeticion estado;
	
	
	public Peticion() {
		
	}
	
	public Peticion (Mentor mentor, Mentorizado mentorizado, String motivo) {
		this.mentor=mentor;
		this.mentorizado=mentorizado;
		this.id = new PeticionId(mentor.getCorreo(),mentorizado.getCorreo());
		this.estado = EstadosPeticion.ENVIADA;
		this.motivo=motivo;
	}
	
	
	public Date getResolucion() {
		return resolucion;
	}

	public void setResolucion(Date resolucion) {
		this.resolucion = resolucion;
	}

	public PeticionId getId() {
		return id;
	}

	public Mentor getMentor() {
		return mentor;
	}

	public Mentorizado getMentorizado() {
		return mentorizado;
	}
	

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public EstadosPeticion getEstado() {
		return estado;
	}

	public void setEstado(EstadosPeticion estado) {
		this.estado = estado;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        Peticion that = (Peticion) o;
        return Objects.equals(mentor, that.mentor) &&
               Objects.equals(mentorizado, that.mentorizado) &&
               Objects.equals(resolucion, that.resolucion) &&
               Objects.equals(estado, that.estado) &&
               Objects.equals(motivo, that.motivo) &&
               Objects.equals(id.getCreadaEn(), that.getId().getCreadaEn());
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(mentor, mentorizado, id.getCreadaEn(), resolucion, estado, motivo);
    }

	@Override
	public String toString() {
		return "Peticion [id=" + id + ", mentor=" + mentor + ", mentorizado=" + mentorizado + ", resolucion="
				+ resolucion + ", motivo=" + motivo + ", estado=" + estado + "]";
	}
    
    
}
