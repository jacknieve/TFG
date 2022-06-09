package com.tfg.mentoring.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.DTO.MentorizacionDTO;
import com.tfg.mentoring.model.auxiliar.DTO.PeticionDTO;
import com.tfg.mentoring.model.auxiliar.requests.CambioFase;
import com.tfg.mentoring.service.UserService;

@RestController
@RequestMapping("/mentor")
public class MentorController {

	@Autowired
	private UserService uservice;

	///////////////////////////////////////
	/* Peticiones */
	//////////////////////////////////////
	// Obtener peticiones
	@GetMapping("/peticiones")
	public ResponseEntity<List<PeticionDTO>> getPeticiones(@AuthenticationPrincipal UserAuth us) {
		try {
			List<PeticionDTO> peticiones = uservice.obtenerPeticiones(us.getUsername());
			if (peticiones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(peticiones, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Obtener peticiones nuevas
	@GetMapping("/peticiones/actualizar")
	public ResponseEntity<List<PeticionDTO>> getNewPeticiones(@AuthenticationPrincipal UserAuth us) {
		try {
			List<PeticionDTO> peticiones = uservice.obtenerPeticionesNuevas(us.getUsername());
			if (peticiones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(peticiones, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/peticiones/aceptar")
	public ResponseEntity<MensajeError> aceptarPeticion(@AuthenticationPrincipal UserAuth us,
			@RequestBody String mentorizado) {
		try {
			Optional<MensajeError> error = uservice.aceptarPeticion(us.getUsername(), mentorizado);
			if(error.isPresent()) {
				return new ResponseEntity<>(error.get(), HttpStatus.NOT_FOUND);
			}
			else {
				return new ResponseEntity<>(null, HttpStatus.OK);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/peticiones/rechazar")
	public ResponseEntity<MensajeError> rechazarPeticion(@AuthenticationPrincipal UserAuth us,
			@RequestBody String mentorizado) {
		try {
			Optional<MensajeError> error = uservice.rechazarPeticion(us.getUsername(), mentorizado);
			if(error.isPresent()) {
				return new ResponseEntity<>(error.get(), HttpStatus.NOT_FOUND);
			}
			else {
				return new ResponseEntity<>(null, HttpStatus.OK);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	///////////////////////////////////////
	/* Mentorizaciones */
	//////////////////////////////////////

	@GetMapping("/mentorizaciones")
	public ResponseEntity<List<MentorizacionDTO>> getMentorizaciones(@AuthenticationPrincipal UserAuth us) {
		try {
			List<MentorizacionDTO> mentorizaciones = uservice.obtenerMentorizacionesMentor(us.getUsername());
			if (mentorizaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(mentorizaciones, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Obtener peticiones nuevas
	@GetMapping("/mentorizaciones/actualizar/{date}")
	public ResponseEntity<List<MentorizacionDTO>> getNewMentorizaciones(@AuthenticationPrincipal UserAuth us,
			@PathVariable("date") Long date) {
		try {
			Timestamp fecha = new Timestamp(date);
			List<MentorizacionDTO> mentorizaciones = uservice.obtenerMentorizacionesMentorNuevas(us.getUsername(), fecha);
			if (mentorizaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(mentorizaciones, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/mentorizaciones/cerrar")
	public ResponseEntity<MensajeError> cerrarMentorizacion(@AuthenticationPrincipal UserAuth us,
			@RequestBody String mentorizado) {
		try {
			Optional<MensajeError> error = uservice.cerrarMentorizacionesMentor(us.getUsername(), mentorizado);
			if(error.isPresent()) {
				return new ResponseEntity<>(error.get(), HttpStatus.NOT_FOUND);
			}
			else {
				return new ResponseEntity<>(null, HttpStatus.OK);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { 
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/mentorizaciones/cambiarfase")
	public ResponseEntity<MensajeError> cambiarFase(@AuthenticationPrincipal UserAuth us,
			@RequestBody CambioFase fase) {
		if (fase.getCorreo() == null || fase.getFase() == null) {
			System.out.println("Algo estaba a null");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		try {
			Optional<MensajeError> error = uservice.cambiarFase(us.getUsername(), fase.getCorreo(), fase.getFase().getValue());
			if(error.isPresent()) {
				return new ResponseEntity<>(error.get(), HttpStatus.NOT_FOUND);
			}
			else {
				return new ResponseEntity<>(null, HttpStatus.OK);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { 
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
