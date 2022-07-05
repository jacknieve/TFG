package com.tfg.mentoring.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.service.util.ListLoad;

@Service
public class RedirectService {
	
	@Autowired
	private UserService uservice;
	@Autowired
	private ActiveUsersService acservice;
	
	@Autowired
	private MentorizadoRepo menrepo;
	@Autowired
	private MentorRepo mrepo;
	
	@Autowired
	private SimpleDateFormat format;
	@Autowired
	private ListLoad listas;
	

	
	public ModelAndView devolverChatMentor(String username) throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentor> mentor = mrepo.findById(username);
		if (mentor.isPresent()) {
			// Asumimos que el usuario se considera logeado
			acservice.entrarChat(username);
			ModelAndView modelo = new ModelAndView("chat");
			Mentor m = mentor.get();
			uservice.addInstitucionUtils(modelo, m.getInstitucion());
			modelo.addObject("nombre", m.getNombre() + " " + m.getPapellido() + " " + m.getSapellido());
			if (m.getUsuario().getFoto() != null) {
				modelo.addObject("foto",
						"/imagenes/mentores/" + m.getCorreo() + "/" + m.getUsuario().getFoto());
			} else {
				modelo.addObject("foto", "/images/usuario.png");
			}
			return modelo;
		} else {
			System.out.println("No existe");
			ModelAndView modelo = new ModelAndView("error_page");
			modelo.addObject("mensaje",
					"No ha sido posible acceder a la información de su perfil, por favor, si recibe este mensaje, "
							+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			modelo.addObject("hora", new Date());
			return modelo;
		}
	}
	
	public ModelAndView devolverChatMentorizado(String username) throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentorizado> mentorizado = menrepo.findById(username);
		if (mentorizado.isPresent()) {
			acservice.entrarChat(username);
			//System.out.println(mentorizado.get().toString());
			Mentorizado m = mentorizado.get();
			ModelAndView modelo = new ModelAndView("chat");
			uservice.addInstitucionUtils(modelo, m.getInstitucion());
			modelo.addObject("nombre", m.getNombre() + " " + m.getPapellido() + " " + m.getSapellido());
			if (m.getUsuario().getFoto() != null) {
				modelo.addObject("foto",
						"/imagenes/mentorizados/" + m.getCorreo() + "/" + m.getUsuario().getFoto());
			} else {
				modelo.addObject("foto", "/images/usuario.png");
			}
			return modelo;
		} else {
			System.out.println("No existe");
			ModelAndView modelo = new ModelAndView("error_page");
			modelo.addObject("mensaje",
					"No ha sido posible acceder a la información de su perfil, por favor, si recibe este mensaje, "
							+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			modelo.addObject("hora", new Date());
			return modelo;
		}
	}
	
	public ModelAndView devolverInicioMentor(String username) throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentor> mentor = mrepo.findById(username);
		if (mentor.isPresent()) {
			ModelAndView modelo = new ModelAndView("home_login");
			modelo.addObject("rol", "Mentor");
			uservice.addInstitucionUtils(modelo, mentor.get().getInstitucion());
			return modelo;
		} else {
			ModelAndView modelo = new ModelAndView("error_page");
			modelo.addObject("mensaje",
					"No ha sido posible acceder a la información de su perfil, por favor, si recibe este mensaje, "
							+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			modelo.addObject("hora", new Date());
			return modelo;
		}
	}
	
	public ModelAndView devolverInicioMentorizado(String username) throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentor> mentor = mrepo.findById(username);
		if (mentor.isPresent()) {
			ModelAndView modelo = new ModelAndView("home_login");
			modelo.addObject("rol", "Mentorizado");
			uservice.addInstitucionUtils(modelo, mentor.get().getInstitucion());
			return modelo;
		} else {
			ModelAndView modelo = new ModelAndView("error_page");
			modelo.addObject("mensaje",
					"No ha sido posible acceder a la información de su perfil, por favor, si recibe este mensaje, "
							+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			modelo.addObject("hora", new Date());
			return modelo;
		}
	}
	
	public ModelAndView devolverPerfilMentor(String username) throws JDBCConnectionException, QueryTimeoutException{
		ModelAndView modelo;
		Optional<Mentor> mentor = mrepo.findById(username);
		if (mentor.isPresent()) {
			modelo = new ModelAndView("perfil");
			modelo.addObject("correo", mentor.get().getCorreo());
			modelo.addObject("rol", "Mentor");
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
	}
	
	public ModelAndView devolverPerfilMentorizado(String username) throws JDBCConnectionException, QueryTimeoutException{
		ModelAndView modelo;
		Optional<Mentorizado> mentorizado = menrepo.findById(username);
		if (mentorizado.isPresent()) {
			modelo = new ModelAndView("perfil");
			modelo.addObject("correo", mentorizado.get().getCorreo());
			modelo.addObject("rol", "Mentorizado");
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
	}
	
	public ModelAndView getPrincipalMentor(String username) throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentor> mentor = mrepo.findById(username);
		if (mentor.isPresent()) {
			ModelAndView modelo = new ModelAndView("prMentor");
			uservice.addInstitucionUtils(modelo, mentor.get().getInstitucion());
			acservice.salirChat(username);
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
	}
	
	public ModelAndView getPrincipalMentorizado(String username) throws JDBCConnectionException, QueryTimeoutException{
		Optional<Mentorizado> mentorizado = menrepo.findById(username);
		if (mentorizado.isPresent()) {
			ModelAndView modelo = new ModelAndView("prMentorizado");
			modelo.addObject("instituciones", listas.getInstituciones());
			modelo.addObject("areas", listas.getAreas());
			uservice.addInstitucionUtils(modelo, mentorizado.get().getInstitucion());
			acservice.salirChat(username);
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
	}
}
