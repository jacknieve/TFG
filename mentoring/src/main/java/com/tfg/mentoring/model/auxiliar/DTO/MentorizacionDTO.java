package com.tfg.mentoring.model.auxiliar.DTO;


import com.tfg.mentoring.model.Mentorizacion;
import com.tfg.mentoring.model.auxiliar.enums.FasesMentorizacion;

public class MentorizacionDTO {
	private String correo;
	private String foto;//Para la futura foto de perfil
	private UsuarioDTO uperfil;
	private FasesMentorizacion fase;
	private Long fecha_fin;
	private boolean verificado;
	
	public MentorizacionDTO(String correo, String foto, UsuarioDTO uperfil, FasesMentorizacion fase, Long fecha_fin, boolean verificado) {
		super();
		this.correo = correo;
		this.foto = foto;
		this.uperfil = uperfil;
		this.fase = fase;
		this.fecha_fin = fecha_fin;
		this.verificado = verificado;
	}
	
	public MentorizacionDTO(Mentorizacion m, UsuarioDTO up, String correo) {
		super();
		this.correo = correo;
		this.foto = null;
		this.uperfil=up;
		this.fase=m.getFase();
		if(m.getFin() != null) {
			this.fecha_fin=m.getFin().getTime();
		}
		else {
			this.fecha_fin=null;
			if(up.isMentor()) {
				this.verificado=up.isVerificado();
			}
			else this.verificado=false;
		}
		
		
	}

	public MentorizacionDTO() {
		super();
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}


	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public UsuarioDTO getUperfil() {
		return uperfil;
	}

	public void setUperfil(UsuarioDTO uperfil) {
		this.uperfil = uperfil;
	}
	
	
	

	public Long getFecha_fin() {
		return fecha_fin;
	}

	public void setFecha_fin(Long fecha_fin) {
		this.fecha_fin = fecha_fin;
	}

	public FasesMentorizacion getFase() {
		return fase;
	}

	public void setFase(FasesMentorizacion fase) {
		this.fase = fase;
	}
	
	

	public boolean isVerificado() {
		return verificado;
	}

	public void setVerificado(boolean verificado) {
		this.verificado = verificado;
	}

	@Override
	public String toString() {
		return "MentorizacionUser [correo=" + correo + ", foto=" + foto + ", uperfil=" + uperfil + ", fase=" + fase
				+ ", fecha_fin=" + fecha_fin + ", verificado=" + verificado + "]";
	}

	

	

	

	

	

}
