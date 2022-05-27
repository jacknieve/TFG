package com.tfg.mentoring.model;

import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.tfg.mentoring.model.Ids.FicheroId;

@Entity
@Table(name = "ficheros")
public class Fichero {
	
	@EmbeddedId
	private FicheroId ficheroId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("usuario")
	private Usuario user;

	public Fichero(Usuario user, String nombre) {
		this.user = user;
		this.ficheroId = new FicheroId(user.getUsername(), nombre);
	}

	public Fichero() {
		
	}

	public FicheroId getFicheroId() {
		return ficheroId;
	}

	public Usuario getUser() {
		return user;
	}
	
	public String getNombre() {
		return ficheroId.getNombre();
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        Fichero that = (Fichero) o;
        return Objects.equals(user, that.user) &&
               Objects.equals(ficheroId, that.ficheroId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(user, ficheroId);
    }

	@Override
	public String toString() {
		return "Fichero [ficheroId=" + ficheroId + ", user=" + user + "]";
	}
    
    
	
	
	
	
	

}
