package com.tfg.mentoring.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tfg.mentoring.exceptions.ExcepcionFileNotFound;
import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.UserAuth;

@ControllerAdvice
public class ManejadorExcepciones extends ResponseEntityExceptionHandler{

	
	 // Catch max file size Exception.
    @ExceptionHandler(MultipartException.class)
    @ResponseBody
    public ResponseEntity<MensajeError> manejadorExcepcionMultipart(HttpServletRequest request, Throwable ex) {
        return new ResponseEntity<>(new MensajeError("Fichero demasiado grande",
				"Se ha producido un error al cargar el fichero, probablemente se deba a que supera el tamaño maximo permitido de 15MB."),
				HttpStatus.BAD_REQUEST);
    }
	
	 @ExceptionHandler(ExcepcionFileNotFound.class)
	    @ResponseBody
	    public ModelAndView manejadorExcepcionRecursos(HttpServletRequest request, Throwable ex, @AuthenticationPrincipal UserAuth u) {
		 ModelAndView model = new ModelAndView("error_page_loged");
			model.addObject("mensaje", "El fichero requerido no existe o ya no se encuentra disponible");
			switch (u.getRol()) {
			case MENTOR:
				model.addObject("rol", "Mentor");
				break;
			case MENTORIZADO:
				model.addObject("rol", "Mentorizado");
				break;
			default:
				model.addObject("rol", "Otro");
			}
			model.addObject("hora", new Date());
			return model;
	    }
 
    // Catch Other Exception
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ModelAndView manejadorExcepcionDefecto(HttpServletRequest request, Throwable ex) {
    	
    	HttpStatus status = this.getStatus(request);
    	ModelAndView model = new ModelAndView("error_page");
		model.addObject("mensaje", "Se ha producido un error inesperado en el servidor, del tipo: "
				+ status + ", por favor, si recibe este mensaje, "
				+ "pongase en contancto con nosotros e indíquenos el contexto en el que se produjo este error.");
		model.addObject("hora", new Date());
		return model;
 
        
    }
    
    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }
 
}
