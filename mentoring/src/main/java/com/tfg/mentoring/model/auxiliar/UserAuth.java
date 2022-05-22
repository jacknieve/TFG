package com.tfg.mentoring.model.auxiliar;

import java.util.Arrays;
import java.util.Collection;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.enums.Roles;

public class UserAuth implements UserDetails{

	private static final long serialVersionUID = 1L;
	private String username;
	private String password;
	private Roles rol;
	private boolean unlocked;
	private boolean enable;
	private boolean notificar_correo;
    private String verificationCode;
	
	
	public UserAuth() {
		this.unlocked=true;
		this.enable=true;
	}
	
	public UserAuth(String correo, String password, Roles rol) {
		super();
		this.username = correo;
		this.password = password;
		this.rol = rol;
		this.unlocked=true;
		this.enable=true;
		this.notificar_correo=true;
	}
	
	public UserAuth(String correo, String password, boolean enable, String verificationCode) {
		super();
		this.username = correo;
		this.password = password;
		this.rol = null;
		this.unlocked=true;
		this.enable=enable;
		this.verificationCode = verificationCode;
		this.notificar_correo=true;
	}


	public UserAuth( String username, String password, Roles rol, boolean unlocked,
			boolean enable, String verificationCode, boolean notificar_correo, String jid, String sid, long rid) {
		super();
		this.username = username;
		this.password = password;
		this.rol = rol;
		this.unlocked = unlocked;
		this.enable = enable;
		this.verificationCode = verificationCode;
		this.notificar_correo=notificar_correo;
	}
	
	public UserAuth(Usuario u) {
		this.username = u.getUsername();
		this.password = u.getPassword();
		this.rol = u.getRol();
		this.unlocked = u.isAccountNonLocked();
		this.enable = u.isEnabled();
		this.verificationCode = u.getVerificationCode();
		this.notificar_correo=u.isNotificar_correo();
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
	
	

	public boolean isNotificar_correo() {
		return notificar_correo;
	}

	public void setNotificar_correo(boolean notificar_correo) {
		this.notificar_correo = notificar_correo;
	}

	@Override
	public String toString() {
		return "UserAuth [username=" + username + ", password=" + password + ", rol=" + rol + ", unlocked=" + unlocked
				+ ", enable=" + enable + ", notificar_correo=" + notificar_correo + ", verificationCode="
				+ verificationCode + "]";
	}
	

	

	

}
