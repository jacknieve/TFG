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


import com.tfg.mentoring.model.Ids.MentorizacionId;
import com.tfg.mentoring.model.auxiliar.enums.FasesMentorizacion;

@Entity(name="Mentorizacion")
@Table(name="mentorizaciones")
public class Mentorizacion {

	@EmbeddedId
	private MentorizacionId id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("mentor")
	private Mentor mentor;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("mentorizado")
	private Mentorizado mentorizado;
	
	@Column(name="fecha_fin")
	private Date fin;
	
	@Column(name="calificacion")
	private Integer calificacion;
	
	@Column(name="comentario", length = 300)
	private String comentario;
	
	@Column(name="fase")
	private FasesMentorizacion fase;
	
	
	public Mentorizacion() {
		
	}
	
	public Mentorizacion (Mentor mentor, Mentorizado mentorizado) {
		this.mentor=mentor;
		this.mentorizado=mentorizado;
		this.id = new MentorizacionId(mentor.getCorreo(),mentorizado.getCorreo());
		this.fase = FasesMentorizacion.NACIMIENTO;
		this.fin = null;
		this.calificacion = null;
		this.comentario=null;
	}
	
	
	public Date getFin() {
		return fin;
	}

	public void setFin(Date fin) {
		this.fin = fin;
	}

	public MentorizacionId getId() {
		return id;
	}

	public Mentor getMentor() {
		return mentor;
	}

	public Mentorizado getMentorizado() {
		return mentorizado;
	}
	
	

	public Integer getCalificacion() {
		return calificacion;
	}

	public void setCalificacion(Integer calificacion) {
		this.calificacion = calificacion;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public FasesMentorizacion getFase() {
		return fase;
	}

	public void setFase(FasesMentorizacion fase) {
		this.fase = fase;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        Mentorizacion that = (Mentorizacion) o;
        return Objects.equals(mentor, that.mentor) &&
               Objects.equals(mentorizado, that.mentorizado) &&
               Objects.equals(comentario, that.comentario) &&
               Objects.equals(calificacion, that.calificacion) &&
               Objects.equals(fase, that.fase) &&
               Objects.equals(fin, that.fin) &&
               Objects.equals(id.getCreadaEn(), that.getId().getCreadaEn());
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(mentor, mentorizado, id.getCreadaEn(), calificacion, comentario, fase, fin);
    }

	@Override
	public String toString() {
		return "Mentorizacion [id=" + id + ", mentor=" + mentor + ", mentorizado=" + mentorizado + ", fin=" + fin
				+ ", calificacion=" + calificacion + ", comentario=" + comentario + ", fase=" + fase + "]";
	}
	
    
    
}
