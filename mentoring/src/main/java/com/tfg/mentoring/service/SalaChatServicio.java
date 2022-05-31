package com.tfg.mentoring.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.tfg.mentoring.model.MensajeChat;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.SalaChat;
import com.tfg.mentoring.model.auxiliar.MensajeConAsunto;
import com.tfg.mentoring.model.auxiliar.DTO.MensajeChatDTO;
import com.tfg.mentoring.model.auxiliar.DTO.SalaChatDTO;
import com.tfg.mentoring.model.auxiliar.enums.AsuntoMensaje;
import com.tfg.mentoring.repository.MensajesRepo;
import com.tfg.mentoring.repository.SalaChatRepo;

@Service
public class SalaChatServicio {

	@Autowired
	private SalaChatRepo srepo;

	@Autowired
	private MensajesRepo mrepo;

	@Autowired
	private ActiveUsersService acservice;

	// Esto habria que ponerlo en otro sitio
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	// Aqui el deMentor se refiere a si el otro usuario es mentor
	public SalaChat getSalaUsuarios(String emisor, String receptor, boolean deMentor) {

		Optional<SalaChat> sala;
		if (deMentor) {
			sala = srepo.findByMentorAndMentorizado(emisor, receptor);
		} else {
			sala = srepo.findByMentorAndMentorizado(receptor, emisor);
		}
		if (sala.isPresent()) {
			return sala.get();
		} else {
			return null; // Null o exception, o la excepcion se ve en el controller, ya veremos
			// Incluso una notificacion al usuario
		}

	}
	
	public Long getIdSalaUsuarios(String mentor, String mentorizado) {

		Optional<Long> sala = srepo.getIdByMentorAndMentorizado(mentor, mentorizado);
		if (sala.isPresent()) {
			return sala.get();
		} else {
			return null; // Null o exception, o la excepcion se ve en el controller, ya veremos
			// Incluso una notificacion al usuario
		}

	}

	public List<SalaChat> getSalasUsuario(String username, boolean mentor)
			throws JDBCConnectionException, QueryTimeoutException {
		if (mentor) {
			return srepo.findByMentor(username);
		} else {
			return srepo.findByMentorizado(username);
		}

	}

	public List<Long> getSalasConMensajesNuevos(String username, boolean mentor) {

		if (mentor) {
			return srepo.nuevosMentor(username);
		} else {
			return srepo.nuevosMentorizado(username);
		}
	}

	public void abrirChat(Mentor mentor, Mentorizado mentorizado) {
		SalaChat sala = new SalaChat(mentor, mentorizado);
		srepo.save(sala);
		if (acservice.activo(mentorizado.getCorreo()) && acservice.enChat(mentorizado.getCorreo())) {
			String nombre = mentor.getNombre() + " " + mentor.getPapellido() + " " + mentor.getSapellido();
			String foto = "";
			if(mentor.getUsuario().getFoto() != null) {
				foto = "/images/usuarios/mentores/" + mentor.getCorreo() + "/" + mentor.getUsuario().getFoto();
			}
			messagingTemplate.convertAndSendToUser(mentorizado.getCorreo(), "/queue/messages", new MensajeConAsunto(
					AsuntoMensaje.CONTACTO, new SalaChatDTO(sala.getId_sala(), mentor.getCorreo(), nombre, false, foto)));
		}

	}

	public void cerrarChat(String mentor, String mentorizado, boolean fueMentor) {
		srepo.salirChat(mentor, mentorizado);
		if (fueMentor) {
			if (acservice.activo(mentorizado) && acservice.enChat(mentorizado)) {
				messagingTemplate.convertAndSendToUser(mentorizado, "/queue/messages",
						new MensajeConAsunto(AsuntoMensaje.CONTACTO, new SalaChatDTO(0, mentor, null, true, null)));
			}
		} else {
			if (acservice.activo(mentor) && acservice.enChat(mentor)) {
				messagingTemplate.convertAndSendToUser(mentor, "/queue/messages",
						new MensajeConAsunto(AsuntoMensaje.CONTACTO, new SalaChatDTO(0, mentorizado, null, true, null)));
			}
		}
	}

	// Cerrar todos los chats de un mentor al este eliminar su cuenta
	public void cerrarChatSalirMentor(String username) {
		List<SalaChat> salas = srepo.findByMentor(username);
		for(SalaChat s : salas) {
			if (acservice.activo(s.getMentorizado().getCorreo()) && acservice.enChat(s.getMentorizado().getCorreo())) {
				messagingTemplate.convertAndSendToUser(s.getMentorizado().getCorreo(), "/queue/messages",
						new MensajeConAsunto(AsuntoMensaje.CONTACTO, new SalaChatDTO(0, username, null, true, null)));
			}
		}
		srepo.salirTodosChatsMentor(username);
	}

	// Lo mismo que la anterior con mentorizado
	public void cerrarChatSalirMentorizado(String username) {
		List<SalaChat> salas = srepo.findByMentorizado(username);
		for(SalaChat s : salas) {
			if (acservice.activo(s.getMentor().getCorreo()) && acservice.enChat(s.getMentor().getCorreo())) {
				messagingTemplate.convertAndSendToUser(s.getMentor().getCorreo(), "/queue/messages",
						new MensajeConAsunto(AsuntoMensaje.CONTACTO, new SalaChatDTO(0, username, null, true, null)));
			}
		}
		srepo.salirTodosChatsMentorizado(username);
	}

	// Guardar un nuevo mensaje de una sala de chat
	public void saveMensaje(MensajeChat mensaje) {
		mrepo.save(mensaje);
	}

	public List<MensajeChatDTO> getMensajes(long id, boolean mentor) {
		List<MensajeChatDTO> resultado = new ArrayList<>();
		if (mentor) {
			resultado = mrepo.findBySala(id).stream().map(this::convertMensajeChatToDTO).collect(Collectors.toList());
			mrepo.actualizarEstadoMensajes(id, !mentor);
		} else {
			resultado = mrepo.findBySala(id).stream().map(this::convertMensajeChatToDTO).collect(Collectors.toList());
			mrepo.actualizarEstadoMensajes(id, !mentor);
		}
		return resultado;
	}

	private MensajeChatDTO convertMensajeChatToDTO(MensajeChat m) {
		return new MensajeChatDTO(m);
	}
	
	public boolean borrarFileChat(String username, long id, String filename, boolean mentor) throws JDBCConnectionException, QueryTimeoutException{
		int n;
		if(mentor) {
			//Hago primero esta, porque en caso de que una de las dos falle, esta no provoca cambios en la DB
			n = srepo.countFileChatMentor(username, filename);
			
		}
		else {
			n = srepo.countFileChatMentorizado(username, filename);
		}
		System.out.println(n);
		mrepo.actualizarFileChat(filename + " (borrado)", true, id, mentor, false, filename);
		
		return n == 1;
	}
	
	public void restoreFileChat(long id, String filename, boolean mentor){
		try {
		mrepo.actualizarFileChat(filename, false, id, mentor, true, filename + " (borrado)");
		}
		catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			//Aqui habria que registrar esto de tener un log, porque es algo que se tendria que arrgelar una vez estuviese disponible
			System.out.println("No ha sido posible restaurar el mensaje en la base de datos");
		}
		
	}
	

}
