package com.tfg.mentoring.controller;

import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

//https://www.baeldung.com/spring-boot-custom-error-page

@Controller
public class ControladorError implements ErrorController{
	
	@RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		ModelAndView model = new ModelAndView("error_page");
		model.addObject("mensaje", "Se ha producido un error con código de error: "+status+".");
		model.addObject("hora", new Date());
		return model;
    }
	

}
