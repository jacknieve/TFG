package com.tfg.mentoring.service;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.exceptions.ExcepcionDB;
import com.tfg.mentoring.exceptions.ExcepcionRecursos;
import com.tfg.mentoring.model.Institucion;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizacion;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.NivelEstudios;
import com.tfg.mentoring.model.Notificacion;
import com.tfg.mentoring.model.Peticion;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.DTO.MentorDTO;
import com.tfg.mentoring.model.auxiliar.DTO.MentorInfoDTO;
import com.tfg.mentoring.model.auxiliar.DTO.MentorizacionDTO;
import com.tfg.mentoring.model.auxiliar.DTO.NotificacionDTO;
import com.tfg.mentoring.model.auxiliar.DTO.PerfilDTO;
import com.tfg.mentoring.model.auxiliar.DTO.PeticionDTO;
import com.tfg.mentoring.model.auxiliar.enums.EstadosNotificacion;
import com.tfg.mentoring.model.auxiliar.enums.EstadosPeticion;
import com.tfg.mentoring.model.auxiliar.enums.MotivosNotificacion;
import com.tfg.mentoring.model.auxiliar.enums.Roles;
import com.tfg.mentoring.model.auxiliar.requests.CamposBusqueda;
import com.tfg.mentoring.model.auxiliar.requests.EnvioPeticion;
import com.tfg.mentoring.model.auxiliar.requests.MentorizacionCerrar;
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
	private MentorizadoRepo menrepo; //
	@Autowired
	private MentorRepo mrepo; //
	@Autowired
	private UsuarioRepo urepo;//
	@Autowired
	private NotificacionRepo nrepo;
	@Autowired
	private InstitucionRepo irepo; //
	@Autowired
	private MentorizacionRepo mentorizacionrepo;//
	@Autowired
	private PeticionRepo prepo;

	@Autowired
	private ListLoad listas;

	@Autowired
	private FileService fservice;
	@Autowired
	private ActiveUsersService acservice;
	@Autowired
	private MessagingService mservice;
	@Autowired
	private MapeadoService mapservice;
	@Autowired
	private ChatService chatservice;

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
				chatservice.enviarMensaje(2, u.getUsername(), new NotificacionDTO(notificacion));
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
						chatservice.enviarMensaje(2, u.get().getUsername(), new NotificacionDTO(notificacion));
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

	public Optional<MentorInfoDTO> getMentorPerfil(String mentor) throws JDBCConnectionException, QueryTimeoutException {
		Optional<Mentor> m = mrepo.findByUsuarioUsernameAndUsuarioEnable(mentor, true);
		if(m.isPresent()) {
			return Optional.of(mapservice.getMentorInfoBusqueda(m.get()));
		}
		else {
			return Optional.empty();
		}
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
		return Optional.empty();
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
		return Optional.empty();
	}

	public List<PeticionDTO> obtenerPeticiones(String username) throws JDBCConnectionException, QueryTimeoutException{
		List<PeticionDTO> pUser = new ArrayList<PeticionDTO>();
		List<Peticion> peticiones = new ArrayList<Peticion>();
		peticiones = prepo.obtenerPeticiones(username);
		for (Peticion p : peticiones) {
			// PeticionInfoDTO up = mservice.getMentorizadoInfo(p.getMentorizado());
			pUser.add(new PeticionDTO(p, mapservice.getMentorizadoInfo(p.getMentorizado())));
			if (p.getEstado() == EstadosPeticion.ENVIADA) {
				p.setEstado(EstadosPeticion.RECIBIDA);
			}
		}
		if(!pUser.isEmpty()) prepo.saveAll(peticiones);
		return pUser;
	}
	
	public List<PeticionDTO> obtenerPeticionesNuevas(String username) throws JDBCConnectionException, QueryTimeoutException{
		List<PeticionDTO> pUser = new ArrayList<PeticionDTO>();
		List<Peticion> peticiones = new ArrayList<Peticion>();
		peticiones = prepo.getNews(username);
		for (Peticion p : peticiones) {
			// PeticionInfoDTO up = mservice.getMentorizadoInfo(p.getMentorizado());
			pUser.add(new PeticionDTO(p, mapservice.getMentorizadoInfo(p.getMentorizado())));
			if (p.getEstado() == EstadosPeticion.ENVIADA) {
				p.setEstado(EstadosPeticion.RECIBIDA);
			}
		}
		if(!pUser.isEmpty()) prepo.saveAll(peticiones);
		return pUser;
	}

	public Optional<MensajeError> aceptarPeticion(String username, String mentorizado) throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentor> m = mrepo.findById(username);// Asumimos que el username nunca va a ser null
		Optional<Mentorizado> men = menrepo.findById(mentorizado);
		if (m.isPresent() && men.isPresent()) {
			Mentorizacion mentorizacion = new Mentorizacion(m.get(), men.get());
			Mentorizacion resultado = mentorizacionrepo.save(mentorizacion);
			prepo.aceptarPeticion(username, mentorizado);
			if (resultado.getMentorizado().getUsuario().isEnabled()) {
				chatservice.abrirChat(resultado.getMentor(), resultado.getMentorizado());
				enviarNotificacion(men.get().getUsuario(), "Peticion aceptada",
						"El usuario " + m.get().getNombre() + " te ha aceptado una petición de mentorizacion.",
						MotivosNotificacion.ACEPTAR);
				return Optional.empty();
			} else {
				resultado.setFin(new Date());
				mentorizacionrepo.save(resultado);
				return Optional.ofNullable(new MensajeError("Mentorizado eliminado",
						"El mentorizado ya no se encuentra disponible, no se creará mentorización."));
			}

		} else {
			System.out.println("No hay usuarios");
			return Optional.ofNullable(new MensajeError("Fallo en la peticion",
					"Se ha producido un problema al intentar acceder al la información de su cuenta o de "
							+ "la del mentorizado, por favor,  si recibe este mensaje,"
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()));
		}
	}
	
	public Optional<MensajeError> rechazarPeticion(String username, String mentorizado) throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentor> m = mrepo.findById(username);// Asumimos que el username nunca va a ser null
		Optional<Mentorizado> men = menrepo.findById(mentorizado);
		if (m.isPresent() && men.isPresent()) {
			prepo.rechazarPeticion(username, mentorizado);
			enviarNotificacion(men.get().getUsuario(), "Peticion rechazada",
					"El usuario " + m.get().getNombre() + " te ha rechazado una petición de mentorizacion.",
					MotivosNotificacion.RECHAZAR);
			return Optional.empty();
		} else {
			System.out.println("No hay usuarios");
			return Optional.ofNullable(new MensajeError("Fallo en la peticion",
					"Se ha producido un problema al intentar acceder al la información de su cuenta o de "
							+ "la del mentorizado, por favor,  si recibe este mensaje,"
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()));
		}
	}
	
	public List<MentorizacionDTO> obtenerMentorizacionesMentor(String username) throws JDBCConnectionException, QueryTimeoutException{
		List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
		List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
		mentorizaciones = mentorizacionrepo.obtenerMentorizacionesMentor(username);
		for (Mentorizacion m : mentorizaciones) {
			mUser.add(new MentorizacionDTO(m, mapservice.getMentorizadoMentorizacion(m.getMentorizado()),
					m.getMentorizado().getCorreo(), false));
		}
		return mUser;
	}
	
	public List<MentorizacionDTO> obtenerMentorizacionesMentorNuevas(String username, Timestamp fecha) throws JDBCConnectionException, QueryTimeoutException{
		List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
		List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
		mentorizaciones = mentorizacionrepo.getNuevasMentor(username, fecha);
		for (Mentorizacion m : mentorizaciones) {
			if (m.getFin() == null) {
				mUser.add(new MentorizacionDTO(m, mapservice.getMentorizadoMentorizacion(m.getMentorizado()),
						m.getMentorizado().getCorreo(), false));
			} else {
				mUser.add(new MentorizacionDTO(m, null, m.getMentorizado().getCorreo(), false));
			}
		}
		return mUser;
	}
	
	public List<MentorizacionDTO> obtenerMentorizacionesMentorizado(String username) throws JDBCConnectionException, QueryTimeoutException{
		List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
		List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
		mentorizaciones = mentorizacionrepo.obtenerMentorizacionesMentorizado(username);
		for (Mentorizacion m : mentorizaciones) {
			mUser.add(new MentorizacionDTO(m, mapservice.getMentorMentorizacion(m.getMentor()),
					m.getMentor().getCorreo(), true));
		}
		return mUser;
	}
	
	public List<MentorizacionDTO> obtenerMentorizacionesMentorizadoNuevas(String username, Timestamp fecha) throws JDBCConnectionException, QueryTimeoutException{
		List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
		List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
		mentorizaciones = mentorizacionrepo.getNuevasMentorizado(username, fecha);
		for (Mentorizacion m : mentorizaciones) {
			if (m.getFin() == null) {
				mUser.add(new MentorizacionDTO(m, mapservice.getMentorMentorizacion(m.getMentor()),
						m.getMentor().getCorreo(), true));
			} else {
				mUser.add(new MentorizacionDTO(m, null, m.getMentor().getCorreo(), true));
			}
		}
		return mUser;
	}
	
	public List<MentorizacionDTO> obtenerMentorizacionesPorPuntuar(String username) throws JDBCConnectionException, QueryTimeoutException{
		List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
		List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
		mentorizaciones = mentorizacionrepo.obtenerMentorizacionesPorPuntuar(username);
		for (Mentorizacion m : mentorizaciones) {
			mUser.add(new MentorizacionDTO(m, mapservice.getMentorInfo(m.getMentor()), m.getMentor().getCorreo(),
					true));
		}
		return mUser;
	}
	
	public List<MentorizacionDTO> obtenerMentorizacionesPorPuntuarNuevas(String username, Timestamp fecha) throws JDBCConnectionException, QueryTimeoutException{
		List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
		List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
		mentorizaciones = mentorizacionrepo.obtenerMentorizacionesPorPuntuarNuevas(username, fecha);
		for (Mentorizacion m : mentorizaciones) {
			if (m.getFin() == null) {
				mUser.add(new MentorizacionDTO(m, mapservice.getMentorInfo(m.getMentor()), m.getMentor().getCorreo(),
						true));
			} else {
				mUser.add(new MentorizacionDTO(m, null, m.getMentor().getCorreo(), true));
			}
		}
		return mUser;
	}
	
	public Optional<MensajeError> cerrarMentorizacionesMentor(String username, String mentorizado) throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentor> m = mrepo.findById(username);
		Optional<Mentorizado> men = menrepo.findById(mentorizado);
		if (m.isPresent() && men.isPresent()) {
			mentorizacionrepo.cerrarMentorizacion(username, mentorizado,
					new Timestamp(System.currentTimeMillis()));
			chatservice.cerrarChat(m.get().getCorreo(), mentorizado, true);
			enviarNotificacion(men.get().getUsuario(), "Mentorizacion cerrada", "El usuario "
					+ m.get().getNombre()
					+ " ha cerrado una mentorización que tenía abierta contigo. Puedes proceder a puntuarla y comentarla en el apartado de puntuar.",
					MotivosNotificacion.CIERRE);
			return Optional.empty();
		} else {
			System.out.println("Fallo al acceder a los usuarios");
			return Optional.ofNullable(new MensajeError("Fallo en la peticion",
					"Se ha producido un problema al intentar acceder al la información de su cuenta o de "
							+ "la del mentorizado, por favor,  si recibe este mensaje,"
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()));
		}
	}
	
	public Optional<MensajeError> cerrarMentorizacionesMentorizado(String username, MentorizacionCerrar mentorizacion) 
			throws JDBCConnectionException, QueryTimeoutException{

		Optional<Mentor> m = mrepo.findById(mentorizacion.getMentor());
		Optional<Mentorizado> men = menrepo.findById(username);
		if (m.isPresent() && men.isPresent()) {
			mentorizacionrepo.cerrarPuntuarMentorizacion(mentorizacion.getPuntuacion(),
					mentorizacion.getComentario(), mentorizacion.getMentor(), username);
			chatservice.cerrarChat(m.get().getCorreo(), men.get().getCorreo(), false);
			enviarNotificacion(m.get().getUsuario(), "Mentorizacion cerrada",
					"El usuario " + men.get().getNombre()
							+ " ha cerrado una mentorización que tenía abierta contigo.",
					MotivosNotificacion.CIERRE);
			return Optional.empty();
		} else {
			System.out.println("Fallo al acceder a los usuarios");
			return Optional.ofNullable(new MensajeError("Fallo en la peticion",
					"Se ha producido un problema al intentar acceder al la información de su cuenta o de "
							+ "la del mentor, por favor,  si recibe este mensaje,"
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()));
		}
	}
	
	public Optional<MensajeError> cambiarFase(String username, String mentorizado, int fase) throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentor> m = mrepo.findById(username);
		Optional<Mentorizado> men = menrepo.findById(mentorizado);
		if (m.isPresent() && men.isPresent()) {
			mentorizacionrepo.cambiarFase(username, mentorizado, fase);
			return Optional.empty();
		} else {
			System.out.println("Fallo al acceder a los usuarios");
			return Optional.ofNullable(new MensajeError("Fallo en la peticion",
					"Se ha producido un problema al intentar acceder al la información de su cuenta o de "
							+ "la del mentorizado, por favor,  si recibe este mensaje,"
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()));
		}
	}
	
	public Optional<MensajeError> puntuarMentorizacion(String username, MentorizacionCerrar mentorizacion) 
			throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentor> m = mrepo.findById(mentorizacion.getMentor());
		Optional<Mentorizado> men = menrepo.findById(username);
		if (m.isPresent() && men.isPresent()) {
			mentorizacionrepo.puntuarMentorizacion(mentorizacion.getPuntuacion(), mentorizacion.getComentario(),
					mentorizacion.getMentor(), username, new Timestamp(mentorizacion.getFechafin()));
			return Optional.empty();
		} else {
			System.out.println("Fallo al acceder a los usuarios");
			return Optional.ofNullable(new MensajeError("Fallo en la peticion",
					"Se ha producido un problema al intentar acceder al la información de su cuenta o de "
							+ "la del mentor, por favor,  si recibe este mensaje,"
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()));
		}
		
	}
	
	public Optional<PerfilDTO> obtenerMiPerfilMentor(String username) 
			throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentor> m = mrepo.findById(username);
		if (m.isPresent()) {
			// UsuarioPerfil up = new UsuarioPerfil(m.get());
			PerfilDTO up = mapservice.getPerfilMentor(m.get());
			up.setNotificar_correo(m.get().getUsuario().isNotificar_correo());
			up.setMentor(true);
			return Optional.of(up);
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<PerfilDTO> obtenerMiPerfilMentorizado(String username) 
			throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentorizado> m = menrepo.findById(username);
		if (m.isPresent()) {
			// UsuarioPerfil up = new UsuarioPerfil(m.get());
			PerfilDTO up = mapservice.getPerfilMentorizado(m.get());
			up.setNotificar_correo(m.get().getUsuario().isNotificar_correo());
			up.setMentor(false);
			up.setHoraspormes(4);
			return Optional.of(up);
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<MensajeError> setInfoMentor(PerfilDTO up, String username) 
			throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentor> m = mrepo.findById(username);
		if (m.isPresent()) {
			Mentor men = m.get();
			men.setNombre(up.getNombre());
			men.setPapellido(up.getPapellido());
			men.setSapellido(up.getSapellido());
			men.setDescripcion(up.getDescripcion());
			men.setFnacimiento(up.getFnacimiento());
			men.setHoraspormes(up.getHoraspormes());
			men.setLinkedin(up.getLinkedin());
			men.setNivelEstudios(new NivelEstudios(up.getNivelEstudiosNivelestudios()));
			men.setEntidad(up.getEntidad());
			men.setTelefono(up.getTelefono());
			if (!men.getInstitucion().getNombre().equals(up.getInstitucionNombre())) {
				List<Institucion> i = irepo.findByNombre(up.getInstitucionNombre());
				men.setInstitucion(i.get(0));
			}
			men.setAreas(up.getAreas());
			men.getUsuario().setNotificar_correo(up.isNotificar_correo());
			mrepo.save(men);
			return Optional.empty();

		} else {
			return Optional.of(new MensajeError());
		}
	}
	
	public Optional<MensajeError> setInfoMentorizado(PerfilDTO up, String username) 
			throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentorizado> me = menrepo.findById(username);
		if (me.isPresent()) {
			Mentorizado men = me.get();
			men.setNombre(up.getNombre());
			men.setPapellido(up.getPapellido());
			men.setSapellido(up.getSapellido());
			men.setDescripcion(up.getDescripcion());
			men.setFnacimiento(up.getFnacimiento());
			men.setLinkedin(up.getLinkedin());
			men.setNivelEstudios(new NivelEstudios(up.getNivelEstudiosNivelestudios()));
			men.setTelefono(up.getTelefono());
			if (!men.getInstitucion().getNombre().equals(up.getInstitucionNombre())) {
				List<Institucion> i = irepo.findByNombre(up.getInstitucionNombre());
				men.setInstitucion(i.get(0));
			}
			men.setAreas(up.getAreas());
			men.getUsuario().setNotificar_correo(up.isNotificar_correo());
			menrepo.save(men);
			return Optional.empty();

		} else {
			return Optional.of(new MensajeError());
		}
	}
	
	public Optional<MensajeError> borrarArea(String area, String username, Roles rol) 
			throws JDBCConnectionException, QueryTimeoutException{
		switch (rol) {
		case MENTOR:
			Optional<Mentor> m = mrepo.findById(username);
			if (m.isPresent()) {
				mrepo.borrarArea(username, area);
				return Optional.empty();
			} else {
				return Optional.of(new MensajeError("Fallo en la peticion",
						"Se ha producido un problema al intentar acceder al la información de su cuenta, si recibe este mensaje,"
								+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
								+ new Date()));
			}
		case MENTORIZADO:
			Optional<Mentorizado> me = menrepo.findById(username);
			if (me.isPresent()) {
				menrepo.borrarArea(username, area);
				return Optional.empty();
			} else {
				return Optional.of(new MensajeError("Fallo en la peticion",
						"Se ha producido un problema al intentar acceder al la información de su cuenta, si recibe este mensaje,"
								+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
								+ new Date()));
			}

		default:
			return Optional.of(new MensajeError("Sin autorización", "No tienes permiso para hacer esto"));
		}
	}
	
	public List<NotificacionDTO> obtenerNotificaciones(String username) throws JDBCConnectionException, QueryTimeoutException{
		List<Notificacion> notificaciones = new ArrayList<Notificacion>();
		List<NotificacionDTO> nUser = new ArrayList<NotificacionDTO>();
		notificaciones = nrepo.getNotificaciosUser(username);
		nrepo.actualizaEstadoNotificaciosUser(username);
		for (Notificacion n : notificaciones) {
			nUser.add(new NotificacionDTO(n));
		}
		return nUser;
	}
	
	
	public Optional<MensajeError> borrarNotificacion(long id, String username) throws JDBCConnectionException, QueryTimeoutException{
		if(nrepo.comprobarNotificacion(id, username) == 1) {
			nrepo.borrarNotificacion(id);
			return Optional.empty();
		}
		else {
			return Optional.of(new MensajeError("No es tuya", "La notificación que estás intentado borrar no te pertenece."));
		}
	}
}
