package com.tfg.mentoring.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
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

import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizacion;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.Peticion;
import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.DTO.MentorizacionDTO;
import com.tfg.mentoring.model.auxiliar.DTO.PeticionDTO;
import com.tfg.mentoring.model.auxiliar.DTO.UsuarioDTO;
import com.tfg.mentoring.model.auxiliar.enums.EstadosPeticion;
import com.tfg.mentoring.model.auxiliar.enums.MotivosNotificacion;
import com.tfg.mentoring.model.auxiliar.requests.CambioFase;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizacionRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.PeticionRepo;
import com.tfg.mentoring.service.MapeadoService;
import com.tfg.mentoring.service.SalaChatServicio;
import com.tfg.mentoring.service.UserService;

@RestController
@RequestMapping("/mentor")
public class MentorController {

	@Autowired
	private UserService uservice;
	@Autowired
	private SalaChatServicio schats;
	@Autowired
	private MapeadoService mservice;

	@Autowired
	private MentorizadoRepo menrepo;
	@Autowired
	private MentorRepo mrepo;
	@Autowired
	private MentorizacionRepo mentorizacionrepo;

	@Autowired
	private PeticionRepo prepo;
	
	///////////////////////////////////////
				/*Peticiones*/
	//////////////////////////////////////
	// Obtener peticiones
	@GetMapping("/peticiones")
	public ResponseEntity<List<PeticionDTO>> getPeticiones(@AuthenticationPrincipal UserAuth us) {
		try {
			List<Peticion> peticiones = new ArrayList<Peticion>();
			List<PeticionDTO> pUser = new ArrayList<PeticionDTO>();
			peticiones = prepo.obtenerPeticiones(us.getUsername());
			if (peticiones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Peticion p : peticiones) {
				UsuarioDTO up = mservice.getMentorizadoInfo(p.getMentorizado());
				pUser.add(new PeticionDTO(p, up));
				if (p.getEstado() == EstadosPeticion.ENVIADA) {
					p.setEstado(EstadosPeticion.RECIBIDA);
				}
			}
			prepo.saveAll(peticiones);

			// Notificaciones.removeIf(n -> (n.getFechaeliminacion() != null));
			return new ResponseEntity<>(pUser, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Obtener peticiones nuevas
	@GetMapping("/peticiones/actualizar")
	public ResponseEntity<List<PeticionDTO>> getNewPeticiones(@AuthenticationPrincipal UserAuth us) {
		try {
			List<Peticion> peticiones = new ArrayList<Peticion>();
			List<PeticionDTO> pUser = new ArrayList<PeticionDTO>();
			peticiones = prepo.getNews(us.getUsername());
			if (peticiones.isEmpty()) {
				System.out.println("Sin nuevas peticiones");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Peticion p : peticiones) {
				// System.out.println(n.getEstado().toString());
				UsuarioDTO up = mservice.getMentorizadoInfo(p.getMentorizado());
				pUser.add(new PeticionDTO(p, up));
				if (p.getEstado() == EstadosPeticion.ENVIADA) {
					p.setEstado(EstadosPeticion.RECIBIDA);
				}
			}
			prepo.saveAll(peticiones);
			return new ResponseEntity<>(pUser, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	@PostMapping("/peticiones/aceptar")
	public ResponseEntity<MensajeError> aceptarPeticion(@AuthenticationPrincipal UserAuth us,
			@RequestBody String mentorizado) {
		if(mentorizado == null || us.getUsername() == null) {
			System.out.println("El mentorizado estaba null");
			//Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(new MensajeError("Fallo en la peticion","Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
					"la del mentorizado, por favor,  si recibe este mensaje,"+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
		}
		try {
			// Aqui, como en las demas, deberia hacerse una comprobacion de que el usuario
			// esta enable
			Optional<Mentor> m = mrepo.findById(us.getUsername());//Asumimos que el username nunca va a ser null
			Optional<Mentorizado> men = menrepo.findById(mentorizado);
			if (m.isPresent() && men.isPresent()) {// Asuminos no hay problemas con el username del usuario, y que no
													// debe haber ya una mentorizacion
					//Esto se solucionaria con una funcion de limpieza al inciar el servidor y ¿que el servidor se reiniciase cada dia?
					Mentorizacion mentorizacion = new Mentorizacion(m.get(), men.get());
					Mentorizacion resultado = mentorizacionrepo.save(mentorizacion);//Mejor cambiar este patra que solo inserte si el usuario es
					// List<Peticion> peticiones = prepo.comprobarPeticion(us.getUsername(), mentorizado);
					prepo.aceptarPeticion(us.getUsername(), mentorizado);
					if(resultado.getMentorizado().getUsuario().isEnabled()) {
						schats.abrirChat(resultado.getMentor(), resultado.getMentorizado());
						uservice.enviarNotificacion(men.get().getUsuario(), "Peticion aceptada",
								"El usuario " + m.get().getNombre() + " te ha aceptado una petición de mentorizacion.", MotivosNotificacion.ACEPTAR);
						return new ResponseEntity<>(null, HttpStatus.OK);
					}
					else {
						resultado.setFin(new Date());
						mentorizacionrepo.save(resultado);
						return new ResponseEntity<>(new MensajeError("Mentorizado eliminado","El mentorizado ya no se encuentra disponible, no se creará mentorización."), HttpStatus.GONE);
						//Aqui un codigo de error para indicar a angular el suceso
					}
					
			} else {
				System.out.println("No hay usuarios");
				//Esto no deberia pasar nunca si se llama desde la aplicacion
				return new ResponseEntity<>(new MensajeError("Fallo en la peticion","Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
						"la del mentorizado, por favor,  si recibe este mensaje,"+
						"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio","Se ha producido un problema al intentar actualizar el repositorio, "+
					"por favor, vuelva a intentarlo más tarde."), HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {//Aqui concretar mas las excepciones, esta es de prueba
			//Acceso a la base de datos
			//Fallo al enviar notificacion? (correo)
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno","Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/peticiones/rechazar")
	public ResponseEntity<MensajeError> rechazarPeticion(@AuthenticationPrincipal UserAuth us,
			@RequestBody String mentorizado) {
		if(mentorizado ==null || us.getUsername() == null) {
			System.out.println("El mentorizado estaba null");
			//Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(new MensajeError("Fallo en la peticion","Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
					"la del mentorizado, por favor,  si recibe este mensaje,"+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
		}
		try {
			Optional<Mentorizado> men = menrepo.findById(mentorizado);
			Optional<Mentor> m = mrepo.findById(us.getUsername());
			if (m.isPresent() && men.isPresent()) {// Asuminos no hay problemas con el username del usuario, y que no
													// debe haber ya una mentorizacion
				// List<Peticion> peticiones = prepo.comprobarPeticion(us.getUsername(),
				// mentorizado);
				prepo.rechazarPeticion(us.getUsername(), mentorizado);
				uservice.enviarNotificacion(men.get().getUsuario(), "Peticion rechazada",
						"El usuario " + m.get().getNombre() + " te ha rechazado una petición de mentorizacion.", MotivosNotificacion.RECHAZAR);
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				System.out.println("No hay usuarios");
				return new ResponseEntity<>(new MensajeError("Fallo en la peticion","Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
						"la del mentorizado, por favor,  si recibe este mensaje,"+
						"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio","Se ha producido un problema al intentar actualizar el repositorio, "+
					"por favor, vuelva a intentarlo más tarde."), HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno","Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	///////////////////////////////////////
			 /*Mentorizaciones*/
	//////////////////////////////////////
	
	@GetMapping("/mentorizaciones")
	public ResponseEntity<List<MentorizacionDTO>> getMentorizaciones(@AuthenticationPrincipal UserAuth us) {
		try {
			List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
			List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
			mentorizaciones = mentorizacionrepo.obtenerMentorizacionesMentor(us.getUsername());
			if (mentorizaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Mentorizacion m : mentorizaciones) {
				// System.out.println(n.getEstado().toString());
				mUser.add(new MentorizacionDTO(m, mservice.getMentorizadoMentorizacion(m.getMentorizado()), m.getMentorizado().getCorreo(), false));
			}

			return new ResponseEntity<>(mUser, HttpStatus.OK);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Obtener peticiones nuevas
	@GetMapping("/mentorizaciones/actualizar/{date}")
		public ResponseEntity<List<MentorizacionDTO>> getNewMentorizaciones(@AuthenticationPrincipal UserAuth us, @PathVariable("date") Long date) {
			if(date != null) {//Tambien ver si de primeras ya se puede ver si es parseable
				try {
					Timestamp fecha = new Timestamp(date);
					List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
					List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
					mentorizaciones = mentorizacionrepo.getNuevasMentor(us.getUsername(), fecha);
					if (mentorizaciones.isEmpty()) {
						return new ResponseEntity<>(HttpStatus.NO_CONTENT);
					}
					for(Mentorizacion m : mentorizaciones) {
						if(m.getFin() == null) {
							mUser.add(new MentorizacionDTO(m, mservice.getMentorizadoMentorizacion(m.getMentorizado()), m.getMentorizado().getCorreo(), false));
						}
						else {
							mUser.add(new MentorizacionDTO(m, null, m.getMentorizado().getCorreo(), false));
						}
					}
					return new ResponseEntity<>(mUser, HttpStatus.OK);
				 } catch (JDBCConnectionException | QueryTimeoutException e) {
					 System.out.println(e.getMessage());
					 return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
				 } catch (Exception e) {
					 System.out.println(e.getMessage());
					 return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				 }
			}
			else {
				System.out.println("Date esta mal");
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}
		}

	
	@PostMapping("/mentorizaciones/cerrar")
	public ResponseEntity<MensajeError> cerrarMentorizacion(@AuthenticationPrincipal UserAuth us,
			@RequestBody String mentorizado) {
		if(mentorizado == null || us.getUsername() == null) {
			System.out.println("El mentorizado o el username estaba null");
			//Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(new MensajeError("Fallo en la peticion","Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
					"la del mentorizado, por favor,  si recibe este mensaje,"+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
		}
		try {
			Optional<Mentor> m = mrepo.findById(us.getUsername());
			Optional<Mentorizado> men = menrepo.findById(mentorizado);
			if (m.isPresent() && men.isPresent()) {
				mentorizacionrepo.cerrarMentorizacion(us.getUsername(), mentorizado, new Timestamp(System.currentTimeMillis()));
				schats.cerrarChat(m.get().getCorreo(), mentorizado, true);
				uservice.enviarNotificacion(men.get().getUsuario(), "Mentorizacion cerrada", "El usuario "+m.get().getNombre()+
						" ha cerrado una mentorización que tenía abierta contigo. Puedes proceder a puntuarla y comentarla en el apartado de puntuar.",
						MotivosNotificacion.CIERRE);
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {//Esto, en teoria, no deberia pasar nunca
				System.out.println("Fallo al acceder a los usuarios");
				return new ResponseEntity<>(new MensajeError("Fallo en la peticion","Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
						"la del mentorizado, por favor,  si recibe este mensaje,"+
						"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio","Se ha producido un problema al intentar actualizar el repositorio, "+
					"por favor, vuelva a intentarlo más tarde."), HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { //Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno","Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/mentorizaciones/cambiarfase")
	public ResponseEntity<MensajeError> cambiarFase(@AuthenticationPrincipal UserAuth us,
			@RequestBody CambioFase fase) {
		if(fase == null || us.getUsername() == null || fase.getCorreo() == null || fase.getFase() == null) {
			System.out.println("Algo estaba a null");
			//Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(new MensajeError("Fallo en la peticion","Se ha producido un problema al intentar realizar la petición o al acceder a su cuenta, "+
					"por favor, si recibe este mensaje, pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. "
					+ "Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
		}
		try {
			System.out.println(fase.toString());
			Optional<Mentor> m = mrepo.findById(us.getUsername());
			Optional<Mentorizado> men = menrepo.findById(fase.getCorreo());
			if (m.isPresent() && men.isPresent()) {
				mentorizacionrepo.cambiarFase(us.getUsername(), fase.getCorreo(), fase.getFase().getValue());
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				System.out.println("Fallo al acceder a los usuarios");
				return new ResponseEntity<>(new MensajeError("Fallo en la peticion","Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
						"la del mentorizado, por favor,  si recibe este mensaje,"+
						"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio","Se ha producido un problema al intentar actualizar el repositorio, "+
					"por favor, vuelva a intentarlo más tarde."), HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { //Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno","Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
