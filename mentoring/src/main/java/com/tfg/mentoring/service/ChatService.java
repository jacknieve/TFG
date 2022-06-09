package com.tfg.mentoring.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.tfg.mentoring.exceptions.ExcepcionRecursos;
import com.tfg.mentoring.model.MensajeChat;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.SalaChat;
import com.tfg.mentoring.model.auxiliar.CuerpoMensaje;
import com.tfg.mentoring.model.auxiliar.MensajeConAsunto;
import com.tfg.mentoring.model.auxiliar.DTO.MensajeChatDTO;
import com.tfg.mentoring.model.auxiliar.DTO.SalaChatDTO;
import com.tfg.mentoring.model.auxiliar.enums.AsuntoMensaje;
import com.tfg.mentoring.repository.MensajesRepo;
import com.tfg.mentoring.repository.SalaChatRepo;

@Service
public class ChatService {

	@Autowired
	private SalaChatRepo srepo;
	@Autowired
	private MensajesRepo mrepo;

	@Autowired
	private ActiveUsersService acservice;
	@Autowired
	private FileService fservice;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	// Aqui el deMentor se refiere a si el otro usuario es mentor
	public SalaChat getSalaUsuarios(String emisor, String receptor, boolean deMentor) throws JDBCConnectionException, QueryTimeoutException {

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
	
	public Long getIdSalaUsuarios(String mentor, String mentorizado) throws JDBCConnectionException, QueryTimeoutException{

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

	public List<Long> getSalasConMensajesNuevos(String username, boolean mentor) throws JDBCConnectionException, QueryTimeoutException{

		if (mentor) {
			return srepo.nuevosMentor(username);
		} else {
			return srepo.nuevosMentorizado(username);
		}
	}

	public void abrirChat(Mentor mentor, Mentorizado mentorizado) throws JDBCConnectionException, QueryTimeoutException{
		SalaChat sala = new SalaChat(mentor, mentorizado);
		sala = srepo.save(sala);
		if (acservice.activo(mentorizado.getCorreo()) && acservice.enChat(mentorizado.getCorreo())) {
			String nombre = mentor.getNombre() + " " + mentor.getPapellido() + " " + mentor.getSapellido();
			String foto = "";
			if(mentor.getUsuario().getFoto() != null) {
				foto = "/images/usuarios/mentores/" + mentor.getCorreo() + "/" + mentor.getUsuario().getFoto();
			}
			messagingTemplate.convertAndSendToUser(mentorizado.getCorreo(), "/queue/messages", new MensajeConAsunto(
					AsuntoMensaje.CONTACTO, new SalaChatDTO(sala.getId_sala(), mentor.getCorreo(), nombre, false, foto)));
		}
		try {
			fservice.crearDirectoriosChat(sala.getId_sala());
		}catch (ExcepcionRecursos | SecurityException e) {
			try {
				srepo.delete(sala);
				if (acservice.activo(mentorizado.getCorreo()) && acservice.enChat(mentorizado.getCorreo())) {
					messagingTemplate.convertAndSendToUser(mentorizado.getCorreo(), "/queue/messages", new MensajeConAsunto(
							AsuntoMensaje.CONTACTO, new SalaChatDTO(0, mentor.getCorreo(), null, true, null)));
				}
				
			} catch (JDBCConnectionException | QueryTimeoutException ex) {
				System.out.println("No ha sido posible limpiar la sala de chat en la base de datos.");
			}
		}
		

	}

	public void cerrarChat(String mentor, String mentorizado, boolean fueMentor) throws JDBCConnectionException, QueryTimeoutException {
		Optional<Long> id = srepo.getIdByMentorAndMentorizado(mentor, mentorizado);
		
		if(id.isPresent()) {
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
		try {
			fservice.limpiarFilesSala(id.get());
		} catch (IOException e) {//Aqui el usuario no puede hacer nada, se tendría que registrar para que lo resolviese el administrador
			System.out.println("No ha sido posible limpiar el directorio de la sala "+id.get()+".");
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println("Se ha producido un error al intentar limpiar el directorio de la sala de chat " + id.get() + ".");
			System.out.println(e.getMessage());
		}
		}
	}

	// Cerrar todos los chats de un mentor al este eliminar su cuenta
	public void cerrarChatSalirMentor(String username) throws JDBCConnectionException, QueryTimeoutException {
		List<SalaChat> salas = srepo.findByMentor(username);
		for(SalaChat s : salas) {
			if (acservice.activo(s.getMentorizado().getCorreo()) && acservice.enChat(s.getMentorizado().getCorreo())) {
				messagingTemplate.convertAndSendToUser(s.getMentorizado().getCorreo(), "/queue/messages",
						new MensajeConAsunto(AsuntoMensaje.CONTACTO, new SalaChatDTO(0, username, null, true, null)));
			}
			try {
				fservice.limpiarFilesSala(s.getId_sala());
			} catch (IOException e) {//Aqui el usuario no puede hacer nada, se tendría que registrar para que lo resolviese el administrador
				System.out.println("No ha sido posible limpiar el directorio de la sala "+s.getId_sala()+".");
			} catch (ExcepcionRecursos e) {
				System.out.println(e.getMessage());
			} catch (Exception e) {
				System.out.println("Se ha producido un error al intentar limpiar el directorio de la sala de chat " + s.getId_sala() + ".");
				System.out.println(e.getMessage());
			}
		}
		srepo.salirTodosChatsMentor(username);
	}

	// Lo mismo que la anterior con mentorizado
	public void cerrarChatSalirMentorizado(String username) throws JDBCConnectionException, QueryTimeoutException{
		List<SalaChat> salas = srepo.findByMentorizado(username);
		for(SalaChat s : salas) {
			if (acservice.activo(s.getMentor().getCorreo()) && acservice.enChat(s.getMentor().getCorreo())) {
				messagingTemplate.convertAndSendToUser(s.getMentor().getCorreo(), "/queue/messages",
						new MensajeConAsunto(AsuntoMensaje.CONTACTO, new SalaChatDTO(0, username, null, true, null)));
			}
			try {
				fservice.limpiarFilesSala(s.getId_sala());
			} catch (IOException e) {//Aqui el usuario no puede hacer nada, se tendría que registrar para que lo resolviese el administrador
				System.out.println("No ha sido posible limpiar el directorio de la sala "+s.getId_sala()+".");
			} catch (ExcepcionRecursos e) {
				System.out.println(e.getMessage());
			} catch (Exception e) {
				System.out.println("Se ha producido un error al intentar limpiar el directorio de la sala de chat " + s.getId_sala() + ".");
				System.out.println(e.getMessage());
			}
		}
		srepo.salirTodosChatsMentorizado(username);
	}

	// Guardar un nuevo mensaje de una sala de chat
	public void saveMensaje(MensajeChat mensaje) throws JDBCConnectionException, QueryTimeoutException {
		mrepo.save(mensaje);
	}

	public List<MensajeChatDTO> getMensajes(long id, boolean mentor) throws JDBCConnectionException, QueryTimeoutException {
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
	
	public void borrarFileChat(String username, long id, String filename, boolean mentor) throws JDBCConnectionException, QueryTimeoutException{
		mrepo.actualizarFileChat(filename + " (borrado)", true, id, mentor, false, filename);
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
	
	public boolean comprobarSalaUsuario(long id, String username, String rol) throws JDBCConnectionException, QueryTimeoutException {
		int n;
		if(rol.equals("mentor")) {
			n = srepo.existeSalaMentorizado(id, username);
		}
		else {
			n = srepo.existeSalaMentor(id, username);
		}
		return n == 1;
		
	}
	
	public void enviarMensaje(int asunto, String receptor, CuerpoMensaje cuerpo) {
		switch (asunto) {
		case 0:
			messagingTemplate.convertAndSendToUser(receptor, "/queue/messages",
					new MensajeConAsunto(AsuntoMensaje.MENSAJE, cuerpo));
			break;
		case 1:
			messagingTemplate.convertAndSendToUser(receptor, "/queue/messages",
					new MensajeConAsunto(AsuntoMensaje.CONTACTO, cuerpo));
			break;
		case 2:
			messagingTemplate.convertAndSendToUser(receptor, "/queue/messages",
					new MensajeConAsunto(AsuntoMensaje.NOTIFICACION, cuerpo));
			break;
		case 3:
			messagingTemplate.convertAndSendToUser(receptor, "/queue/messages",
					new MensajeConAsunto(AsuntoMensaje.ERROR, cuerpo));
			break;
		case 4:
			messagingTemplate.convertAndSendToUser(receptor, "/queue/messages", 
					new MensajeConAsunto(AsuntoMensaje.MENSAJEERROR, cuerpo));
			break;

		default:
			break;
		}
		
		
	}
	
	
	

}
