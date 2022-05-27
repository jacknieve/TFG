package com.tfg.mentoring.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import com.tfg.mentoring.model.Ids.SalaChatId;

@Entity
@Table(name = "salaschat")
public class SalaChat implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private SalaChatId id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("mentor")
	private Mentor mentor;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("mentorizado")
	private Mentorizado mentorizado;
	
	@Column(name="fecha_cierre")
	private Date cierre;
	
	@Generated(GenerationTime.INSERT)
	@Column(name="id_sala", columnDefinition = "serial")
	private long id_sala = 0;
	
	/*@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumns({@JoinColumn(name="id_mentor", referencedColumnName = "mentor_usuario_mentor"),
		@JoinColumn(name="id_mentorizado", referencedColumnName = "mentorizado_usuario_username"),@JoinColumn(name="inicio", referencedColumnName = "fecha_inicio")})
	private List<MensajeChat> mensajes;*/
	
	/*@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name="id_sala", referencedColumnName = "id_sala")
	private List<MensajeChat> mensajes;*/

	public SalaChat(Mentor mentor, Mentorizado mentorizado) {
		super();
		this.mentor=mentor;
		this.mentorizado=mentorizado;
		this.id = new SalaChatId(mentor.getCorreo(), mentorizado.getCorreo());
		//mensajes = new ArrayList<MensajeChat>();
	}

	public SalaChat() {
		super();
	}


	public SalaChatId getId() {
		return id;
	}

	/*public List<MensajeChat> getMensajes() {
		return mensajes;
	}*/

	public Mentor getMentor() {
		return mentor;
	}

	public void setMentor(Mentor mentor) {
		this.mentor = mentor;
	}

	public Mentorizado getMentorizado() {
		return mentorizado;
	}

	public void setMentorizado(Mentorizado mentorizado) {
		this.mentorizado = mentorizado;
	}

	public Date getCierre() {
		return cierre;
	}

	public void setCierre(Date cierre) {
		this.cierre = cierre;
	}
	
	
	
	public long getId_sala() {
		return id_sala;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        SalaChat that = (SalaChat) o;
        return Objects.equals(id, that.id) &&
        	   Objects.equals(mentor, that.mentor) &&
               Objects.equals(mentorizado, that.mentorizado) &&
               Objects.equals(id_sala, that.id_sala) &&
               Objects.equals(cierre, that.cierre);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id, mentor, mentorizado, id_sala, cierre);
    }

	@Override
	public String toString() {
		return "SalaChat [id=" + id + ", mentor=" + mentor + ", mentorizado=" + mentorizado + ", cierre=" + cierre
				+ ", id_sala=" + id_sala + "]";
	}

	/*@Override
	public String toString() {
		return "SalaChat [id=" + id + ", mentor=" + mentor + ", mentorizado=" + mentorizado + ", cierre=" + cierre
				+ ", id_sala=" + id_sala + ", mensajes=" + mensajes + "]";
	}*/
    
    

	
	
    
	
}
