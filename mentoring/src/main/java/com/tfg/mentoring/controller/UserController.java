package com.tfg.mentoring.controller;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.DTO.NotificacionDTO;
import com.tfg.mentoring.model.auxiliar.DTO.PerfilDTO;
import com.tfg.mentoring.model.auxiliar.DTO.UserAuthDTO;
import com.tfg.mentoring.repository.InstitucionRepo;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizacionRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.NotificacionRepo;
import com.tfg.mentoring.repository.PeticionRepo;
import com.tfg.mentoring.repository.UsuarioRepo;
import com.tfg.mentoring.service.ActiveUsersService;
import com.tfg.mentoring.service.MapeadoService;
import com.tfg.mentoring.service.SalaChatServicio;
import com.tfg.mentoring.service.UserService;
import com.tfg.mentoring.service.util.ListLoad;

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
	private SalaChatServicio schats;
	@Autowired
	private ActiveUsersService acservice;
	@Autowired
	private MapeadoService mservice;

	@Autowired
	private ListLoad listas;
	@Autowired
	private SimpleDateFormat format;

/////////////////////////////////////////////////////////////////////////
////////                  Perfil de usuario                       ///////
/////////////////////////////////////////////////////////////////////////

	// Proveer la pagina del perfil
	@GetMapping("/perfil")
	public ModelAndView getPerfilPrivado(@AuthenticationPrincipal UserAuth us) {
		String rol = "";
		ModelAndView modelo;
		try {
			switch (us.getRol()) {
			case MENTOR:
				rol = "Mentor";
				Optional<Mentor> mentor = mrepo.findById(us.getUsername());
				if (mentor.isPresent()) {
					modelo = new ModelAndView("perfil");
					modelo.addObject("correo", mentor.get().getCorreo());
					modelo.addObject("rol", rol);
					modelo.addObject("fregistro", format.format(mentor.get().getFregistro()));
					modelo.addObject("password", "");
					uservice.addListasModelo(modelo);
					uservice.addInstitucionUtils(modelo, mentor.get().getInstitucion());
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
				rol = "Mentorizado";
				Optional<Mentorizado> mentorizado = menrepo.findById(us.getUsername());
				if (mentorizado.isPresent()) {
					modelo = new ModelAndView("perfil");
					modelo.addObject("correo", mentorizado.get().getCorreo());
					modelo.addObject("rol", rol);
					modelo.addObject("fregistro", format.format(mentorizado.get().getFregistro()));
					modelo.addObject("password", "");
					uservice.addListasModelo(modelo);
					uservice.addInstitucionUtils(modelo, mentorizado.get().getInstitucion());
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
				modelo = new ModelAndView("error_page");
				modelo.addObject("mensaje", "No estas autorizado a acceder a esta página con tu rol actual.");
				modelo.addObject("hora", new Date());
				return modelo;
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			ModelAndView model = new ModelAndView("error_page_loged");
			model.addObject("mensaje",
					"No ha sido posible acceder al repositorio de la aplicación, por favor, inténtelo más tarde");
			model.addObject("rol", rol);
			model.addObject("hora", new Date());
			return model;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje", "Se ha producido un error inesperado en el servidor, del tipo: "
					+ e.getClass().getCanonicalName() + ", por favor, si recibe este mensaje, "
					+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			model.addObject("hora", new Date());
			return model;
		}

	}

	// Obtener la informacion del perfil del usuario
	@GetMapping("/info")
	public ResponseEntity<PerfilDTO> getInfoPerfil(@AuthenticationPrincipal UserAuth us) {
		try {
			PerfilDTO up;
			switch (us.getRol()) {
			case MENTOR:
				Optional<Mentor> m = mrepo.findById(us.getUsername());
				if (m.isPresent()) {
					// UsuarioPerfil up = new UsuarioPerfil(m.get());
					up = mservice.getPerfilMentor(m.get());
					up.setNotificar_correo(m.get().getUsuario().isNotificar_correo());
					up.setMentor(true);
					return new ResponseEntity<>(up, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}
			case MENTORIZADO:
				Optional<Mentorizado> me = menrepo.findById(us.getUsername());
				if (me.isPresent()) {
					// UsuarioPerfil up = new UsuarioPerfil(m.get());
					up = mservice.getPerfilMentorizado(me.get());
					up.setNotificar_correo(me.get().getUsuario().isNotificar_correo());
					up.setMentor(false);
					up.setHoraspormes(4); // Le damos el valor por defecto para evitar error al enviar
					return new ResponseEntity<>(up, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}
			default:
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (EntityNotFoundException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Actualizar la informacion del perfil del usuario
	@PostMapping("/setinfo")
	public ResponseEntity<Usuario> setInfoPerfil(@RequestBody PerfilDTO up, @AuthenticationPrincipal UserAuth us) {
		if (up.getNombre() == null) {
			System.out.println("El nombre estaba vacío");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		try {
			switch (us.getRol()) {
			case MENTOR:
				Optional<Mentor> m = mrepo.findById(us.getUsername());
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
					return new ResponseEntity<>(null, HttpStatus.OK);

				} else {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}
			case MENTORIZADO:
				Optional<Mentorizado> me = menrepo.findById(us.getUsername());
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
					return new ResponseEntity<>(null, HttpStatus.OK);

				} else {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}

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

	// Eliminar areas de conocimiento de un usuario
	@PostMapping("/areas/delete")
	public ResponseEntity<MensajeError> borrarAreaUsuario(@AuthenticationPrincipal UserAuth us,
			@RequestBody AreaConocimiento area) {
		if (area.getArea() == null) {
			System.out.println("El area estaba vacía");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		try {
			switch (us.getRol()) {
			case MENTOR:
				Optional<Mentor> m = mrepo.findById(us.getUsername());
				if (m.isPresent()) {
					mrepo.borrarArea(us.getUsername(), area.getArea());
					return new ResponseEntity<>(null, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(new MensajeError("Fallo en la peticion",
							"Se ha producido un problema al intentar acceder al la información de su cuenta, si recibe este mensaje,"
									+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
									+ new Date()),
							HttpStatus.NOT_FOUND);
				}
			case MENTORIZADO:
				Optional<Mentorizado> me = menrepo.findById(us.getUsername());
				if (me.isPresent()) {
					menrepo.borrarArea(us.getUsername(), area.getArea());
					return new ResponseEntity<>(null, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(new MensajeError("Fallo en la peticion",
							"Se ha producido un problema al intentar acceder al la información de su cuenta, si recibe este mensaje,"
									+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
									+ new Date()),
							HttpStatus.BAD_REQUEST);
				}

			default:
				return new ResponseEntity<>(new MensajeError("Sin autorización", "No tienes permiso para hacer esto"),
						HttpStatus.UNAUTHORIZED);
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

/////////////////////////////////////////////////////////////////////////
////////                  Notificaciones                          ///////
/////////////////////////////////////////////////////////////////////////

	// Obtener notificaciones
	@GetMapping("/notificaciones")
	public ResponseEntity<List<NotificacionDTO>> getAllNotificaciones(@AuthenticationPrincipal UserAuth us) {
		try {
			List<Notificacion> Notificaciones = new ArrayList<Notificacion>();
			List<NotificacionDTO> nUser = new ArrayList<NotificacionDTO>();
			Notificaciones = notrepo.getNotificaciosUser(us.getUsername());
			if (Notificaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			notrepo.actualizaEstadoNotificaciosUser(us.getUsername());
			for (Notificacion n : Notificaciones) {
				nUser.add(new NotificacionDTO(n));
			}
			return new ResponseEntity<>(nUser, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Borrar una notificacion
	@PostMapping("/notificaciones/delete")
	public ResponseEntity<MensajeError> borrarNotificacion(@RequestBody long id) {
		try {
			notrepo.borrarNotificacion(id);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

/////////////////////////////////////////////////////////////////////////
////////             Pagina principal usuarios                    ///////
/////////////////////////////////////////////////////////////////////////

	@GetMapping("/principal")
	public ModelAndView getPaginaPrincipal(@AuthenticationPrincipal UserAuth us) {
		try {
			switch (us.getRol()) {
			case MENTOR:
				Optional<Mentor> mentor = mrepo.findById(us.getUsername());
				if (mentor.isPresent()) {
					ModelAndView modelo = new ModelAndView("prMentor");
					uservice.addInstitucionUtils(modelo, mentor.get().getInstitucion());
					acservice.salirChat(us.getUsername());
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
					ModelAndView modelo = new ModelAndView("prMentorizado");
					modelo.addObject("instituciones", listas.getInstituciones());
					modelo.addObject("areas", listas.getAreas());
					uservice.addInstitucionUtils(modelo, mentorizado.get().getInstitucion());
					acservice.salirChat(us.getUsername());
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
		} catch (Exception e) {
			System.out.println(e.getMessage());
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje", "Se ha producido un error inesperado en el servidor, del tipo: "
					+ e.getClass().getCanonicalName() + ", por favor, si recibe este mensaje, "
					+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			model.addObject("hora", new Date());
			return model;
		}
	}

	@PostMapping("/eliminar")
	public ModelAndView eliminarCuentaUsuario(@AuthenticationPrincipal UserAuth us,
			@ModelAttribute("password") String password, HttpServletRequest request, HttpServletResponse response) {
		try {
			if (uservice.comprobarPassword(password, us)) {
				String username = us.getUsername();
				String rol = "";
				Usuario u;
				ModelAndView modelo;
				try {
					switch (us.getRol()) {
					case MENTOR:
						rol = "Mentor";
						u = urepo.findByUsername(username);
						if (u == null) {
							modelo = new ModelAndView("error_page_loged");
							modelo.addObject("mensaje",
									"Se ha producido un fallo al intentar acceder a su cuenta, por favor, si recibe este mensaje,"
											+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
							modelo.addObject("rol", rol);
							modelo.addObject("hora", new Date());
							return modelo;
						}
						prepo.borrarPeticionesMentor(username);
						schats.cerrarChatSalirMentor(username);
						mentorizacionrepo.borrarMentorizacionesMentor(username);
						mrepo.borrarMentor(username);
						urepo.borrarUsuario(username);// Esto lo ultimo, para que, en el peor de los casos, un usuario
						// pueda volver a logearse para intentar volver a borrar de nuevo su cuenta
						uservice.notificarPorCorreo(u, "Cuenta de mentoring eliminada",
								"Le notificamos que su cuenta de usuario ha sido eliminada de forma exitosa. Recuerde que no puede<br>"
										+ "volver a usar esta cuenta de correo para registrar otra cuenta en nuestra aplicación. <br>"
										+ "Le damos las gracias por todo lo que haya aportado y le queremos desear mucha suerte.");
					case MENTORIZADO:
						rol = "Mentorizado";
						u = urepo.findByUsername(username);
						if (u == null) {
							modelo = new ModelAndView("error_page_loged");
							modelo.addObject("mensaje",
									"Se ha producido un fallo al intentar acceder a su cuenta, por favor, si recibe este mensaje,"
											+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
							modelo.addObject("rol", rol);
							modelo.addObject("hora", new Date());
							return modelo;
						}
						prepo.borrarPeticionesMentorizado(username);
						schats.cerrarChatSalirMentorizado(username);
						mentorizacionrepo.borrarMentorizacionesMentorizado(username);
						menrepo.borrarMentorizado(username);
						urepo.borrarUsuario(username);
						uservice.notificarPorCorreo(u, "Cuenta de mentoring eliminada",
								"Le notificamos que su cuenta de usuario ha sido eliminada de forma exitosa. Recuerde que no puede<br>"
										+ "volver a usar esta cuenta de correo para registrar otra cuenta en nuestra aplicación. <br>"
										+ "Le damos las gracias por todo lo que haya aportado y le queremos desear mucha suerte.");
					default:
						System.out.println("Otro rol");
						modelo = new ModelAndView("error_page");
						modelo.addObject("mensaje", "No estas autorizado a acceder a esta página con tu rol actual.");
						modelo.addObject("hora", new Date());
						return modelo;
					}
					
				} catch (JDBCConnectionException | QueryTimeoutException e) {
					System.out.println(e.getMessage());
					ModelAndView model = new ModelAndView("error_page_loged");
					model.addObject("mensaje",
							"No ha sido posible acceder al repositorio de la aplicación, por favor, inténtelo más tarde");
					model.addObject("rol", rol);
					model.addObject("hora", new Date());
					return model;
				} catch (MessagingException | UnsupportedEncodingException e) {// Si falla el envio del correo no es
					// muy importante
					System.out.println(e.getMessage());
				} catch (Exception e) {
					System.out.println(e.getMessage());
					ModelAndView model = new ModelAndView("error_page");
					model.addObject("mensaje", "Se ha producido un error inesperado en el servidor, del tipo: "
							+ e.getClass().getCanonicalName() + ", por favor, si recibe este mensaje, "
							+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
					model.addObject("hora", new Date());
					return model;
				}
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (auth != null) {
					new SecurityContextLogoutHandler().logout(request, response, auth);
				}
				return new ModelAndView("home");
			} else {
				System.out.println("La contraseña no coincide");
				ModelAndView modelo = new ModelAndView("perfil");
				modelo.addObject("correo", us.getUsername());
				switch (us.getRol()) {
				case MENTOR:
					modelo.addObject("rol", "Mentor");
					break;
				case MENTORIZADO:
					modelo.addObject("rol", "Mentorizado");
					break;
				default:
					modelo.addObject("rol", "Otro");
					break;
				}
				modelo.addObject("password", "");
				uservice.addListasModelo(modelo);
				modelo.addObject("error", "La contraseña introducida al intentar eliminar la cuenta no era correcta");
				return modelo;
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje", "Se ha producido un error inesperado en el servidor, del tipo: "
					+ e.getClass().getCanonicalName() + ", por favor, si recibe este mensaje, "
					+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			model.addObject("hora", new Date());
			return model;
		}
	}

	// Esto nos sirve para que AngularJS pueda saber si el usuario es o no mentor,
	// para el chat, por ejemplo
	@GetMapping("/miinfo")
	public ResponseEntity<UserAuthDTO> miRol(@AuthenticationPrincipal UserAuth us) {
		switch (us.getRol()) {
		case MENTOR:
			return new ResponseEntity<>(new UserAuthDTO(us.getUsername(), true), HttpStatus.OK);
		case MENTORIZADO:
			return new ResponseEntity<>(new UserAuthDTO(us.getUsername(), false), HttpStatus.OK);
		default:
			return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
		}
	}

}
