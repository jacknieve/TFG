package com.tfg.mentoring.service;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.exceptions.ExcepcionDB;
import com.tfg.mentoring.exceptions.ExcepcionRecursos;
import com.tfg.mentoring.model.Institucion;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizacion;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.Notificacion;
import com.tfg.mentoring.model.Peticion;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.MensajeConAsunto;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.DTO.MentorDTO;
import com.tfg.mentoring.model.auxiliar.DTO.NotificacionDTO;
import com.tfg.mentoring.model.auxiliar.enums.AsuntoMensaje;
import com.tfg.mentoring.model.auxiliar.enums.EstadosNotificacion;
import com.tfg.mentoring.model.auxiliar.enums.EstadosPeticion;
import com.tfg.mentoring.model.auxiliar.enums.MotivosNotificacion;
import com.tfg.mentoring.model.auxiliar.enums.Roles;
import com.tfg.mentoring.model.auxiliar.requests.CamposBusqueda;
import com.tfg.mentoring.model.auxiliar.requests.EnvioPeticion;
import com.tfg.mentoring.model.auxiliar.requests.UserAux;
import com.tfg.mentoring.repository.InstitucionRepo;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizacionRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.NotificacionRepo;
import com.tfg.mentoring.repository.PeticionRepo;
import com.tfg.mentoring.repository.UsuarioRepo;
import com.tfg.mentoring.service.util.ListLoad;

import net.bytebuddy.utility.RandomString;

@Service
public class UserService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MentorizadoRepo menrepo;
	@Autowired
	private MentorRepo mrepo;
	@Autowired
	private UsuarioRepo urepo;
	@Autowired
	private NotificacionRepo nrepo;
	@Autowired
	private InstitucionRepo irepo;
	@Autowired
	private MentorizacionRepo mentorizacionrepo;
	@Autowired
	private PeticionRepo prepo;

	@Autowired
	private ListLoad listas;

	@Autowired
	private SimpMessagingTemplate messagingTemplate; // Esto quizas mejor un servicio de envio de mensajes

	@Autowired
	private FileService fservice;
	@Autowired
	private ActiveUsersService acservice;
	@Autowired
	private MessagingService mservice;
	@Autowired
	private MapeadoService mapservice;
	@Autowired
	private SalaChatServicio chatservice;

	public void register(UserAux useraux, String siteURL)
			throws UnsupportedEncodingException, MessagingException, ExcepcionDB, JDBCConnectionException,
			QueryTimeoutException, SecurityException, ExcepcionRecursos, FileNotFoundException {
		String random = RandomString.make(64);
		Usuario user = new Usuario(useraux.getCorreo(), passwordEncoder.encode(useraux.getPassword()), false, random);
		if (useraux.getMentor()) {
			user.setRol(Roles.MENTOR);
			Mentor mentor = new Mentor(user, useraux, irepo.findByNombre(useraux.getInstitucion()).get(0));
			try {
				mrepo.save(mentor);
			} catch (DataIntegrityViolationException e) {
				throw new ExcepcionDB("Clave duplicada");
			}
			fservice.crearDirectoriosUsuario(useraux.getCorreo(), "mentores");
			mservice.sendVerificationEmail(user, mentor.getNombre(), siteURL);

		} else {
			user.setRol(Roles.MENTORIZADO);
			Mentorizado mentorizado = new Mentorizado(user, useraux,
					irepo.findByNombre(useraux.getInstitucion()).get(0));
			try {
				menrepo.save(mentorizado);
			} catch (DataIntegrityViolationException e) {
				throw new ExcepcionDB("Clave duplicada");
			}
			fservice.crearDirectoriosUsuario(useraux.getCorreo(), "mentorizados");
			mservice.sendVerificationEmail(user, mentorizado.getNombre(), siteURL);

		}
	}

	public boolean verify(String verificationCode) throws JDBCConnectionException, QueryTimeoutException {
		Usuario user = urepo.findByVerificationCode(verificationCode);
		// Si no se encuentra al usuario o este ya esta verificado
		if (user == null || user.isEnabled()) {
			return false;
		} else {
			user.setVerificationCode(null);
			user.setEnable(true);
			urepo.save(user);
			if (user.getRol() == Roles.MENTOR) {
				enviarNotificacion(user, "Bienvenido/a ",
						"Te damos la bienvenida a nuestra aplicación, esperemos que le sea de utilidad.\n Por favor, no olvide "
								+ "rellenar los campos extra en su perfíl, como las áreas de conocimiento en las que podría ayudar, o su descripción.",
						MotivosNotificacion.SISTEMA);
			} else if (user.getRol() == Roles.MENTORIZADO) {
				enviarNotificacion(user, "Bienvenido/a ",
						"Te damos la bienvenida a nuestra aplicación, esperemos que le sea de utilidad.\n Por favor, no olvide "
								+ "rellenar los campos extra en su perfíl, como las áreas de conocimiento en las que quiere ser mentorizado, o su descripción.",
						MotivosNotificacion.SISTEMA);
			}
			user.setNotificar_correo(false);
			return true;
		}

	}

	public void enviarNotificacion(Usuario u, String titulo, String descripcion, MotivosNotificacion motivo) {
		try {
			Notificacion notificacion = new Notificacion(u, titulo, descripcion, motivo);

			if (!acservice.activo(u.getUsername())) {
				if (u.isNotificar_correo()) {
					Thread thread = new Thread() {
						public void run() {
							try {
								mservice.notificarPorCorreo(u, titulo, descripcion);
								System.out.println("Se envio el correo");
							} catch (UnsupportedEncodingException | MessagingException e) {
								// TODO Auto-generated catch block
								System.out.println(e.getMessage());
								e.printStackTrace();
							}
						}
					};
					thread.start();
				}
			} else if (acservice.enNotificacion(u.getUsername())) {
				if (!acservice.enChat(u.getUsername()))
					notificacion.setEstado(EstadosNotificacion.ENTREGADA);
				messagingTemplate.convertAndSendToUser(u.getUsername(), "/queue/messages",
						new MensajeConAsunto(AsuntoMensaje.NOTIFICACION, new NotificacionDTO(notificacion)));
			}
			nrepo.save(notificacion);
		} catch (JDBCConnectionException | QueryTimeoutException e) {// Si falla el acceso a la base de datos o tarda
																		// mucho
			// TODO: handle exception
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public void enviarNotificacionMensaje(String username, boolean porCorreo) {
		try {
			Optional<Usuario> u = urepo.findById(username);
			if (u.isPresent()) {
				if (nrepo.notificacionesMensajes(username).isEmpty()) {
					String titulo = "Mensajes nuevos";
					String descripcion = "Has recibido nuevos mensajes";
					Notificacion notificacion = new Notificacion(u.get(), titulo, descripcion,
							MotivosNotificacion.MENSAJE);
					nrepo.save(notificacion);
					if (porCorreo) {
						if (u.get().isNotificar_correo()) {

							try {
								mservice.notificarPorCorreo(u.get(), titulo, descripcion);
								System.out.println("Se envio el correo");
							} catch (UnsupportedEncodingException | MessagingException e) {
								// TODO Auto-generated catch block
								System.out.println(e.getMessage());
								e.printStackTrace();
							}
						}
					} else if (acservice.enNotificacion(username)) {
						messagingTemplate.convertAndSendToUser(u.get().getUsername(), "/queue/messages",
								new MensajeConAsunto(AsuntoMensaje.NOTIFICACION, new NotificacionDTO(notificacion)));
					}
				} // En caso contrario, nada
			} else {
				// Esto se registraria en un log
				System.out.println("Se ha intentado enviar una notificacion a un usuario que no existe");
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public void limpiarUsuario(UserAux user) throws JDBCConnectionException, QueryTimeoutException {
		if (user.getMentor()) {
			mrepo.limpiarUsuario(user.getCorreo());
		} else {
			menrepo.limpiarUsuario(user.getCorreo());
		}
		urepo.limpiarUsuario(user.getCorreo());
	}

	public void addListasModelo(ModelAndView modelo) {
		modelo.addObject("estudios", listas.getEstudios());
		modelo.addObject("instituciones", listas.getInstituciones());
		modelo.addObject("areas", listas.getAreas());
	}

	public void addListasModeloSinAreas(ModelAndView modelo) {
		modelo.addObject("estudios", listas.getEstudios());
		modelo.addObject("instituciones", listas.getInstituciones());
	}

	public List<MentorDTO> buscarMentores(CamposBusqueda campos) throws JDBCConnectionException, QueryTimeoutException {
		List<Mentor> mentores = new ArrayList<Mentor>();
		if (campos.getArea() == null || campos.getArea().equals("sin"))
			campos.setArea(null);
		if (campos.getInstitucion() == null || campos.getInstitucion().equals("sin"))
			campos.setInstitucion(null);
		mentores = mrepo.buscarMentores(campos.getInstitucion(), campos.getHoras(), campos.getArea());
		return mapservice.getMentorBusqueda(mentores);
	}

	public Optional<Mentor> getMentorPerfil(String mentor) throws JDBCConnectionException, QueryTimeoutException {
		return mrepo.findByUsuarioUsernameAndUsuarioEnable(mentor, true);
	}

	public int enviarSolicitud(EnvioPeticion peticion, String username) {
		Optional<Mentor> m = mrepo.findById(peticion.getMentor());
		Optional<Mentorizado> men = menrepo.findById(username);
		if (m.isPresent() && men.isPresent()) {
			// Primero comprobar si ya existe una mentorizacion y si ya existe una peticion
			List<Mentorizacion> mentorizacion = mentorizacionrepo.comprobarSiHayMentorizacion(peticion.getMentor(),
					username);
			if (mentorizacion.isEmpty()) {// Comprobamos que no haya ya una mentorizacion abierta entre ambas partes
				List<Peticion> peticiones = prepo.comprobarPeticion(peticion.getMentor(), username);
				if (peticiones.isEmpty()) {// Comprobamos si no existe ya una peticion pendiente
					Peticion p = new Peticion(m.get(), men.get(), peticion.getMotivo());
					// String nombre =prepo.save(p).getMentorizado().getNombre();
					Peticion resultado = prepo.save(p);
					if (resultado.getMentor().getUsuario().isEnabled()) {
						// Solo vamos a enviar notificacion si es la primera vez
						enviarNotificacion(m.get().getUsuario(), "Nueva peticion",
								"El usuario " + men.get().getNombre() + " te ha enviado una petición de mentorizacion.",
								MotivosNotificacion.PETICION);
						return 0;
					} else {
						return 3;
					}
				} else {// Si ya la hay, la actualizamos
					System.out.println("Se va a actualizar la peticion");
					Peticion p = peticiones.get(0);
					p.setEstado(EstadosPeticion.ENVIADA);
					p.setMotivo(peticion.getMotivo());
					prepo.save(p);
					return 1;
				}
			} else {
				return 2;
			}
		} else {
			return 4;
		}
	}

	public boolean comprobarPassword(String password, UserAuth u) {
		return passwordEncoder.matches(password, u.getPassword());
	}

	public void addInstitucionUtils(ModelAndView modelo, Institucion i) {
		if (i.getColor() != null && !i.getColor().equals("")) {
			modelo.addObject("color", i.getColor());
		} else {
			modelo.addObject("color", "#5AB9EA");
		}
		if (i.getColorB() != null && !i.getColorB().equals("")) {
			modelo.addObject("colorB", i.getColorB());
		} else {
			modelo.addObject("colorB", "#5AB9EA");
		}
		modelo.addObject("letrasBB", i.isLetrasBB());
		modelo.addObject("letrasBlancas", i.isLetrasBlancas());
		if (i.getUsuario().getFoto() != null) {
			modelo.addObject("logo",
					"/images/usuarios/instituciones/" + i.getUsuario().getUsername() + "/" + i.getUsuario().getFoto());
		}
		if (i.getNombre().equals("Otra")) {
			modelo.addObject("letrasBB", false);
			modelo.addObject("letrasBlancas", false);
		}

	}
	
	public Optional<ModelAndView> borrarMentor(String username) throws UnsupportedEncodingException, 
	MessagingException, JDBCConnectionException, QueryTimeoutException {
		Usuario u = urepo.findByUsername(username);
		ModelAndView modelo;
		if (u == null) {
			modelo = new ModelAndView("error_page_loged");
			modelo.addObject("mensaje",
					"Se ha producido un fallo al intentar acceder a su cuenta, por favor, si recibe este mensaje,"
							+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			modelo.addObject("rol", "Mentor");
			modelo.addObject("hora", new Date());
			return Optional.ofNullable(modelo);
		}
		prepo.borrarPeticionesMentor(username);
		chatservice.cerrarChatSalirMentor(username);
		mentorizacionrepo.borrarMentorizacionesMentor(username);
		mrepo.borrarMentor(username);
		urepo.borrarUsuario(username);// Esto lo ultimo, para que, en el peor de los casos, un usuario
		// pueda volver a logearse para intentar volver a borrar de nuevo su cuenta
		mservice.notificarPorCorreo(u, "Cuenta de mentoring eliminada",
				"Le notificamos que su cuenta de usuario ha sido eliminada de forma exitosa. Recuerde que no puede<br>"
						+ "volver a usar esta cuenta de correo para registrar otra cuenta en nuestra aplicación. <br>"
						+ "Le damos las gracias por todo lo que haya aportado y le queremos desear mucha suerte.");
		fservice.borrarTodosUsuario(username, "mentores");
		return Optional.ofNullable(null);
	}
	
	public Optional<ModelAndView> borrarMentorizado(String username) throws UnsupportedEncodingException, 
	MessagingException, JDBCConnectionException, QueryTimeoutException {
		Usuario u = urepo.findByUsername(username);
		ModelAndView modelo;
		if (u == null) {
			modelo = new ModelAndView("error_page_loged");
			modelo.addObject("mensaje",
					"Se ha producido un fallo al intentar acceder a su cuenta, por favor, si recibe este mensaje,"
							+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			modelo.addObject("rol", "Mentorizado");
			modelo.addObject("hora", new Date());
			return Optional.ofNullable(modelo);
		}
		prepo.borrarPeticionesMentorizado(username);
		chatservice.cerrarChatSalirMentorizado(username);
		mentorizacionrepo.borrarMentorizacionesMentorizado(username);
		menrepo.borrarMentorizado(username);
		urepo.borrarUsuario(username);
		mservice.notificarPorCorreo(u, "Cuenta de mentoring eliminada",
				"Le notificamos que su cuenta de usuario ha sido eliminada de forma exitosa. Recuerde que no puede<br>"
						+ "volver a usar esta cuenta de correo para registrar otra cuenta en nuestra aplicación. <br>"
						+ "Le damos las gracias por todo lo que haya aportado y le queremos desear mucha suerte.");
		fservice.borrarTodosUsuario(username, "mentorizados");
		return Optional.ofNullable(null);
	}
}
