package com.tfg.mentoring.model;

import java.util.Arrays;
import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails{
	@Id
	@Column(name = "correo", nullable=false)
	private String correo;
	@Column(name = "password", nullable=false)
	private String password;
	@Column(name = "id", nullable=false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@Column(name = "rol", nullable=false)
	private Roles rol;
	@Column(name = "unlocked")
	private boolean unlocked;
	@Column(name = "enable")
	private boolean enable;
	//orphanRemoval para indicar que la entidad hija sera eliminada directamente al dejar de
	//referenciar a la padre
	//Aqui igual mejor invertir, porque al recuperar un mentor, mentorizado... se recuperarían si no también sus notificaciones
	/*@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name="correo")
	private List<Notificacion> notificaciones = new ArrayList<>();*/
	
	
	public Usuario() {
		this.unlocked=true;
		this.enable=true;
	}
	
	public Usuario(String correo, String password, Roles rol) {
		super();
		this.correo = correo;
		this.password = password;
		this.rol = rol;
		this.unlocked=true;
		this.enable=true;
	}


	public String getCorreo() {
		return correo;
	}
	public void setCorreo(String correo) {
		this.correo = correo;
	}
	
	public long getId() {
		return id;
	}

	public Roles getRol() {
		return rol;
	}

	public void setRol(Roles rol) {
		this.rol = rol;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return this.password;
	}
	
	public void setPassword(String password) {
		this.password=password;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return this.correo;
	}
	
	//UserDetails methods
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
			//Nos devuelve los permisos del usuario en forma de array
		return Arrays.asList(new SimpleGrantedAuthority(this.rol.toString()));
	}
	
	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return this.unlocked;
	}
	
	
	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return this.enable;
	}

	@Override
	public String toString() {
		return "Usuario [correo=" + correo + ", password=" + password + ", id=" + id + ", rol=" + rol + ", unlocked="
				+ unlocked + ", enable=" + enable + "]";
	}
	

	
	
	
	

	
	

}
