package com.tfg.mentoring.controller;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.mail.MessagingException;
import javax.persistence.QueryTimeoutException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.exceptions.ExcepcionDB;
import com.tfg.mentoring.exceptions.ExcepcionRecursos;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.enums.Roles;
import com.tfg.mentoring.model.auxiliar.requests.UserAux;
import com.tfg.mentoring.service.UserService;

//@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/auth")
public class RegisterController {

	/*
	 * @Autowired private InstitucionRepo irepo;
	 */

	@Autowired
	private UserService uservice;

	// Esto para que se llame al crearlo, para configurar cosas o leer desde fichero
	/*
	 * @Autowired public RegisterController() {
	 * 
	 * }
	 */

	@ModelAttribute("useraux")
	public UserAux useraux() {
		return new UserAux();
	}

	// Probar a pasar en el prototipo argumentos a un metodo como este
	@GetMapping("/registration/{mentor}")
	public ModelAndView showRegistrationForm(HttpServletRequest request, @PathVariable("mentor") String mentor,
			@AuthenticationPrincipal UserAuth us) {
		if (us == null) {
			// Usuario user = new Usuario();
			if (mentor == null) {// aqui por null en los parametros
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje", "Los parámetros de la dirección no son correctos.");
				return model;
			}
			UserAux useraux = new UserAux();
			useraux.setHoraspormes(4);
			ModelAndView model = new ModelAndView("registro");
			System.out.println(mentor);
			if (mentor.equals("mentor")) {
				useraux.setMentor(true);
				useraux.setMensajeCambio("Prefiero registrarme como mentorizado");
			} else {
				useraux.setMentor(false);
				useraux.setMensajeCambio("Prefiero registrarme como mentor");
			}
			System.out.println(useraux.toString());
			model.addObject("useraux", useraux);
			uservice.addListasModeloSinAreas(model);
			return model;
		} else {// Aqui porque ya se habia logeado
			if (us.getUsername() == null) {
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje",
						"Se ha producido un error con su información de acceso, por favor, si recibe este mensaje, "
								+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
				model.addObject("hora", new Date());
				return model;
			}
			if (us.getRol() == Roles.MENTOR) {
				ModelAndView modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentor");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;
			} else if (us.getRol() == Roles.MENTORIZADO) {
				ModelAndView modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentorizado");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;
			} else {
				System.out.println("Otro rol");
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje", "No ha sido posible reconocer tu rol.");
				model.addObject("hora", new Date());
				return model;
			}
		}
	}

	@PostMapping("/register/cambio")
	public ModelAndView cambioRolRegistro(@ModelAttribute("useraux") UserAux useraux,
			@AuthenticationPrincipal UserAuth us) {
		System.out.println("cambio");
		if (us == null) {
			if (useraux != null) {
				System.out.println(useraux.toString());
				ModelAndView model = new ModelAndView("registro");
				useraux.setHoraspormes(4);
				if (useraux.getMentor()) {
					useraux.setMentor(false);
					useraux.setMensajeCambio("Prefiero registrarme como mentor");
				} else {
					useraux.setMentor(true);
					useraux.setMensajeCambio("Prefiero registrarme como mentorizado");
				}
				model.addObject("useraux", useraux);
				uservice.addListasModeloSinAreas(model);
				return model;
			} else {// Aqui por que el cuerpo estaba a null
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje",
						"Se ha producido un error al realizar la petición al servidor, por favor, si recibe este mensaje, "
								+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
				model.addObject("hora", new Date());
				return model;
			}
		} else {// Aqui por que ya se habia logeado
			if (us.getUsername() == null) {
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje",
						"Se ha producido un error con su información de acceso, por favor, si recibe este mensaje, "
								+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
				model.addObject("hora", new Date());
				return model;
			}
			if (us.getRol() == Roles.MENTOR) {
				ModelAndView modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentor");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;
			} else if (us.getRol() == Roles.MENTORIZADO) {
				ModelAndView modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentorizado");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;
			} else {
				System.out.println("Otro rol");
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje", "No ha sido posible reconocer tu rol.");
				model.addObject("hora", new Date());
				return model;
			}
		}
	}

	@PostMapping("/register")
	public ModelAndView registerUserAccount(@Valid @ModelAttribute("useraux") UserAux useraux, BindingResult result,
			HttpServletRequest request, @AuthenticationPrincipal UserAuth us) {
		System.out.println("registro");
		if (useraux == null) { // Peticion nula
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje",
					"Se ha producido un error al realizar la petición al servidor, por favor, si recibe este mensaje, "
							+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			model.addObject("hora", new Date());
			return model;
		}
		if (us == null) {
			if (result.hasErrors()) {
				System.out.println(useraux.toString());
				ModelAndView model = new ModelAndView("registro");
				model.addObject("useraux", useraux);
				uservice.addListasModeloSinAreas(model);
				return model;
			}
			try {
				uservice.register(useraux, getSiteURL(request));
			} catch (MessagingException | UnsupportedEncodingException | JDBCConnectionException | QueryTimeoutException e) {
				System.out.println(e.getMessage());
				System.out.println(e);
				try {
					uservice.limpiarUsuario(useraux);
				} catch (JDBCConnectionException | QueryTimeoutException ex) {
					System.out.println(e.getMessage());
					System.out.println("No hay sido posible limpiar al usuario");
				}
				return prepareModelToError(useraux,
						"Se ha producido un problema al intentar enviar el correo, por favor, intente registrarse más tarde");
			} catch (ExcepcionRecursos e) {
				System.out.println(e.getMessage());
				System.out.println(e);
				try {
					uservice.limpiarUsuario(useraux);
				} catch (JDBCConnectionException | QueryTimeoutException ex) {
					System.out.println(e.getMessage());
					System.out.println("No hay sido posible limpiar al usuario");
				}
				return prepareModelToError(useraux, e.getMessage());
			} catch (SecurityException | FileNotFoundException e) {
				System.out.println(e.getMessage());
				System.out.println(e);
				try {
					uservice.limpiarUsuario(useraux);
				} catch (JDBCConnectionException | QueryTimeoutException ex) {
					System.out.println(e.getMessage());
					System.out.println("No hay sido posible limpiar al usuario");
				}
				return prepareModelToError(useraux, "No ha sido posible crear los directorios del usuario");
			} catch (ExcepcionDB e) {
				System.out.println(e.getMessage());
				System.out.println(e);
				return prepareModelToError(useraux, "El correo indicado ya está registrado");
			} catch (Exception e) {// Otra excepción
				System.out.println(e.getMessage());
				System.out.println(e);
				try {
					uservice.limpiarUsuario(useraux);
				} catch (JDBCConnectionException | QueryTimeoutException ex) {
					System.out.println(e.getMessage());
					System.out.println("No hay sido posible limpiar al usuario");
				}
				return prepareModelToError(useraux,
						"Se ha producido un error inesperado, por favor, si lee este mensaje, contacte con nosotros y "
								+ "detallenos el contexto en el que ha ocurrido el error y sea lo más preciso posible con la hora del suceso.");
			}
			return new ModelAndView("register_success");
		} else { // Por estar ya logeado
			if (us.getUsername() == null) {
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje",
						"Se ha producido un error con su información de acceso, por favor, si recibe este mensaje, "
								+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
				model.addObject("hora", new Date());
				return model;
			}
			if (us.getRol() == Roles.MENTOR) {
				ModelAndView modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentor");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;
			} else if (us.getRol() == Roles.MENTORIZADO) {
				ModelAndView modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentorizado");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;
			} else {
				System.out.println("Otro rol");
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje", "No ha sido posible reconocer tu rol.");
				model.addObject("hora", new Date());
				return model;
			}
		}
	}

	private ModelAndView prepareModelToError(UserAux useraux, String error) {
		ModelAndView model = new ModelAndView("registro");
		model.addObject("useraux", useraux);
		uservice.addListasModeloSinAreas(model);
		model.addObject("errorGlobal", error);
		return model;
	}

	@GetMapping("/verify")
	public ModelAndView verificarUsuario(@Param("code") String code) {
		if (code == null) {// Por atributo vacio
			return new ModelAndView("error_page");
		}
		System.out.println(code);
		try {
			if (uservice.verify(code)) {
				return new ModelAndView("verify_success");
			} else {
				return new ModelAndView("verify_fail");
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			// Error al acceder, que lo vuelva a intentar
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje",
					"No ha sido posible acceder al repositorio de la aplicación, por favor, inténtelo más tarde");
			model.addObject("hora", new Date());
			return model;
		} catch (Exception e) {
			// Otro error, inesperado
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje", "Se ha producido un error inesperado en el servidor, del tipo: "
					+ e.getClass().getCanonicalName() + ", por favor, si recibe este mensaje, "
					+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			model.addObject("hora", new Date());
			return model;
		}
	}

	// Nos devuelve la ruta de contexto de la aplicación, luego se le añade el
	// sufijo necesario
	// (/verify?code=) junto con el código para que pueda llamar al método de
	// verificación ya con el código de verificación
	private String getSiteURL(HttpServletRequest request) {
		String siteURL = request.getRequestURL().toString();
		return siteURL.replace(request.getServletPath(), "");
	}

	@GetMapping("/logout")
	public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println(auth.getDetails().toString());
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return new ModelAndView("afterlogout");
	}

	/*
	 * @GetMapping("/autologout") public ResponseEntity<String>
	 * autologout(HttpServletRequest request, HttpServletResponse response) {
	 * System.out.println("kk"); Authentication auth =
	 * SecurityContextHolder.getContext().getAuthentication();
	 * System.out.println(auth.getDetails().toString()); if (auth != null){ new
	 * SecurityContextLogoutHandler().logout(request, response, auth); } return new
	 * ResponseEntity<>(null, HttpStatus.OK); }
	 */

}
