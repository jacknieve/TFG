package com.tfg.mentoring.controller;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.UserAux;
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

	@ModelAttribute("user")
	public Usuario usuario() {
		return new Usuario();
	}

	@ModelAttribute("useraux")
	public UserAux useraux() {
		return new UserAux();
	}

	// Probar a pasar en el prototipo argumentos a un metodo como este
	@GetMapping("/registration/{mentor}")
	public ModelAndView showRegistrationForm(HttpServletRequest request, @PathVariable("mentor") String mentor) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			// Usuario user = new Usuario();
			UserAux useraux = new UserAux();
			useraux.setHoraspormes(4);
			ModelAndView model = new ModelAndView("register");
			// System.out.println(getSiteURL(request));
			// model.addObject("user", user);
			// System.out.println(useraux.toString());
			// if(mentor.equals("mentor")) useraux.setMentor(true);
			// else useraux.setMentor(false);
			model.addObject("useraux", useraux);
			uservice.addListasModeloSinAreas(model);
			return model;
		} else {
			return new ModelAndView("error_page");
		}
	}

	@PostMapping("/register")
	public ModelAndView registerUserAccount(@Valid @ModelAttribute("useraux") UserAux useraux, BindingResult result,
			HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			if (result.hasErrors()) {
				System.out.println(useraux.toString());
				ModelAndView model = new ModelAndView("register");
				model.addObject("useraux", useraux);
				uservice.addListasModeloSinAreas(model);
				return model;
			}
			try {

				uservice.register(useraux, getSiteURL(request));
			} catch (MessagingException | UnsupportedEncodingException e) {
				// return new ModelAndView("error_page");
				System.out.println(e.getMessage());
				System.out.println(e);
				uservice.limpiarUsuario(useraux);
				ModelAndView model = new ModelAndView("register");
				model.addObject("useraux", useraux);
				uservice.addListasModeloSinAreas(model);
				// Habria que eliminar el usuario introducido
				model.addObject("errorGlobal",
						"Se ha producido un problema al intentar enviar el correo, por favor, intente registrarse más tarde");
				return model;
			} catch (ExcepcionDB e) {
				System.out.println(e.getMessage());
				System.out.println(e);
				ModelAndView model = new ModelAndView("register");
				model.addObject("useraux", useraux);
				uservice.addListasModeloSinAreas(model);
				// Habria que eliminar el usuario introducido
				model.addObject("errorGlobal", "El correo indicado ya está registrado");
				return model;
			}
			return new ModelAndView("login");
		} else {
			return new ModelAndView("error_page");
		}
	}

	@GetMapping("/verify")
	public ModelAndView verificarUsuario(@Param("code") String code) {
		System.out.println(code);
		if (uservice.verify(code)) {
			return new ModelAndView("verify_success");
		} else {
			return new ModelAndView("verify_fail");
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
	 @GetMapping("/autologout") 
	 public ResponseEntity<String> autologout(HttpServletRequest request, HttpServletResponse response) {
	 		System.out.println("kk"); 
	 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	 		System.out.println(auth.getDetails().toString()); 
	 		if (auth != null){ 
	 			new SecurityContextLogoutHandler().logout(request, response, auth); 
	 		} 
	 		return new ResponseEntity<>(null, HttpStatus.OK); 
	 }
	 */

}
