package com.tfg.mentoring.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.Roles;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.Listas;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;


@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private MentorizadoRepo menrepo;
	@Autowired
	private MentorRepo mrepo;
	
	@Autowired
	private Listas listas;
	
	@GetMapping("/perfil")
	public ModelAndView getPerfilPrivado(@AuthenticationPrincipal Usuario us) {
		try {
			if(us.getRol().equals(Roles.MENTOR)) {
				Optional<Mentor> mentor = mrepo.findById(us.getUsername());
				if(mentor.isPresent()) {
					System.out.println(mentor.get().toString());
					ModelAndView modelo = new ModelAndView("perfil");
					modelo.addObject("usuario", mentor.get());
					modelo.addObject("puestos", listas.getPuestos());
				    modelo.addObject("estudios", listas.getPuestos());
					return modelo;
				}
				else {
					return new ModelAndView("error_page");
				}
			}
			else if(us.getRol().equals(Roles.MENTORIZADO)){
				Optional<Mentorizado> mentorizado = menrepo.findById(us.getUsername());
				if(mentorizado.isPresent()) {
					System.out.println(mentorizado.get().toString());
					ModelAndView modelo = new ModelAndView("perfil");
					modelo.addObject("usuario", mentorizado.get());
					modelo.addObject("puestos", listas.getPuestos());
				    modelo.addObject("estudios", listas.getPuestos());
					return modelo;
				}
				else {
					return new ModelAndView("error_page");
				}
			}
			else {
				return new ModelAndView("error_page");
			}
		} catch (Exception e) {
			return new ModelAndView("error_page");
		}
	}
	
	
}
