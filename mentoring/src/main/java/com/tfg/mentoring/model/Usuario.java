package com.tfg.mentoring.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tfg.mentoring.model.auxiliar.enums.Roles;

//Esto para los errores al verificar los campos
//https://www.baeldung.com/spring-thymeleaf-error-messages

@Entity
@Table(name = "usuarios")
public class Usuario{

	@Id
	@Column(name = "username", nullable=false, length = 240)
	private String username;
	@Column(name = "password", nullable=false)
	private String password;
	@Column(name = "rol", nullable=false)
	private Roles rol;
	@Column(name = "unlocked")
	private boolean unlocked;
	@Column(name = "enable")
	private boolean enable;
	@Column(name = "notificar_correo")
	private boolean notificar_correo;
	@Column(name = "verification_code", length = 64)
    private String verificationCode;
	@Column(name="foto", length = 250)
	private String foto;//Extension de la imagen de perfil
	
	
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
		this.notificar_correo=true;
		this.foto = null;
	}
	
	public Usuario(String correo, String password, boolean enable, String verificationCode) {
		super();
		this.username = correo;
		this.password = password;
		this.rol = null;
		this.unlocked=true;
		this.enable=enable;
		this.verificationCode = verificationCode;
		this.notificar_correo=true;
		this.foto = null;
	}


	public Usuario( String username, String password, Roles rol, boolean unlocked,
			boolean enable, String verificationCode, boolean notificar_correo, String foto) {
		super();
		this.username = username;
		this.password = password;
		this.rol = rol;
		this.unlocked = unlocked;
		this.enable = enable;
		this.verificationCode = verificationCode;
		this.notificar_correo=notificar_correo;
		this.foto = foto;
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

	public String getPassword() {
		// TODO Auto-generated method stub
		return this.password;
	}
	
	public void setPassword(String password) {
		this.password=password;
	}

	public String getUsername() {
		// TODO Auto-generated method stub
		return this.username;
	}
	
	
	
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return this.unlocked;
	}
	
	
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
	
	

	public boolean isNotificar_correo() {
		return notificar_correo;
	}

	public void setNotificar_correo(boolean notificar_correo) {
		this.notificar_correo = notificar_correo;
	}

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	@Override
	public String toString() {
		return "Usuario [username=" + username + ", password=" + password + ", rol=" + rol + ", unlocked=" + unlocked
				+ ", enable=" + enable + ", notificar_correo=" + notificar_correo + ", verificationCode="
				+ verificationCode + ", foto=" + foto + "]";
	}
	
	
	

}
