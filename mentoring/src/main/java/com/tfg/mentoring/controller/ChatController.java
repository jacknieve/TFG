package com.tfg.mentoring.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.model.MensajeChat;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.SalaChat;
import com.tfg.mentoring.model.auxiliar.MensajeConAsunto;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.DTO.MensajeChatDTO;
import com.tfg.mentoring.model.auxiliar.DTO.NotificacionDTO;
import com.tfg.mentoring.model.auxiliar.DTO.SalaChatDTO;
import com.tfg.mentoring.model.auxiliar.enums.AsuntoMensaje;
import com.tfg.mentoring.model.auxiliar.enums.EstadoMensaje;
import com.tfg.mentoring.model.auxiliar.enums.MotivosNotificacion;
import com.tfg.mentoring.model.auxiliar.enums.Roles;
import com.tfg.mentoring.model.auxiliar.requests.MensajeReenvio;
import com.tfg.mentoring.model.auxiliar.requests.MensajesGet;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.service.ActiveUsersService;
import com.tfg.mentoring.service.SalaChatServicio;
import com.tfg.mentoring.service.UserService;

@Controller
public class ChatController {

	@Autowired
	private SalaChatServicio salaChats;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private ActiveUsersService acservice;
	@Autowired
	private UserService uservice;

	@Autowired
	private MentorizadoRepo menrepo;
	@Autowired
	private MentorRepo mrepo;

	@MessageMapping("/send")
	public void procesarMensaje(@Payload MensajeReenvio mensaje) {
		// Asumimos que el receptor existe (esto se puede dejar de
		// asumir en un futuro y añadir comprobacion)
		try {
			SalaChat sala = salaChats.getSalaUsuarios(mensaje.getEmisor(), mensaje.getReceptor(), mensaje.isDeMentor());
			if (sala == null) {
				// Salida de error
			}
			MensajeChat msg = new MensajeChat(mensaje.getContenido(), sala, mensaje.isDeMentor());
			
			System.out.println(msg.toString());
			if (acservice.activo(mensaje.getReceptor())) {
				if (acservice.enChat(mensaje.getReceptor())) {
					msg.setEstado(EstadoMensaje.ENTREGADO);

					salaChats.saveMensaje(msg);// Esto lo pongo varia veces por que en este caso debe de actualizase
												// para indicar que ya se entrego
					messagingTemplate.convertAndSendToUser(mensaje.getReceptor(), "/queue/messages",
							new MensajeConAsunto(AsuntoMensaje.MENSAJE, new MensajeChatDTO(msg)));
					System.out.println("Nuevo mensaje : " + mensaje.toString());
				} else {
					// Notificar
					salaChats.saveMensaje(msg);
					uservice.enviarNotificacionMensaje(mensaje.getReceptor(), false);
				}
			} else {
				// Notificar por correo
				salaChats.saveMensaje(msg);
				uservice.enviarNotificacionMensaje(mensaje.getReceptor(), true);
			}

		} catch (DataIntegrityViolationException e) {
			System.out.println(
					"Error al guardar el mensaje, seguramente producido por no existir la sala de chat (tambien se puede dar si ya hay un msg con el mismo id)");
			System.out.println(e.getMessage());
			messagingTemplate.convertAndSendToUser(mensaje.getEmisor(), "/queue/messages",
					new MensajeConAsunto(AsuntoMensaje.ERROR, new NotificacionDTO(0, "Fallo al enviar el mensaje", 
							"Se ha producido un fallo interno al intentar almcanenar el mensaje, por favor, pongase en contacto con nosotros si recibe este error", 
							null, true, MotivosNotificacion.ERROR)));
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println("Error al guardar el mensaje en la db: "+e.getMessage());
			messagingTemplate.convertAndSendToUser(mensaje.getEmisor(), "/queue/messages",
					new MensajeConAsunto(AsuntoMensaje.ERROR, new NotificacionDTO(0, "Fallo al enviar el mensaje", 
							"No ha sido posible almacenar el mensaje en el repositorio, por favor, vuelva a intentarlo más tarde", 
							null, true, MotivosNotificacion.ERROR)));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			messagingTemplate.convertAndSendToUser(mensaje.getEmisor(), "/queue/messages",
					new MensajeConAsunto(AsuntoMensaje.ERROR, new NotificacionDTO(0, "Fallo al enviar el mensaje", 
							"Se ha producido un error interno al enviar el mensaje, por favor, pongase en contacto con nosotros si recibe este error", 
							null, true, MotivosNotificacion.ERROR)));
		}

	}

	// Obtener ventana de chat
	@GetMapping("/chat")
	public ModelAndView getPaginaChat(@AuthenticationPrincipal UserAuth us) {
		try {
			// System.out.println(us.toString());
			if (us.getRol() == Roles.MENTOR) {
				Optional<Mentor> mentor = mrepo.findById(us.getUsername());
				if (mentor.isPresent()) {
					// Asumimos que el usuario se considera logeado
					acservice.entrarChat(us.getUsername());
					ModelAndView modelo = new ModelAndView("chat");
					Mentor m = mentor.get();
					uservice.addInstitucionUtils(modelo, m.getInstitucion());
					modelo.addObject("nombre", m.getNombre() + " " + m.getPapellido() + " " + m.getSapellido());
					// Aqui faltaria el path de la foto de perfil
					return modelo;
				} else {
					System.out.println("No existe");
					ModelAndView model = new ModelAndView("error_page");
					model.addObject("mensaje",
							"No ha sido posible acceder a la información de su perfil, por favor, si recibe este mensaje, "
									+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
					model.addObject("hora", new Date());
					return model;
				}
			} else if (us.getRol() == Roles.MENTORIZADO) {
				Optional<Mentorizado> mentorizado = menrepo.findById(us.getUsername());
				if (mentorizado.isPresent()) {
					acservice.entrarChat(us.getUsername());
					System.out.println(mentorizado.get().toString());
					Mentorizado m = mentorizado.get();
					ModelAndView modelo = new ModelAndView("chat");
					uservice.addInstitucionUtils(modelo, m.getInstitucion());
					modelo.addObject("nombre", m.getNombre() + " " + m.getPapellido() + " " + m.getSapellido());
					return modelo;
				} else {
					System.out.println("No existe");
					ModelAndView model = new ModelAndView("error_page");
					model.addObject("mensaje",
							"No ha sido posible acceder a la información de su perfil, por favor, si recibe este mensaje, "
									+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
					model.addObject("hora", new Date());
					return model;
				}
			} else {
				System.out.println("Otro rol");
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje", "No estas autorizado a acceder a esta página con tu rol actual.");
				model.addObject("hora", new Date());
				return model;
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.toString());
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje", "No ha sido posible acceder al repositorio de la aplicación, por favor, inténtelo más tarde");
			model.addObject("hora", new Date());
			return model;
		}catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.toString());
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje", "Se ha producido un error inesperado en el servidor, del tipo: "
					+ e.getClass().getCanonicalName() + ", por favor, si recibe este mensaje, "
					+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			model.addObject("hora", new Date());
			return model;
		}
	}

	// Recuperar chats a partir de crendenciales

	@GetMapping("/chat/chatsuser")
	public ResponseEntity<List<SalaChatDTO>> getChats(@AuthenticationPrincipal UserAuth u) {
		List<SalaChatDTO> resultado = new ArrayList<>();
		if (u.getRol() == Roles.MENTOR) {
			try {
				List<SalaChat> salas = new ArrayList<>();
				salas = salaChats.getSalasUsuario(u.getUsername(), true);
				if (salas.isEmpty()) {
					System.out.println("No hay chats abiertos");
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
				List<Long> conNuevos = new ArrayList<>();
				conNuevos = salaChats.getSalasConMensajesNuevos(u.getUsername(), true);
				for (SalaChat s : salas) {
					String nombre = s.getMentorizado().getNombre() + " " + s.getMentorizado().getPapellido() + " "
							+ s.getMentorizado().getSapellido();
					SalaChatDTO sala = new SalaChatDTO(s.getId_sala(), s.getMentorizado().getCorreo(), nombre, false);
					if (conNuevos.contains(s.getId_sala())) {
						sala.setNuevos(true);
					}
					resultado.add(sala);
				}
				return new ResponseEntity<>(resultado, HttpStatus.OK);
			} catch (JDBCConnectionException | QueryTimeoutException e) {
				System.out.println(e.getMessage());
				return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else if (u.getRol() == Roles.MENTORIZADO) {
			try {
				List<SalaChat> salas = new ArrayList<>();
				salas = salaChats.getSalasUsuario(u.getUsername(), false);
				if (salas.isEmpty()) {
					System.out.println("No hay chats abiertos");
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
				List<Long> conNuevos = new ArrayList<>();
				conNuevos = salaChats.getSalasConMensajesNuevos(u.getUsername(), false);
				for (SalaChat s : salas) {
					String nombre = s.getMentor().getNombre() + " " + s.getMentor().getPapellido() + " "
							+ s.getMentor().getSapellido();
					SalaChatDTO sala = new SalaChatDTO(s.getId_sala(), s.getMentor().getCorreo(), nombre, false);
					if (conNuevos.contains(s.getId_sala())) {
						sala.setNuevos(true);
					}
					resultado.add(sala);
				}
				return new ResponseEntity<>(resultado, HttpStatus.OK);
			} catch (JDBCConnectionException | QueryTimeoutException e) {
				System.out.println(e.getMessage());
				return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	// Recuperar mensajes de un chat
	@PostMapping("/chat/mensajes")
	public ResponseEntity<List<MensajeChatDTO>> getChats(@RequestBody MensajesGet peticion) {
		if (peticion == null) {
			System.out.println("La peticion de mensajes estaba a null");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		try {
			List<MensajeChatDTO> mensajes = new ArrayList<>();
			mensajes = salaChats.getMensajes(peticion.getId(), peticion.isMentor());
			if (mensajes.isEmpty()) {
				System.out.println("No hay mensajes en ese chat");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(mensajes, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@PostMapping("/chat/idchat")
	public ResponseEntity<Long> getSalaId(@RequestBody String otroUsuario, @AuthenticationPrincipal UserAuth u) {
		if (otroUsuario == null) {
			System.out.println("El otro usuario estaba a null");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		try {
			Long id;
			if(u.getRol() == Roles.MENTOR) {
				id = salaChats.getIdSalaUsuarios(u.getUsername(), otroUsuario);
			} else if(u.getRol() == Roles.MENTORIZADO) {
				id = salaChats.getIdSalaUsuarios(otroUsuario, u.getUsername());
			}
			else {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			if (id == null) {
				System.out.println("No hay un chat abierto");
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(id, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	/*
	 * @Autowired private SalaChatRepo srepo;
	 *
	 * @GetMapping("/perror") public ModelAndView perror() { Optional<Mentor> m =
	 * mrepo.findById("jacknieve@gmail.com"); Optional<Mentorizado> men =
	 * menrepo.findById("solowowglez@gmail.com"); Optional<Mentorizado> men2 =
	 * menrepo.findById("jonhelcapo2@gmail.com"); Optional<Mentorizado> men3 =
	 * menrepo.findById("siemprehistoria@gmail.com"); srepo.save(new
	 * SalaChat(m.get(), men.get())); srepo.save(new SalaChat(m.get(), men2.get()));
	 * srepo.save(new SalaChat(m.get(), men3.get())); ModelAndView model = new
	 * ModelAndView("error_page"); model.addObject("mensaje",
	 * "No ha sido posible acceder a la información de su perfil, por favor, si recibe este mensaje, "
	 * +
	 * "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error."
	 * ); model.addObject("hora", new Date()); return model; }
	 */

	/*
	 * @GetMapping("/chat/prueba") public ResponseEntity<List<Long>>
	 * getPrueba(@AuthenticationPrincipal UserAuth u) { return new
	 * ResponseEntity<>(salaChats.getSalasConMensajesNuevos(u.getUsername(), true),
	 * HttpStatus.OK);
	 * 
	 * }
	 */

}
