package com.tfg.mentoring.controller;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.QueryTimeoutException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.model.AreaConocimiento;
import com.tfg.mentoring.model.Institucion;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.NivelEstudios;
import com.tfg.mentoring.model.Notificacion;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.EstadosNotificacion;
import com.tfg.mentoring.model.auxiliar.IdNotificacion;
import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.NotificacionUser;
import com.tfg.mentoring.model.auxiliar.Roles;
import com.tfg.mentoring.model.auxiliar.UsuarioPerfil;
import com.tfg.mentoring.repository.InstitucionRepo;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizacionRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.NotificacionRepo;
import com.tfg.mentoring.repository.PeticionRepo;
import com.tfg.mentoring.repository.UsuarioRepo;
import com.tfg.mentoring.service.UserService;
import com.tfg.mentoring.service.util.ListLoad;

//Partir este controlador en varios, al menos en uno para mentor y otro para mentorizado
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private MentorizadoRepo menrepo;
	@Autowired
	private MentorRepo mrepo;
	@Autowired
	private UsuarioRepo urepo;
	@Autowired
	private NotificacionRepo notrepo;
	@Autowired
	private InstitucionRepo irepo;
	@Autowired
	private MentorizacionRepo mentorizacionrepo;
	@Autowired
	private PeticionRepo prepo;

	@Autowired
	private UserService uservice;

	@Autowired
	private ListLoad listas;
	@Autowired
	private SimpleDateFormat format;

/////////////////////////////////////////////////////////////////////////
////////                  Perfil de usuario                       ///////
/////////////////////////////////////////////////////////////////////////

	// Proveer la pagina del perfil
	@GetMapping("/perfil")
	public ModelAndView getPerfilPrivado(@AuthenticationPrincipal Usuario us) {
		try {
			// System.out.println(us.toString());
			if (us.getRol() == Roles.MENTOR) {
				Optional<Mentor> mentor = mrepo.findById(us.getUsername());
				if (mentor.isPresent()) {
					// System.out.println(mentor.get().toString());
					// System.out.println(format.format(mentor.get().getFnacimiento()));
					ModelAndView modelo = new ModelAndView("perfil");
					modelo.addObject("correo", mentor.get().getCorreo());
					modelo.addObject("rol", "Mentor");
					modelo.addObject("fregistro", format.format(mentor.get().getFregistro()));
					modelo.addObject("password", "");
					uservice.addListasModelo(modelo);
					uservice.addInstitucionUtils(modelo, mentor.get().getInstitucion());
					return modelo;
				} else {
					System.out.println("No existe");
					return new ModelAndView("error_page");
				}
			} else if (us.getRol() == Roles.MENTORIZADO) {
				Optional<Mentorizado> mentorizado = menrepo.findById(us.getUsername());
				if (mentorizado.isPresent()) {
					// System.out.println(mentorizado.get().toString());
					// System.out.println(format.format(mentorizado.get().getFnacimiento()));
					ModelAndView modelo = new ModelAndView("perfil");
					modelo.addObject("correo", mentorizado.get().getCorreo());
					modelo.addObject("rol", "Mentorizado");
					modelo.addObject("fregistro", format.format(mentorizado.get().getFregistro()));
					modelo.addObject("password", "");
					uservice.addListasModelo(modelo);
					uservice.addInstitucionUtils(modelo, mentorizado.get().getInstitucion());
					return modelo;
				} else {
					System.out.println("No existe");
					return new ModelAndView("error_page");
				}
			} else {
				System.out.println("Otro rol");
				return new ModelAndView("error_page");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.toString());
			return new ModelAndView("error_page");
		}
	}

	// Obtener la informacion del perfil del usuario
	@GetMapping("/info")
	public ResponseEntity<UsuarioPerfil> getInfoPerfil(@AuthenticationPrincipal Usuario us) {
		try {
			if (us.getRol() == Roles.MENTOR) {
				Optional<Mentor> m = mrepo.findById(us.getUsername());
				if (m.isPresent()) {
					System.out.println(m.get().toString());
					// UsuarioPerfil up = new UsuarioPerfil(m.get());
					UsuarioPerfil up = uservice.getPerfilMentor(m.get());
					up.setNotificar_correo(m.get().getUsuario().isNotificar_correo());// Esto se podria meter en el
																						// mapeo cambiandole el nombre
					up.setMentor(true);
					System.out.println(up.toString());
					return new ResponseEntity<>(up, HttpStatus.OK);
				} else {
					// Aqui tambien estaria bien hacer algo de error personalizado
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}
			} else if (us.getRol() == Roles.MENTORIZADO) {
				Optional<Mentorizado> m = menrepo.findById(us.getUsername());
				if (m.isPresent()) {
					// UsuarioPerfil up = new UsuarioPerfil(m.get());
					UsuarioPerfil up = uservice.getPerfilMentorizado(m.get());
					up.setNotificar_correo(m.get().getUsuario().isNotificar_correo());
					up.setMentor(false);
					up.setHoraspormes(4);// Aqui le ponemos este valor para que no se nos queje al intentar modificar la
											// informacion del perfil de un mentorizado
					System.out.println(up.toString());
					return new ResponseEntity<>(up, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}
			} else {
				// Esto cambiarlo a otro para decir que no es un rol permitido
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (EntityNotFoundException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Actualizar la informacion del perfil del usuario
	@PostMapping("/setinfo")
	public ResponseEntity<Usuario> setInfoPerfil(@RequestBody UsuarioPerfil up, @AuthenticationPrincipal Usuario us) {
		if (up == null || us.getUsername() == null || up.getNombre() == null) {
			System.out.println("El usuario o la informacion estaban a null");
			// Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		try {
			System.out.println(up.toString());
			if (us.getRol() == Roles.MENTOR) {
				Optional<Mentor> m = mrepo.findById(us.getUsername());
				if (m.isPresent()) {
					try {
						Mentor men = m.get();
						men.setNombre(up.getNombre());
						men.setPapellido(up.getPapellido());
						men.setSapellido(up.getSapellido());
						men.setDescripcion(up.getDescripcion());
						men.setFnacimiento(up.getFnacimiento());
						men.setHoraspormes(up.getHoraspormes());
						men.setLinkedin(up.getLinkedin());
						men.setNivelEstudios(new NivelEstudios(up.getNivelEstudiosNivelestudios()));
						// men.setNivelEstudios(up.getNivelEstudios());
						// men.setPuesto(up.getPuesto());
						men.setEntidad(up.getEntidad());
						men.setTelefono(up.getTelefono());
						if (!men.getInstitucion().getNombre().equals(up.getInstitucionNombre())) {
							List<Institucion> i = irepo.findByNombre(up.getInstitucionNombre());
							men.setInstitucion(i.get(0));
						}
						men.setAreas(up.getAreas());
						men.getUsuario().setNotificar_correo(up.isNotificar_correo());

						mrepo.save(men);
					} catch (JDBCConnectionException | QueryTimeoutException e) {// Fallo al acceder a la base de datos
																					// o demasiado tiempo
						// Estas dos excepciones de momento las vamos a meter en el mismo saco, pero se
						// podrian separar, ya que la segunda es recuperable
						System.out.println(e.getMessage());
						return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
					} catch (Exception e) { // Otro fallo
						System.out.println(e.getMessage());
						return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					return new ResponseEntity<>(null, HttpStatus.OK);

				} else {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}
			} else if (us.getRol() == Roles.MENTORIZADO) {
				Optional<Mentorizado> m = menrepo.findById(us.getUsername());
				if (m.isPresent()) {
					try {
						Mentorizado men = m.get();
						men.setNombre(up.getNombre());
						men.setPapellido(up.getPapellido());
						men.setSapellido(up.getSapellido());
						men.setDescripcion(up.getDescripcion());
						men.setFnacimiento(up.getFnacimiento());
						men.setLinkedin(up.getLinkedin());
						men.setNivelEstudios(new NivelEstudios(up.getNivelEstudiosNivelestudios()));
						// men.setNivelEstudios(up.getNivelEstudios());
						men.setTelefono(up.getTelefono());
						if (!men.getInstitucion().getNombre().equals(up.getInstitucionNombre())) {
							List<Institucion> i = irepo.findByNombre(up.getInstitucionNombre());
							men.setInstitucion(i.get(0));
						}
						men.setAreas(up.getAreas());
						men.getUsuario().setNotificar_correo(up.isNotificar_correo());

						menrepo.save(men);
					} catch (JDBCConnectionException | QueryTimeoutException e) {// Fallo al acceder a la base de datos
																					// o demasiado tiempo
						System.out.println(e.getMessage());
						return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
					} catch (Exception e) { // Otro fallo
						System.out.println(e.getMessage());
						return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					return new ResponseEntity<>(null, HttpStatus.OK);

				} else {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}
			} else {
				// Esto cambiarlo a otro para decir que no es un rol permitido
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {// Fallo al acceder a la base de datos
			// o demasiado tiempo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Eliminar areas de conocimiento de un usuario
	@PostMapping("/areas/delete")
	public ResponseEntity<MensajeError> borrarAreaUsuario(@AuthenticationPrincipal Usuario us,
			@RequestBody AreaConocimiento area) {
		if (area == null || us.getUsername() == null || area.getArea() == null) {
			System.out.println("El mentorizado estaba null");
			// Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(
					new MensajeError("Se ha producido un problema al intentar realizar la peticion "
							+ "al servidor o al acceder a su información si recibe este mensaje,"
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "
							+ "intente ser lo más preciso posible al indicar la hora en la que ocurrió."),
					HttpStatus.BAD_REQUEST);
		}
		System.out.println("Borrando");
		if (us.getRol() == Roles.MENTOR) {
			Optional<Mentor> m = mrepo.findById(us.getUsername());
			if (m.isPresent()) {
				try {
					mrepo.borrarArea(us.getUsername(), area.getArea());
				} catch (JDBCConnectionException | QueryTimeoutException e) {
					// TODO: handle exception
					System.out.println(e.getMessage());
					return new ResponseEntity<>(
							new MensajeError("Se ha producido un problema al intentar actualizar el repositorio, "
									+ "por favor, vuelva a intentarlo más tarde."),
							HttpStatus.SERVICE_UNAVAILABLE);
				} catch (Exception e) { // Otro fallo
					System.out.println(e.getMessage());
					return new ResponseEntity<>(new MensajeError(
							"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
									+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "
									+ "intente ser lo más preciso posible al indicar la hora en la que ocurrió."),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(new MensajeError(
						"Se ha producido un problema al intentar acceder al la información de su cuenta, si recibe este mensaje,"
								+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "
								+ "intente ser lo más preciso posible al indicar la hora en la que ocurrió."),
						HttpStatus.BAD_REQUEST);
			}
		} else if (us.getRol() == Roles.MENTORIZADO) {
			Optional<Mentorizado> m = menrepo.findById(us.getUsername());
			if (m.isPresent()) {
				try {
					menrepo.borrarArea(us.getUsername(), area.getArea());
				} catch (JDBCConnectionException | QueryTimeoutException e) {
					// TODO: handle exception
					System.out.println(e.getMessage());
					return new ResponseEntity<>(
							new MensajeError("Se ha producido un problema al intentar actualizar el repositorio, "
									+ "por favor, vuelva a intentarlo más tarde."),
							HttpStatus.SERVICE_UNAVAILABLE);
				} catch (Exception e) { // Otro fallo
					System.out.println(e.getMessage());
					return new ResponseEntity<>(new MensajeError(
							"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
									+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "
									+ "intente ser lo más preciso posible al indicar la hora en la que ocurrió."),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(new MensajeError(
						"Se ha producido un problema al intentar acceder al la información de su cuenta, si recibe este mensaje,"
								+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "
								+ "intente ser lo más preciso posible al indicar la hora en la que ocurrió."),
						HttpStatus.BAD_REQUEST);
			}
		} else {
			// Esto, en principio, no deberia pasar, puesto que solo puede acceder aqui los
			// que tengan de rol mentor o mentorizado
			return new ResponseEntity<>(new MensajeError("No tienes permiso para hacer esto"), HttpStatus.UNAUTHORIZED);
		}
	}

/////////////////////////////////////////////////////////////////////////
////////                  Notificaciones                          ///////
/////////////////////////////////////////////////////////////////////////

	// Obtener notificaciones
	@GetMapping("/notificaciones")
	public ResponseEntity<List<NotificacionUser>> getAllNotificaciones(@AuthenticationPrincipal Usuario us) {
		try {
			List<Notificacion> Notificaciones = new ArrayList<Notificacion>();
			List<NotificacionUser> nUser = new ArrayList<NotificacionUser>();
			Notificaciones = notrepo.getNotificaciosUser(us.getUsername());
			if (Notificaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			// notrepo.actualizaEstadoNotificaciosUser(us.getUsername());
			for (Notificacion n : Notificaciones) {
				nUser.add(new NotificacionUser(n));
				if (n.getEstado() == EstadosNotificacion.ENTREGADA) {
					n.setEstado(EstadosNotificacion.LEIDA);
				}
			}
			notrepo.saveAll(Notificaciones);

			// Notificaciones.removeIf(n -> (n.getFechaeliminacion() != null));
			return new ResponseEntity<>(nUser, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {// Fallo al acceder a la base de datos o demasiado
																		// tiempo
			// Estas dos excepciones de momento las vamos a meter en el mismo saco, pero se
			// podrian separar, ya que la segunda es recuperable
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { // Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Obtener notificaciones nuevas
	@GetMapping("/notificaciones/nuevas")
	public ResponseEntity<List<NotificacionUser>> getNewNotificaciones(@AuthenticationPrincipal Usuario us) {
		try {
			List<NotificacionUser> nUser = new ArrayList<NotificacionUser>();
			List<Notificacion> Notificaciones = new ArrayList<Notificacion>();
			Notificaciones = notrepo.getNews(us.getUsername());
			if (Notificaciones.isEmpty()) {
				System.out.println("Sin nuevas");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Notificacion n : Notificaciones) {
				nUser.add(new NotificacionUser(n));
				if (n.getEstado() == EstadosNotificacion.ENTREGADA) {
					n.setEstado(EstadosNotificacion.LEIDA);
				}
			}
			notrepo.saveAll(Notificaciones);
			return new ResponseEntity<>(nUser, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {// Fallo al acceder a la base de datos o demasiado
																		// tiempo
			// Estas dos excepciones de momento las vamos a meter en el mismo saco, pero se
			// podrian separar, ya que la segunda es recuperable
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { // Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Borrar una notificacion
	@PostMapping("/notificaciones/delete")
	public ResponseEntity<MensajeError> borrarNotificacion(@RequestBody IdNotificacion id) {
		System.out.println("Borrando");
		if (id == null) {
			return new ResponseEntity<>(new MensajeError("No se ha seleccionado ninguna notificacion."),
					HttpStatus.BAD_REQUEST);
		}
		try {
			notrepo.borrarNotificacion(id.getId());
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(
					new MensajeError("Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { // Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError(
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "
							+ "intente ser lo más preciso posible al indicar la hora en la que ocurrió."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

/////////////////////////////////////////////////////////////////////////
////////             Pagina principal usuarios                    ///////
/////////////////////////////////////////////////////////////////////////

	@GetMapping("/principal")
	public ModelAndView getPaginaPrincipal(@AuthenticationPrincipal Usuario us) {
		try {
			// System.out.println(us.toString());
			if (us.getRol() == Roles.MENTOR) {
				Optional<Mentor> mentor = mrepo.findById(us.getUsername());
				if (mentor.isPresent()) {
					// Aqeui habria que recuperar cosas de su institucion para la pagina
					ModelAndView modelo = new ModelAndView("prMentor");
					uservice.addInstitucionUtils(modelo, mentor.get().getInstitucion());
					return modelo;
				} else {
					System.out.println("No existe");
					return new ModelAndView("error_page");
				}
			} else if (us.getRol() == Roles.MENTORIZADO) {
				Optional<Mentorizado> mentorizado = menrepo.findById(us.getUsername());
				if (mentorizado.isPresent()) {
					// System.out.println(mentorizado.get().toString());
					// System.out.println(format.format(mentorizado.get().getFnacimiento()));
					ModelAndView modelo = new ModelAndView("prMentorizado");
					modelo.addObject("instituciones", listas.getInstituciones());
					modelo.addObject("areas", listas.getAreas());
					uservice.addInstitucionUtils(modelo, mentorizado.get().getInstitucion());
					return modelo;
				} else {
					System.out.println("No existe");
					return new ModelAndView("error_page");
				}
			} else {
				System.out.println("Otro rol");
				return new ModelAndView("error_page");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.toString());
			return new ModelAndView("error_page");
		}
	}

	@PostMapping("/eliminar")
	public ModelAndView eliminarCuentaUsuario(@AuthenticationPrincipal Usuario us,
			@ModelAttribute("password") String password, HttpServletRequest request, HttpServletResponse response) {
		try {
			System.out.println(password);
			if (uservice.comprobarPassword(password, us)) {
				String username = us.getUsername();
				if (us.getRol() == Roles.MENTOR) {
					try {
						// Aqui tambien faltaria alguna notificacion a los mentorizados
						prepo.borrarPeticionesMentor(username);
						mentorizacionrepo.borrarMentorizacionesMentor(username);
						mrepo.borrarMentor(username);
						urepo.borrarUsuario(username);// Esto lo ultimo, para que, en el peor de los casos, un usuario
														// pueda volver a
						// logearse para intentar volver a borrar de nuevo su cuenta
						uservice.notificarPorCorreo(us, "Cuenta de mentoring eliminada",
								"Le notificamos que su cuenta de usuario ha sido eliminada de forma exitosa. Recuerde que no puede<br>"
										+ "volver a usar esta cuenta de correo para registrar otra cuenta en nuestra aplicación. <br>"
										+ "Le damos las gracias por todo lo que haya aportado y le queremos desear mucha suerte.");
						// Aqui falta enviar un correo
					} catch (JDBCConnectionException | QueryTimeoutException e) {// Si falla el acceso a la base de
																					// datos
						// TODO: handle exception
						System.out.println(e.getMessage());
						return new ModelAndView("error_page");// Esto ponerle un mensaje
					} catch (MessagingException | UnsupportedEncodingException e) {// Si falla el envio del correo
						// TODO: handle exception
						System.out.println(e.getMessage());
						// return new ModelAndView("error_page");
					}
				} else if (us.getRol() == Roles.MENTORIZADO) {
					try {
						// Aqui tambien faltaria alguna notificacion a los mentores
						prepo.borrarPeticionesMentorizado(username);
						mentorizacionrepo.borrarMentorizacionesMentorizado(username);
						menrepo.borrarMentorizado(username);
						urepo.borrarUsuario(username);// Esto lo ultimo, para que, en el peor de los casos, un usuario
														// pueda volver a
						// logearse para intentar volver a borrar de nuevo su cuenta
						uservice.notificarPorCorreo(us, "Cuenta de mentoring eliminada",
								"Le notificamos que su cuenta de usuario ha sido eliminada de forma exitosa. Recuerde que no puede<br>"
										+ "volver a usar esta cuenta de correo para registrar otra cuenta en nuestra aplicación. <br>"
										+ "Le damos las gracias por todo lo que haya aportado y le queremos desear mucha suerte.");
					} catch (JDBCConnectionException | QueryTimeoutException e) {// Si falla el acceso a la base de
																					// datos
						// TODO: handle exception
						System.out.println(e.getMessage());
						return new ModelAndView("error_page");// Esto ponerle un mensaje
					} catch (MessagingException | UnsupportedEncodingException e) {// Si falla el envio del correo
						// TODO: handle exception
						System.out.println(e.getMessage());
						// return new ModelAndView("error_page");
					}

				}
				// Cerramos sesion con el usuario
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (auth != null) {
					new SecurityContextLogoutHandler().logout(request, response, auth);
				}
				return new ModelAndView("home");
			} else {
				System.out.println("La contraseña no coincide");
				// Si lo de borrar esta en un desplegable, quizas habria que pasar por path que
				// se despliegue
				ModelAndView modelo = new ModelAndView("perfil");
				modelo.addObject("correo", us.getUsername());
				if (us.getRol() == Roles.MENTOR)
					modelo.addObject("rol", "Mentor");
				else if (us.getRol() == Roles.MENTORIZADO)
					modelo.addObject("rol", "Mentorizado");
				else
					modelo.addObject("rol", "Otro");
				modelo.addObject("password", "");
				// modelo.addObject("fregistro", format.format(up.)); //Reemplazar la fecha de
				// registro por el error en la plantilla
				uservice.addListasModelo(modelo);
				modelo.addObject("error", "La contraseña introducida al intentar eliminar la cuenta no era correcta");
				return modelo;
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.toString());
			return new ModelAndView("error_page");
		}
	}

}
