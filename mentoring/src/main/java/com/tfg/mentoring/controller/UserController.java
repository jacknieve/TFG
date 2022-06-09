package com.tfg.mentoring.controller;

import java.io.UnsupportedEncodingException;
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
import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.DTO.NotificacionDTO;
import com.tfg.mentoring.model.auxiliar.DTO.PerfilDTO;
import com.tfg.mentoring.model.auxiliar.DTO.UserAuthDTO;
import com.tfg.mentoring.service.RedirectService;
import com.tfg.mentoring.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService uservice;
	@Autowired
	private RedirectService rservice;


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
				return rservice.devolverPerfilMentor(us.getUsername());
			case MENTORIZADO:
				rol = "Mentorizado";
				return rservice.devolverPerfilMentorizado(us.getUsername());
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
			Optional<PerfilDTO> m;
			switch (us.getRol()) {
			case MENTOR:
				m = uservice.obtenerMiPerfilMentor(us.getUsername());
				if (m.isPresent()) {
					return new ResponseEntity<>(m.get(), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}
			case MENTORIZADO:
				m = uservice.obtenerMiPerfilMentorizado(us.getUsername());
				if (m.isPresent()) {
					return new ResponseEntity<>(m.get(), HttpStatus.OK);
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
	public ResponseEntity<MensajeError> setInfoPerfil(@RequestBody PerfilDTO up, @AuthenticationPrincipal UserAuth us) {
		if (up.getNombre() == null) {
			System.out.println("El nombre estaba vacío");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		try {
			Optional<MensajeError> error;
			switch (us.getRol()) {
			case MENTOR:
				error = uservice.setInfoMentor(up, us.getUsername());
				if (error.isPresent()) {				
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				} else {
					return new ResponseEntity<>(null, HttpStatus.OK);
				}
			case MENTORIZADO:
				error = uservice.setInfoMentorizado(up, us.getUsername());
				if (error.isPresent()) {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);

				} else {
					return new ResponseEntity<>(null, HttpStatus.OK);
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
			Optional<MensajeError> error = uservice.borrarArea(area.getArea(), us.getUsername(), us.getRol());
			if(error.isPresent()) {
				return new ResponseEntity<>(error.get(),HttpStatus.INTERNAL_SERVER_ERROR);
			}
			else {
				return new ResponseEntity<>(null, HttpStatus.OK);
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
			List<NotificacionDTO> notificaciones = uservice.obtenerNotificaciones(us.getUsername());
			if (notificaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(notificaciones, HttpStatus.OK);
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
	public ResponseEntity<MensajeError> borrarNotificacion(@RequestBody long id, @AuthenticationPrincipal UserAuth us) {
		try {
			uservice.borrarNotificacion(id, us.getUsername());
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
				return rservice.getPrincipalMentor(us.getUsername());
			case MENTORIZADO:
				return rservice.getPrincipalMentorizado(us.getUsername());
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
				ModelAndView modelo;
				Optional<ModelAndView> errorBorrar;
				try {
					switch (us.getRol()) {
					case MENTOR:
						rol = "Mentor";
						errorBorrar = uservice.borrarMentor(username);
						if(errorBorrar.isPresent()) {//En caso de que haya un modelo es que se produjo un error porque no se encontró al usuario
							return errorBorrar.get();
						}
						break;
					case MENTORIZADO:
						rol = "Mentorizado";
						errorBorrar = uservice.borrarMentorizado(username);
						if(errorBorrar.isPresent()) {
							return errorBorrar.get();
						}
						break;
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
