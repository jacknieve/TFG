package com.example.prototipoRegistro.model;

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
@Entity
@Table(name = "usuarios")
public class UsuarioInv {
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private long id;
		@Column(name = "username", nullable=false)
		private String username;
		@Column(name = "password", nullable=false)
		private String password;
		@Column(name = "edad")
		private int edad;
		@Column(name = "mentor", nullable=false)
		private boolean mentor;
		@Column(name = "area")
		private String area;
		@Column(name = "unlocked")
		private boolean unlocked;
		@Column(name = "enable")
		private boolean enable;
		//orphanRemoval para indicar que la entidad hija sera eliminada directamente al dejar de
		//referenciar a la padre
		/*@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
		@JoinColumn(name="id_user")
		private List<Notificacion> notificaciones = new ArrayList<>();*/
		
		public UsuarioInv() {
			this.unlocked=true;
			this.enable=true;
		}
		
		public UsuarioInv(String nombre, String password, int edad, boolean mentor, String area_conocimiento) {
			super();
			this.username = nombre;
			this.password = password;
			this.edad = edad;
			this.mentor = mentor;
			this.area = area_conocimiento;
			this.unlocked=true;
			this.enable=true;
		}


		public long getId() {
			return id;
		}
		public void setUsername(String nombre) {
			this.username = nombre;
		}
		public int getEdad() {
			return edad;
		}
		public void setEdad(int edad) {
			this.edad = edad;
		}
		public boolean isMentor() {
			return mentor;
		}
		public void setMentor(boolean mentor) {
			this.mentor = mentor;
		}
		public String getArea() {
			return area;
		}
		public void setArea(String area) {
			this.area = area;
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
		
		//UserDetails methods
		
		public Collection<? extends GrantedAuthority> getAuthorities() {
			if(mentor) {
				//Nos devuelve los permisos del usuario en forma de array
				return Arrays.asList(new SimpleGrantedAuthority("MENTOR"));
			}
			else {
				return Arrays.asList(new SimpleGrantedAuthority("MENTORIZADO"));
			}
		}
		
		public boolean isAccountNonExpired() {
			// TODO Auto-generated method stub
			return true;
		}
		
		public boolean isCredentialsNonExpired() {
			// TODO Auto-generated method stub
			return true;
		}
		
		public boolean isAccountNonLocked() {
			// TODO Auto-generated method stub
			return this.unlocked;
		}
		
		
		public boolean isEnabled() {
			// TODO Auto-generated method stub
			return this.enable;
		}
		

		@Override
		public String toString() {
			return "Usuario [id=" + id + ", username=" + username + ", password=" + password + ", edad=" + edad
					+ ", mentor=" + mentor + ", area=" + area + ", unlocked=" + unlocked + ", enable=" + enable
					+ "]";
		}
		
		
		

		
		
		
	
}
