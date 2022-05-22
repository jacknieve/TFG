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
			messagingTemplate.convertAndSendToUser(mentorizado.getCorreo(), "/queue/messages", new MensajeConAsunto(
					AsuntoMensaje.CONTACTO, new SalaChatDTO(sala.getId_sala(), mentor.getCorreo(), nombre, false)));
		}

	}

	public void cerrarChat(String mentor, String mentorizado, boolean fueMentor) {
		srepo.salirChat(mentor, mentorizado);
		if (fueMentor) {
			if (acservice.activo(mentorizado) && acservice.enChat(mentorizado)) {
				messagingTemplate.convertAndSendToUser(mentorizado, "/queue/messages",
						new MensajeConAsunto(AsuntoMensaje.CONTACTO, new SalaChatDTO(0, mentor, null, true)));
			}
		} else {
			if (acservice.activo(mentor) && acservice.enChat(mentor)) {
				messagingTemplate.convertAndSendToUser(mentor, "/queue/messages",
						new MensajeConAsunto(AsuntoMensaje.CONTACTO, new SalaChatDTO(0, mentorizado, null, true)));
			}
		}
	}

	// Cerrar todos los chats de un mentor al este eliminar su cuenta
	public void cerrarChatSalirMentor(String username) {
		srepo.salirTodosChatsMentor(username);
	}

	// Lo mismo que la anterior con mentorizado
	public void cerrarChatSalirMentorizado(String username) {
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

}
