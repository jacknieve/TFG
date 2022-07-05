package com.tfg.mentoring.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.SalaChat;
import com.tfg.mentoring.model.auxiliar.DTO.MentorDTO;
import com.tfg.mentoring.model.auxiliar.DTO.MentorInfoDTO;
import com.tfg.mentoring.model.auxiliar.DTO.PerfilDTO;
import com.tfg.mentoring.model.auxiliar.DTO.PeticionInfoDTO;
import com.tfg.mentoring.model.auxiliar.DTO.SalaChatDTO;
import com.tfg.mentoring.model.auxiliar.DTO.UsuarioDTO;

@Service
public class MapeadoService {

	@Autowired
	private ModelMapper maper;

	
	@Autowired
	private FileService fservice;
	@Autowired
	private ChatService cservice;
	
	
	
	public PerfilDTO getPerfilMentor(Mentor mentor) {
		PerfilDTO user = new PerfilDTO();
		user = maper.map(mentor, PerfilDTO.class);
		if(mentor.getUsuario().getFoto() == null) {
			user.setFoto("/images/usuario.png");
		}
		else {
			user.setFoto("/imagenes/mentores/" + mentor.getCorreo() + "/" + mentor.getUsuario().getFoto());
			
		}
		user.setFicheros(fservice.getFicherosUser(mentor.getCorreo()));
		return user;
	}

	public PerfilDTO getPerfilMentorizado(Mentorizado mentorizado) {
		PerfilDTO user = new PerfilDTO();
		user = maper.map(mentorizado, PerfilDTO.class);
		if(mentorizado.getUsuario().getFoto() == null) {
			user.setFoto("/images/usuario.png");
			
		}
		else {
			user.setFoto("/imagenes/mentorizados/" + mentorizado.getCorreo() + "/" + mentorizado.getUsuario().getFoto());
		}
		user.setFicheros(fservice.getFicherosUser(mentorizado.getCorreo()));
		return user;
	}
	
	public UsuarioDTO getMentorMentorizacion(Mentor mentor) {
		UsuarioDTO user = new UsuarioDTO();
		user = maper.map(mentor, UsuarioDTO.class);
		if (mentor.getFnacimiento() != null) {
			LocalDate fnac = Instant.ofEpochMilli(mentor.getFnacimiento().getTime()).atZone(ZoneId.systemDefault())
					.toLocalDate();
			user.setEdad(Period.between(fnac, LocalDate.now()).getYears());
		}
		if(mentor.getUsuario().getFoto() == null) {
			user.setFoto("/images/usuario.png");
		}
		else {
			user.setFoto("/imagenes/mentores/" + mentor.getCorreo() + "/" + mentor.getUsuario().getFoto());
			
		}
		user.setFicheros(fservice.getFicherosUser(mentor.getCorreo()));

		return user;
	}

	public UsuarioDTO getMentorizadoMentorizacion(Mentorizado mentorizado) {
		UsuarioDTO user = new UsuarioDTO();
		user = maper.map(mentorizado, UsuarioDTO.class);
		if (mentorizado.getFnacimiento() != null) {
			LocalDate fnac = Instant.ofEpochMilli(mentorizado.getFnacimiento().getTime()).atZone(ZoneId.systemDefault())
					.toLocalDate();
			user.setEdad(Period.between(fnac, LocalDate.now()).getYears());
		}
		if(mentorizado.getUsuario().getFoto() == null) {
			user.setFoto("/images/usuario.png");
			
		}
		else {
			user.setFoto("/imagenes/mentorizados/" + mentorizado.getCorreo() + "/" + mentorizado.getUsuario().getFoto());
		}
		user.setFicheros(fservice.getFicherosUser(mentorizado.getCorreo()));
		return user;
	}
	
	public UsuarioDTO getMentorInfo(Mentor mentor) {
		UsuarioDTO user = new UsuarioDTO();
		user = maper.map(mentor, UsuarioDTO.class);
		if (mentor.getFnacimiento() != null) {
			LocalDate fnac = Instant.ofEpochMilli(mentor.getFnacimiento().getTime()).atZone(ZoneId.systemDefault())
					.toLocalDate();
			user.setEdad(Period.between(fnac, LocalDate.now()).getYears());
		}
		if(mentor.getUsuario().getFoto() == null) {
			user.setFoto("/images/usuario.png");
		}
		else {
			user.setFoto("/imagenes/mentores/" + mentor.getCorreo() + "/" + mentor.getUsuario().getFoto());
			
		}

		return user;
	}
	
	public MentorInfoDTO getMentorInfoBusqueda(Mentor mentor) {
		MentorInfoDTO user = new MentorInfoDTO(mentor.getLinkedin(), 0, mentor.getDescripcion(), mentor.getAreas());
		if (mentor.getFnacimiento() != null) {
			LocalDate fnac = Instant.ofEpochMilli(mentor.getFnacimiento().getTime()).atZone(ZoneId.systemDefault())
					.toLocalDate();
			user.setEdad(Period.between(fnac, LocalDate.now()).getYears());
		}

		return user;
	}

	public PeticionInfoDTO getMentorizadoInfo(Mentorizado mentorizado) {
		PeticionInfoDTO user = new PeticionInfoDTO();
		user = maper.map(mentorizado, PeticionInfoDTO.class);
		if (mentorizado.getFnacimiento() != null) {
			LocalDate fnac = Instant.ofEpochMilli(mentorizado.getFnacimiento().getTime()).atZone(ZoneId.systemDefault())
					.toLocalDate();
			user.setEdad(Period.between(fnac, LocalDate.now()).getYears());
		}
		if(mentorizado.getUsuario().getFoto() == null) {
			user.setFoto("/images/usuario.png");
			
		}
		else {
			user.setFoto("/imagenes/mentorizados/" + mentorizado.getCorreo() + "/" + mentorizado.getUsuario().getFoto());
		}

		return user;
	}
	
	public List<MentorDTO> getMentorBusqueda(List<Mentor> mentores) {
		return mentores.stream().map(this::convertMentortoDTO).collect(Collectors.toList());
	}

	private MentorDTO convertMentortoDTO(Mentor m) {
		MentorDTO user = new MentorDTO();
		user = maper.map(m, MentorDTO.class);
		if(m.getUsuario().getFoto() == null) {
			user.setFoto("/images/usuario.png");
		}
		else {
			user.setFoto("/imagenes/mentores/" + m.getCorreo() + "/" + m.getUsuario().getFoto());
		}
		return user;
	}
	
	public List<SalaChatDTO> convertSalasToDTOMentor(List<SalaChat> salas, String username){
		List<SalaChatDTO> salasDto = new ArrayList<>();
		List<Long> conNuevos = cservice.getSalasConMensajesNuevos(username, true);
		for (SalaChat s : salas) {
			Mentorizado m = s.getMentorizado();
			String nombre = m.getNombre() + " " + m.getPapellido() + " " + m.getSapellido();
			String foto;
			if (m.getUsuario().getFoto() != null) {
				foto = "/imagenes/mentorizados/" + m.getCorreo() + "/" + m.getUsuario().getFoto();
			} else {
				foto = "/images/usuario.png";
			}
			SalaChatDTO sala = new SalaChatDTO(s.getId_sala(), m.getCorreo(), nombre, false, foto);
			if (conNuevos.contains(s.getId_sala())) {
				sala.setNuevos(true);
			}
			salasDto.add(sala);
		}
		return salasDto;
	}
	
	public List<SalaChatDTO> convertSalasToDTOMentorizado(List<SalaChat> salas, String username){
		List<SalaChatDTO> salasDto = new ArrayList<>();
		List<Long> conNuevos = cservice.getSalasConMensajesNuevos(username, false);
		for (SalaChat s : salas) {
			Mentor m = s.getMentor();
			String nombre = m.getNombre() + " " + m.getPapellido() + " " + m.getSapellido();
			String foto;
			if (m.getUsuario().getFoto() != null) {
				foto = "/imagenes/mentores/" + m.getCorreo() + "/" + m.getUsuario().getFoto();
			} else {
				foto = "/images/usuario.png";
			}
			SalaChatDTO sala = new SalaChatDTO(s.getId_sala(), m.getCorreo(), nombre, false, foto);
			if (conNuevos.contains(s.getId_sala())) {
				sala.setNuevos(true);
			}
			salasDto.add(sala);
		}
		return salasDto;
	}

}
