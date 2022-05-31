package com.tfg.mentoring.controller;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.exceptions.ExcepcionDB;
import com.tfg.mentoring.exceptions.ExcepcionFichero;
import com.tfg.mentoring.exceptions.ExcepcionRecursos;
import com.tfg.mentoring.model.MensajeChat;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.SalaChat;
import com.tfg.mentoring.model.auxiliar.MensajeConAsunto;
import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.DTO.MensajeChatDTO;
import com.tfg.mentoring.model.auxiliar.DTO.NotificacionDTO;
import com.tfg.mentoring.model.auxiliar.DTO.SalaChatDTO;
import com.tfg.mentoring.model.auxiliar.enums.AsuntoMensaje;
import com.tfg.mentoring.model.auxiliar.enums.EstadoMensaje;
import com.tfg.mentoring.model.auxiliar.enums.MotivosNotificacion;
import com.tfg.mentoring.model.auxiliar.requests.MensajeReenvio;
import com.tfg.mentoring.model.auxiliar.requests.MensajesGet;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.service.ActiveUsersService;
import com.tfg.mentoring.service.FileService;
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
	private FileService fservice;

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
				messagingTemplate.convertAndSendToUser(mensaje.getEmisor(), "/queue/messages", new MensajeConAsunto(
						AsuntoMensaje.MENSAJEERROR,
						new NotificacionDTO(0, "Fallo al enviar el mensaje",
								"No se ha encontrado la conversación a donde queria enviar el mensaje, por favor, pongase en contacto con nosotros si recibe este error",
								null, true, MotivosNotificacion.ERROR)));
			}
			MensajeChat msg = new MensajeChat(mensaje.getContenido(), sala, mensaje.isDeMentor(), true);
			if (acservice.activo(mensaje.getReceptor())) {
				if (acservice.enChat(mensaje.getReceptor())) {
					msg.setEstado(EstadoMensaje.ENTREGADO);

					salaChats.saveMensaje(msg);// Esto lo pongo varia veces por que en este caso debe de actualizase
												// para indicar que ya se entrego
					messagingTemplate.convertAndSendToUser(mensaje.getReceptor(), "/queue/messages",
							new MensajeConAsunto(AsuntoMensaje.MENSAJE, new MensajeChatDTO(msg)));
					System.out.println("Nuevo mensaje : " + mensaje.toString() + " de " + mensaje.getEmisor() + " para "
							+ mensaje.getReceptor());
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
			messagingTemplate.convertAndSendToUser(mensaje.getEmisor(), "/queue/messages", new MensajeConAsunto(
					AsuntoMensaje.MENSAJEERROR,
					new NotificacionDTO(0, "Fallo al enviar el mensaje",
							"Se ha producido un fallo interno al intentar almcanenar el mensaje, por favor, pongase en contacto con nosotros si recibe este error",
							null, true, MotivosNotificacion.ERROR)));
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println("Error al guardar el mensaje en la db: " + e.getMessage());
			messagingTemplate.convertAndSendToUser(mensaje.getEmisor(), "/queue/messages", new MensajeConAsunto(
					AsuntoMensaje.MENSAJEERROR,
					new NotificacionDTO(0, "Fallo al enviar el mensaje",
							"No ha sido posible almacenar el mensaje en el repositorio, por favor, vuelva a intentarlo más tarde",
							null, true, MotivosNotificacion.ERROR)));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			messagingTemplate.convertAndSendToUser(mensaje.getEmisor(), "/queue/messages", new MensajeConAsunto(
					AsuntoMensaje.MENSAJEERROR,
					new NotificacionDTO(0, "Fallo al enviar el mensaje",
							"Se ha producido un error interno al enviar el mensaje, por favor, pongase en contacto con nosotros si recibe este error",
							null, true, MotivosNotificacion.ERROR)));
		}

	}

	// Icono usuario
	// https://www.flaticon.es/icono-gratis/usuario_1077063?term=usuario&page=1&position=10&page=1&position=10&related_id=1077063&origin=tag

	// Obtener ventana de chat
	@GetMapping("/chat")
	public ModelAndView getPaginaChat(@AuthenticationPrincipal UserAuth us) {
		try {
			switch (us.getRol()) {
			case MENTOR:
				Optional<Mentor> mentor = mrepo.findById(us.getUsername());
				if (mentor.isPresent()) {
					// Asumimos que el usuario se considera logeado
					acservice.entrarChat(us.getUsername());
					ModelAndView modelo = new ModelAndView("chat");
					Mentor m = mentor.get();
					uservice.addInstitucionUtils(modelo, m.getInstitucion());
					modelo.addObject("nombre", m.getNombre() + " " + m.getPapellido() + " " + m.getSapellido());
					if (m.getUsuario().getFoto() != null) {
						modelo.addObject("foto",
								"/images/usuarios/mentores/" + m.getCorreo() + "/" + m.getUsuario().getFoto());
					} else {
						modelo.addObject("foto", "/images/usuario.png");
					}
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
			case MENTORIZADO:
				Optional<Mentorizado> mentorizado = menrepo.findById(us.getUsername());
				if (mentorizado.isPresent()) {
					acservice.entrarChat(us.getUsername());
					System.out.println(mentorizado.get().toString());
					Mentorizado m = mentorizado.get();
					ModelAndView modelo = new ModelAndView("chat");
					uservice.addInstitucionUtils(modelo, m.getInstitucion());
					modelo.addObject("nombre", m.getNombre() + " " + m.getPapellido() + " " + m.getSapellido());
					if (m.getUsuario().getFoto() != null) {
						modelo.addObject("foto",
								"/images/usuarios/mentorizados/" + m.getCorreo() + "/" + m.getUsuario().getFoto());
					} else {
						modelo.addObject("foto", "/images/usuario.png");
					}
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
			default:
				System.out.println("Otro rol");
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje", "No estas autorizado a acceder a esta página con tu rol actual.");
				model.addObject("hora", new Date());
				return model;
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.toString());
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje",
					"No ha sido posible acceder al repositorio de la aplicación, por favor, inténtelo más tarde");
			model.addObject("hora", new Date());
			return model;
		} catch (Exception e) {
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
		List<SalaChat> salas = new ArrayList<>();
		List<Long> conNuevos = new ArrayList<>();
		try {
			switch (u.getRol()) {
			case MENTOR:
				salas = salaChats.getSalasUsuario(u.getUsername(), true);
				if (salas.isEmpty()) {
					System.out.println(u.getUsername() + " no tiene chats abiertos.");
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
				conNuevos = salaChats.getSalasConMensajesNuevos(u.getUsername(), true);
				for (SalaChat s : salas) {
					Mentorizado m = s.getMentorizado();
					String nombre = m.getNombre() + " " + m.getPapellido() + " " + m.getSapellido();
					String foto;
					if (m.getUsuario().getFoto() != null) {
						foto = "/images/usuarios/mentorizados/" + m.getCorreo() + "/" + m.getUsuario().getFoto();
					} else {
						foto = "/images/usuario.png";
					}
					SalaChatDTO sala = new SalaChatDTO(s.getId_sala(), m.getCorreo(), nombre, false, foto);
					if (conNuevos.contains(s.getId_sala())) {
						sala.setNuevos(true);
					}
					resultado.add(sala);
				}
				//System.out.println("Recuperando las salas de chat de " + u.getUsername());
				return new ResponseEntity<>(resultado, HttpStatus.OK);
			case MENTORIZADO:
				salas = salaChats.getSalasUsuario(u.getUsername(), false);
				if (salas.isEmpty()) {
					System.out.println(u.getUsername() + " no tiene chats abiertos.");
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
				conNuevos = salaChats.getSalasConMensajesNuevos(u.getUsername(), false);
				System.out.println(conNuevos.toString());
				for (SalaChat s : salas) {
					Mentor m = s.getMentor();
					String nombre = m.getNombre() + " " + m.getPapellido() + " " + m.getSapellido();
					String foto;
					if (m.getUsuario().getFoto() != null) {
						foto = "/images/usuarios/mentores/" + m.getCorreo() + "/" + m.getUsuario().getFoto();
					} else {
						foto = "/images/usuario.png";
					}
					SalaChatDTO sala = new SalaChatDTO(s.getId_sala(), m.getCorreo(), nombre, false, foto);
					if (conNuevos.contains(s.getId_sala())) {
						sala.setNuevos(true);
					}
					resultado.add(sala);
				}
				//System.out.println("Recuperando las salas de chat de " + u.getUsername());
				return new ResponseEntity<>(resultado, HttpStatus.OK);
			default:
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Recuperar mensajes de un chat
	@PostMapping("/chat/mensajes")
	public ResponseEntity<List<MensajeChatDTO>> getChats(@RequestBody MensajesGet peticion) {
		try {
			List<MensajeChatDTO> mensajes = new ArrayList<>();
			mensajes = salaChats.getMensajes(peticion.getId(), peticion.isMentor());
			if (mensajes.isEmpty()) {
				System.out.println("No hay mensajes en el chat " + peticion.getId());
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
		try {
			Long id;
			switch (u.getRol()) {
			case MENTOR:
				id = salaChats.getIdSalaUsuarios(u.getUsername(), otroUsuario);
				break;
			case MENTORIZADO:
				id = salaChats.getIdSalaUsuarios(otroUsuario, u.getUsername());
				break;
			default:
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			if (id == null) {
				System.out.println("No hay ningún chat abierto entre " + u.getUsername() + " y " + otroUsuario);
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

	@PostMapping("/chat/sendfile")
	public ResponseEntity<MensajeError> enviarFichero(@RequestParam("file") MultipartFile file,
			@RequestParam("receptor") String receptor, @AuthenticationPrincipal UserAuth u) {
		MensajeChat msg = new MensajeChat();
		try {
			SalaChat sala;
			switch (u.getRol()) {
			case MENTOR:
				sala = salaChats.getSalaUsuarios(u.getUsername(), receptor, true);
				if (sala == null) {
					return new ResponseEntity<>(new MensajeError("Conversación no encontrada",
							"No ha sido posible acceder a la conversación para almacenar el"
									+ " mensaje, por favor, si recibe este mensaje, pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. "
									+ "Hora del suceso: " + new Date()),
							HttpStatus.NOT_FOUND);
				}
				msg = fservice.guardarFicheroSend(file, u.getUsername(), "mentores", sala, true);
				break;
			case MENTORIZADO:
				sala = salaChats.getSalaUsuarios(receptor, u.getUsername(), true);
				if (sala == null) {
					return new ResponseEntity<>(new MensajeError("Conversación no encontrada",
							"No ha sido posible acceder a la conversación para almacenar el"
									+ " mensaje, por favor, si recibe este mensaje, pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. "
									+ "Hora del suceso: " + new Date()),
							HttpStatus.NOT_FOUND);
				}
				msg = fservice.guardarFicheroSend(file, u.getUsername(), "mentorizados", sala, false);
				break;
			default:
				return new ResponseEntity<>(new MensajeError("Sin autorización", "No tienes permiso para hacer esto"),
						HttpStatus.UNAUTHORIZED);
			}
			if (acservice.activo(receptor)) {
				if (acservice.enChat(receptor)) {
					try {
						msg.setEstado(EstadoMensaje.ENTREGADO);

						salaChats.saveMensaje(msg);
					} catch (JDBCConnectionException | QueryTimeoutException e) {
						// Aqui no pasa mucho si no se puede actualizar el estado ahora
						System.out.println(e.getMessage());
					}
					messagingTemplate.convertAndSendToUser(receptor, "/queue/messages",
							new MensajeConAsunto(AsuntoMensaje.MENSAJE, new MensajeChatDTO(msg)));
				} else {
					uservice.enviarNotificacionMensaje(receptor, false);
				}
			} else {
				uservice.enviarNotificacionMensaje(receptor, true);
			}
			//System.out.println("Fichero enviado: " + msg.getContenido() + " por " + u.getUsername() + " para " + receptor);
			return new ResponseEntity<>(new MensajeError(msg.getContenido(), ""), HttpStatus.OK);

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
			fservice.clearFile(u.getUsername(), file.getOriginalFilename());
			return new ResponseEntity<>(
					new MensajeError("Fallo al almacenar el fichero",
							"Se ha producido un fallo al tratar de almacenar el fichero."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ExcepcionFichero e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError(e.getTitulo(), e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fichero vacío", e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
		} catch (ExcepcionDB e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Usuario no encontrado", e.getMessage()),
					HttpStatus.NOT_FOUND);
		} catch (Exception e) { // Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping("/chat/deletefile")
	public ResponseEntity<MensajeError> borrarFichero(@RequestParam("file") String file,
			@RequestParam("sala") long sala, @AuthenticationPrincipal UserAuth u) {
		try {
			boolean borrar;
			switch (u.getRol()) {
			case MENTOR:
				borrar = salaChats.borrarFileChat(u.getUsername(), sala, file, true);
				if (borrar)
					fservice.borrarFileSend(file, u.getUsername(), "mentores");
				break;
			case MENTORIZADO:
				borrar = salaChats.borrarFileChat(u.getUsername(), sala, file, false);
				if (borrar)
					fservice.borrarFileSend(file, u.getUsername(), "mentorizados");
				break;
			default:
				return new ResponseEntity<>(new MensajeError("Sin autorización", "No tienes permiso para hacer esto"),
						HttpStatus.UNAUTHORIZED);
			}
			//System.out.println("Fichero eliminado: " + file + " por " + u.getUsername());
			return new ResponseEntity<>(new MensajeError(file + " (borrado)", ""), HttpStatus.OK);

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
			// fservice.clearFile(u.getUsername(), file.getOriginalFilename());
			return new ResponseEntity<>(
					new MensajeError("Fallo al almacenar el fichero",
							"Se ha producido un fallo al tratar de almacenar el fichero."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ExcepcionRecursos e) {
			// Aqui tambien limpiar
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo de recurso", e.getMessage()),
					HttpStatus.NOT_ACCEPTABLE);
		} catch (Exception e) { // Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
