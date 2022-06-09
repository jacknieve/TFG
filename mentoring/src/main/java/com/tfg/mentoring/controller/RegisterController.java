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
import com.tfg.mentoring.model.auxiliar.requests.UserAux;
import com.tfg.mentoring.service.UserService;

@RestController
@RequestMapping("/auth")
public class RegisterController {

	@Autowired
	private UserService uservice;

	@ModelAttribute("useraux")
	public UserAux useraux() {
		return new UserAux();
	}

	@GetMapping("/registration/{rol}")
	public ModelAndView showRegistrationForm(@PathVariable("rol") String rol,
			@AuthenticationPrincipal UserAuth us) {
		ModelAndView modelo;
		if (us == null) {
			if (rol == null) {
				modelo = new ModelAndView("error_page");
				modelo.addObject("mensaje", "Los parámetros de la dirección no son correctos.");
				return modelo;
			}
			UserAux useraux = new UserAux();
			useraux.setHoraspormes(4);
			modelo = new ModelAndView("registro");
			if (rol.equals("mentor")) {
				useraux.setMentor(true);
				useraux.setMensajeCambio("Prefiero registrarme como mentorizado");
			} else {
				useraux.setMentor(false);
				useraux.setMensajeCambio("Prefiero registrarme como mentor");
			}
			modelo.addObject("useraux", useraux);
			uservice.addListasModeloSinAreas(modelo);
			return modelo;
		} else {
			switch (us.getRol()) {
			case MENTOR:
				modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentor");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;
			case MENTORIZADO:
				modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentorizado");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;

			default:
				System.out.println("Otro rol");
				modelo = new ModelAndView("error_page");
				modelo.addObject("mensaje", "No ha sido posible reconocer tu rol.");
				modelo.addObject("hora", new Date());
				return modelo;
			}
		}
	}

	@PostMapping("/register/cambio")
	public ModelAndView cambioRolRegistro(@ModelAttribute("useraux") UserAux useraux,
			@AuthenticationPrincipal UserAuth us) {
		ModelAndView modelo;
		if (us == null) {
			System.out.println(useraux.toString());
			modelo = new ModelAndView("registro");
			useraux.setHoraspormes(4);
			if (useraux.getMentor()) {
				useraux.setMentor(false);
				useraux.setMensajeCambio("Prefiero registrarme como mentor");
			} else {
				useraux.setMentor(true);
				useraux.setMensajeCambio("Prefiero registrarme como mentorizado");
			}
			modelo.addObject("useraux", useraux);
			uservice.addListasModeloSinAreas(modelo);
			return modelo;
		} else {// Aqui por que ya se habia logeado
			switch (us.getRol()) {
			case MENTOR:
				modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentor");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;
			case MENTORIZADO:
				modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentorizado");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;
			default:
				System.out.println("Otro rol");
				modelo = new ModelAndView("error_page");
				modelo.addObject("mensaje", "No ha sido posible reconocer tu rol.");
				modelo.addObject("hora", new Date());
				return modelo;
			}
		}
	}

	@PostMapping("/register")
	public ModelAndView registerUser(@Valid @ModelAttribute("useraux") UserAux useraux, BindingResult result,
			HttpServletRequest request, @AuthenticationPrincipal UserAuth us) {
		if (us == null) {
			if (result.hasErrors()) {
				//System.out.println(useraux.toString());
				ModelAndView model = new ModelAndView("registro");
				model.addObject("useraux", useraux);
				uservice.addListasModeloSinAreas(model);
				return model;
			}
			try {
				uservice.register(useraux, getSiteURL(request));
			} catch (MessagingException | UnsupportedEncodingException | JDBCConnectionException
					| QueryTimeoutException e) {
				System.out.println(e.getMessage());
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
				try {
					uservice.limpiarUsuario(useraux);
				} catch (JDBCConnectionException | QueryTimeoutException ex) {
					System.out.println(e.getMessage());
					System.out.println("No hay sido posible limpiar al usuario");
				}
				return prepareModelToError(useraux, e.getMessage());
			} catch (SecurityException | FileNotFoundException e) {
				System.out.println(e.getMessage());
				try {
					uservice.limpiarUsuario(useraux);
				} catch (JDBCConnectionException | QueryTimeoutException ex) {
					System.out.println(e.getMessage());
					System.out.println("No hay sido posible limpiar al usuario");
				}
				return prepareModelToError(useraux, "No ha sido posible crear los directorios del usuario");
			} catch (ExcepcionDB e) {
				System.out.println(e.getMessage());
				return prepareModelToError(useraux, "El correo indicado ya está registrado");
			} catch (Exception e) {// Otra excepción
				System.out.println(e.getMessage());
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
			ModelAndView modelo;
			switch (us.getRol()) {
			case MENTOR:
				modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentor");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;
			case MENTORIZADO:
				modelo = new ModelAndView("error_page_loged");
				modelo.addObject("rol", "Mentorizado");
				modelo.addObject("mensaje", "Ya te has iniciado sesión, no puedes acceder aquí.");
				modelo.addObject("hora", new Date());
				return modelo;
			default:
				System.out.println("Otro rol");
				modelo = new ModelAndView("error_page");
				modelo.addObject("mensaje", "No ha sido posible reconocer tu rol.");
				modelo.addObject("hora", new Date());
				return modelo;
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
		try {
			if (uservice.verify(code)) {
				return new ModelAndView("verify_success");
			} else {
				return new ModelAndView("verify_fail");
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje",
					"No ha sido posible acceder al repositorio de la aplicación, por favor, inténtelo más tarde");
			model.addObject("hora", new Date());
			return model;
		} catch (Exception e) {
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
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return new ModelAndView("afterlogout");
	}


}
