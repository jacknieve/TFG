package com.tfg.mentoring.model.auxiliar;


import com.tfg.mentoring.model.Mentorizacion;

public class MentorizacionUser {
	private String correo;
	private String nombre;
	private String foto;//Para la futura foto de perfil
	private UsuarioPerfil uperfil;
	private FasesMentorizacion fase;
	
	public MentorizacionUser(String correo, String nombre, String foto, UsuarioPerfil uperfil, FasesMentorizacion fase) {
		super();
		this.correo = correo;
		this.nombre = nombre;
		this.foto = foto;
		this.uperfil = uperfil;
		this.fase = fase;
	}
	
	public MentorizacionUser(Mentorizacion m, UsuarioPerfil up) {
		super();
		this.correo = m.getMentorizado().getCorreo();
		this.nombre = m.getMentorizado().getNombre()+" "+m.getMentorizado().getPapellido()+" "+m.getMentorizado().getSapellido();
		this.foto = null;
		this.uperfil=up;
		this.fase=m.getFase();
	}

	public MentorizacionUser() {
		super();
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public UsuarioPerfil getUperfil() {
		return uperfil;
	}

	public void setUperfil(UsuarioPerfil uperfil) {
		this.uperfil = uperfil;
	}
	
	

	public FasesMentorizacion getFase() {
		return fase;
	}

	public void setFase(FasesMentorizacion fase) {
		this.fase = fase;
	}

	@Override
	public String toString() {
		return "MentorizacionUser [correo=" + correo + ", nombre=" + nombre + ", foto=" + foto + ", uperfil=" + uperfil
				+ ", fase=" + fase + "]";
	}

	

	

}
