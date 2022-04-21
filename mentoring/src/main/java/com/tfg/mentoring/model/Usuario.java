package com.tfg.mentoring.model;

import java.util.Arrays;
import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tfg.mentoring.model.auxiliar.Roles;

//Esto para los errores al verificar los campos
//https://www.baeldung.com/spring-thymeleaf-error-messages

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "username", nullable=false)
	@NotBlank(message = "El correo no puede estar vacío")
	@Email(message = "Formato no válido")
	private String username;
	@Column(name = "password", nullable=false)
	@NotBlank(message = "La contrasela no puede estar vacía")
	private String password;
	@Column(name = "rol", nullable=false)
	private Roles rol;
	@Column(name = "unlocked")
	private boolean unlocked;
	@Column(name = "enable")
	private boolean enable;
	@Column(name = "verification_code", length = 64)
    private String verificationCode;
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
		this.username = correo;
		this.password = password;
		this.rol = rol;
		this.unlocked=true;
		this.enable=true;
	}


	public void setUsername(String correo) {
		this.username = correo;
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
		return this.username;
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
	
	
	public void setUnlocked(boolean unlocked) {
		this.unlocked = unlocked;
	}


	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	@Override
	public String toString() {
		return "Usuario [correo=" + username + ", password=" + password + ", rol=" + rol + ", unlocked="
				+ unlocked + ", enable=" + enable + "]";
	}
	

	
	
	
	

	
	

}
