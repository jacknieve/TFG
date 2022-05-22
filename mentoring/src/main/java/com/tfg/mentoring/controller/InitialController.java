package com.tfg.mentoring.controller;

import java.util.Date;
import java.util.Optional;

import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.enums.Roles;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.service.UserService;

@RestController
public class InitialController {

	@Autowired
	private UserService uservice;
	@Autowired
	private MentorizadoRepo menrepo;
	@Autowired
	private MentorRepo mrepo;
	
	@Autowired
	WebSocketMessageBrokerStats webSocketMessageBrokerStats;

	@GetMapping({ "/", "/home" })
	public ModelAndView home(@AuthenticationPrincipal UserAuth us) {

		if (us == null) {
			System.out.println("No estoy autentificado");
			return new ModelAndView("home");
		}
		if (us.getUsername() == null) {
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje",
					"Se ha producido un error con su información de acceso, por favor, si recibe este mensaje, "
							+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
			model.addObject("hora", new Date());
			return model;
		}
		if (us.getRol() == Roles.MENTOR) {
			try {
				Optional<Mentor> mentor = mrepo.findById(us.getUsername());
				if (mentor.isPresent()) {
					ModelAndView modelo = new ModelAndView("home_login");
					// Aqui tal vez seria interesante pasar algun dato del usuario para mostrarlo,
					// para saber con quien ha entrado
					modelo.addObject("rol", "Mentor");
					uservice.addInstitucionUtils(modelo, mentor.get().getInstitucion());
					return modelo;
				} else {
					ModelAndView model = new ModelAndView("error_page");
					model.addObject("mensaje",
							"No ha sido posible acceder a la información de su perfil, por favor, si recibe este mensaje, "
									+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
					model.addObject("hora", new Date());
					return model;
				}
			} catch (JDBCConnectionException | QueryTimeoutException e) {
				System.out.println(e.getMessage());
				ModelAndView model = new ModelAndView("error_page_loged");
				model.addObject("mensaje", "No ha sido posible acceder al repositorio de la aplicación, por favor, inténtelo más tarde");
				model.addObject("rol", "Mentor");
				model.addObject("hora", new Date());
				return model;
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(e.getLocalizedMessage());
				System.out.println(e.toString());
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje", "Se ha producido un error inesperado en el servidor, del tipo: "
						+ e.getClass().getCanonicalName() + ", por favor, si recibe este mensaje, "
						+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
				model.addObject("hora", new Date());
				return model;
			}
		} else if (us.getRol() == Roles.MENTORIZADO) {
			try {
				Optional<Mentorizado> mentorizado = menrepo.findById(us.getUsername());
				if (mentorizado.isPresent()) {
					ModelAndView modelo = new ModelAndView("home_login");
					modelo.addObject("rol", "Mentorizado");
					uservice.addInstitucionUtils(modelo, mentorizado.get().getInstitucion());
					return modelo;
				} else {
					ModelAndView model = new ModelAndView("error_page");
					model.addObject("mensaje",
							"No ha sido posible acceder a la información de su perfil, por favor, si recibe este mensaje, "
									+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
					model.addObject("hora", new Date());
					return model;
				}
			} catch (JDBCConnectionException | QueryTimeoutException e) {
				System.out.println(e.getMessage());
				ModelAndView model = new ModelAndView("error_page_loged");
				model.addObject("mensaje", "No ha sido posible acceder al repositorio de la aplicación, por favor, inténtelo más tarde");
				model.addObject("rol", "Mentorizado");
				model.addObject("hora", new Date());
				return model;
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(e.getLocalizedMessage());
				System.out.println(e.toString());
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje", "Se ha producido un error inesperado en el servidor, del tipo: "
						+ e.getClass().getCanonicalName() + ", por favor, si recibe este mensaje, "
						+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
				model.addObject("hora", new Date());
				return model;
			}
		} else {
			System.out.println("Otro rol");
			ModelAndView model = new ModelAndView("error_page");
			model.addObject("mensaje", "No estas autorizado a acceder a esta página con tu rol actual.");
			model.addObject("hora", new Date());
			return model;
		}

	}

	@GetMapping("/login")
	public ModelAndView showLoginForm(@AuthenticationPrincipal UserAuth us) {

		if (us == null) {
			return new ModelAndView("login");
		}
		
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

	/*@GetMapping("/perror")
	public ModelAndView perror() {
		ModelAndView model = new ModelAndView("error_page");
		model.addObject("mensaje", "No ha sido posible acceder a la información de su perfil, por favor, si recibe este mensaje, "
				+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
		model.addObject("hora", new Date());
		return model;
	}*/

	@GetMapping("/perrorl")
	public ModelAndView perrorl() {

		ModelAndView model = new ModelAndView("error_page_loged");
		model.addObject("mensaje", "kaka kolas, kakota");
		model.addObject("hora", new Date());
		return model;
	}
	
	@GetMapping("/cierre")
	public void cierre() {
		System.out.println("El usuario a abandonado la pagina");
		
	}
	
	@GetMapping("/info")
	public void info() {
		System.out.println("Session Stats info " +webSocketMessageBrokerStats.getWebSocketSessionStatsInfo());
		
	}
	
	
	//Esto me lo guardo porque devuleve un map y es muy interesante
	/*@GetMapping("/configuracion")
	public Map<String, Object> configuracion(@AuthenticationPrincipal UserAuth us, HttpServletRequest request) {

		Map<String, Object> configuracion = new HashMap<String, Object>();
		configuracion.put("prebindUrl", xmpp.getPrebindUrl());
		configuracion.put("bindUrl", "http://" + xmpp.getHost() + ":" + xmpp.getPort() + "" + xmpp.getHttpBind());
		System.out.println(us.toString());
		configuracion.put("jid", us.getJid());
		return configuracion;
	}*/


}
