package com.tfg.mentoring.controller;

import java.util.Date;

import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.service.RedirectService;

@RestController
public class InitialController {

	@Autowired
	private RedirectService rservice;

	/*@Autowired
	WebSocketMessageBrokerStats webSocketMessageBrokerStats;*/

	@GetMapping({ "/", "/home" })
	public ModelAndView home(@AuthenticationPrincipal UserAuth us) {

		if (us == null) {
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
		String rol = "";
		try {
			switch (us.getRol()) {
			case MENTOR:
				rol = "Mentor";
				return rservice.devolverInicioMentor(us.getUsername());
				
			case MENTORIZADO:
				rol = "Mentorizado";
				return rservice.devolverInicioMentorizado(us.getUsername());

			default:
				System.out.println("Otro rol");
				ModelAndView model = new ModelAndView("error_page");
				model.addObject("mensaje", "No estas autorizado a acceder a esta página con tu rol actual.");
				model.addObject("hora", new Date());
				return model;
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

	/*@GetMapping("/info")
	public void info() {
		System.out.println("Session Stats info " + webSocketMessageBrokerStats.getWebSocketSessionStatsInfo());

	}*/

}
