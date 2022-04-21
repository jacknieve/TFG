package com.tfg.mentoring.controller;



import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.exceptions.ExcepcionDB;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.UserAux;
import com.tfg.mentoring.service.UserService;
import com.tfg.mentoring.service.util.ListLoad;

//@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/auth")
public class RegisterController {
	
	/*@Autowired
	private InstitucionRepo irepo;*/
	
	@Autowired
    private UserService uservice;
	
	@Autowired
	private ListLoad listas;
	
	
	
	//Esto para que se llame al crearlo, para configurar cosas o leer desde fichero
	/*@Autowired
	public RegisterController() {
		
	}*/
	
	@ModelAttribute("user")
	public Usuario usuario() {
		return new Usuario();
	}
	
	@ModelAttribute("useraux")
	public UserAux useraux() {
		return new UserAux();
	}
	
	//Probar a pasar en el prototipo argumentos a un metodo como este
	@GetMapping("/registration")
	public ModelAndView showRegistrationForm(HttpServletRequest request) {
		Usuario user = new Usuario();
	    UserAux useraux = new UserAux();
	    ModelAndView model = new ModelAndView("register");
	    //System.out.println(getSiteURL(request));
	    model.addObject("user", user);
	    //System.out.println(useraux.toString());
	    model.addObject("useraux", useraux);
	    model.addObject("puestos", listas.getPuestos());
	    model.addObject("estudios", listas.getEstudios());
	    model.addObject("instituciones", listas.getInstituciones());
	    return model;
	}
	
	
	@PostMapping("/register")
	public ModelAndView registerUserAccount(@ModelAttribute("user") Usuario user, @ModelAttribute("useraux") UserAux useraux,
			HttpServletRequest request) {
	    try {
	    	uservice.register(user, useraux, getSiteURL(request));
	    }catch (MessagingException e) {
	    	return new ModelAndView("error_page");
		}catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
			return new ModelAndView("error_page");
		}catch (ExcepcionDB e) {
			System.out.println(e.getMessage());
			ModelAndView modelo = new ModelAndView("error_page");
			modelo.addObject("mensaje", e.getMessage());
			return modelo;
		}
	    return new ModelAndView("login");
	}
	
	@GetMapping("/verify")
	public ModelAndView verificarUsuario(@Param("code") String code) {
		System.out.println(code);
		if(uservice.verify(code)) {
			return new ModelAndView("verify_success");
		}
		else {
			return new ModelAndView("verify_fail");
		}
	}
	
	//Nos devuelve la ruta de contexto de la aplicación, luego se le añade el sufijo necesario 
	//(/verify?code=) junto con el código para que pueda llamar al método de verificación ya con el código de verificación
	private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    } 
	
	
	@GetMapping("/logout")
	public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {
		 Authentication auth = SecurityContextHolder.getContext().getAuthentication();  
		 System.out.println(auth.getDetails().toString());
	        if (auth != null){      
	           new SecurityContextLogoutHandler().logout(request, response, auth);  
	        }  
	    return new ModelAndView("afterlogout");
	}
	
	/*@GetMapping("/autologout")
	public ResponseEntity<String> autologout(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("kk");
		 Authentication auth = SecurityContextHolder.getContext().getAuthentication();  
		 System.out.println(auth.getDetails().toString());
	        if (auth != null){      
	           new SecurityContextLogoutHandler().logout(request, response, auth);  
	        }  
	        return new ResponseEntity<>(null, HttpStatus.OK);
	}*/
	
	
	

}
