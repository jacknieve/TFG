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
import com.tfg.mentoring.model.auxiliar.DTO.MentorDTO;
import com.tfg.mentoring.model.auxiliar.DTO.MentorizacionDTO;
import com.tfg.mentoring.model.auxiliar.DTO.UsuarioDTO;
import com.tfg.mentoring.model.auxiliar.enums.EstadosPeticion;
import com.tfg.mentoring.model.auxiliar.enums.MotivosNotificacion;
import com.tfg.mentoring.model.auxiliar.requests.CamposBusqueda;
import com.tfg.mentoring.model.auxiliar.requests.EnvioPeticion;
import com.tfg.mentoring.model.auxiliar.requests.MentorizacionCerrar;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizacionRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.PeticionRepo;
import com.tfg.mentoring.service.SalaChatServicio;
import com.tfg.mentoring.service.UserService;

@RestController
@RequestMapping("/mentorizado")
public class MentorizadoController {

	@Autowired
	private MentorizadoRepo menrepo;
	@Autowired
	private MentorRepo mrepo;
	@Autowired
	private MentorizacionRepo mentorizacionrepo;
	@Autowired
	private PeticionRepo prepo;

	@Autowired
	private UserService uservice;
	@Autowired
	private SalaChatServicio schats;
	
	// A esto hay que meterle "seguridad", es decir, try y catch para las excepciones y demas
	@PostMapping("/busqueda") public ResponseEntity<List<MentorDTO>> buscar(@RequestBody CamposBusqueda campos) { 
		if(campos == null || campos.getHoras() < 4 || campos.getHoras() >80) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		 	List<Mentor> mentores = new ArrayList<Mentor>();
		 	if(campos.getArea() == null || campos.getArea().equals("sin")) campos.setArea(null); 
		 	if(campos.getInstitucion() == null || campos.getInstitucion().equals("sin")) campos.setInstitucion(null); 
		 	try { 
		 		mentores = mrepo.buscarPrototipo(campos.getInstitucion(), campos.getHoras(), campos.getArea());
		 		List<MentorDTO> resultado = uservice.getMentorBusqueda(mentores);
		 		if(resultado.isEmpty()) {
		 			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		 		}
		 		return new ResponseEntity<>(resultado, HttpStatus.OK); 
		 	} catch (JDBCConnectionException | QueryTimeoutException e) {
				System.out.println(e.getMessage());
				return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	
	
	@PostMapping("/obtenermentor")
	public ResponseEntity<UsuarioDTO> getPerfilBusqueda(@AuthenticationPrincipal UserAuth us,
			@RequestBody String mentor) {
		if(mentor == null || us.getUsername() == null) {
			System.out.println("El mentorizado es null o la entrada es mala");
			//Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		try {
			//Optional<Mentor> m = mrepo.findById(mentor);
			Optional<Mentor> m = mrepo.findByUsuarioUsernameAndUsuarioEnable(mentor, true);
			if (m.isPresent()) {
				UsuarioDTO up = uservice.getPerfilMentor(m.get());
				return new ResponseEntity<>(up, HttpStatus.OK);
			} else {
				System.out.println("No hay mentor");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// Habria que poner en todas una comprobacion previa para que el usuario no este
	// a null?
	// Tambien, en los post, habria que asegurarse que la clase que llega es
	// correcta? o eso ya lo comprueba ya Spring, creo que si
	@PostMapping("/enviarsolicitud")
	public ResponseEntity<MensajeError> enviarSolicitud(@AuthenticationPrincipal UserAuth us,
			@RequestBody EnvioPeticion peticion) {
		if(peticion == null || us.getUsername() == null || peticion.getMentor() == null) {
			System.out.println("El mentorizado es null o la entrada es mala");
			//Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
					"la del mentor, por favor,  si recibe este mensaje,"+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
		}
		try {
			Optional<Mentor> m = mrepo.findById(peticion.getMentor());
			Optional<Mentorizado> men = menrepo.findById(us.getUsername());
			if (m.isPresent() && men.isPresent()) {
				// Primero comprobar si ya existe una mentorizacion y si ya existe una peticion
				List<Mentorizacion> mentorizacion = mentorizacionrepo.comprobarSiHayMentorizacion(peticion.getMentor(),
						us.getUsername());
				if (mentorizacion.isEmpty()) {// Comprobamos que no haya ya una mentorizacion abierta entre ambas partes
					List<Peticion> peticiones = prepo.comprobarPeticion(peticion.getMentor(), us.getUsername());
					if (peticiones.isEmpty()) {// Comprobamos si no existe ya una peticion pendiente
						Peticion p = new Peticion(m.get(), men.get(), peticion.getMotivo());
						//String nombre =prepo.save(p).getMentorizado().getNombre();
						Peticion resultado = prepo.save(p);
						if(resultado.getMentor().getUsuario().isEnabled()) {
						// Solo vamos a enviar notificacion si es la primera vez
							uservice.enviarNotificacion(m.get().getUsuario(), "Nueva peticion", "El usuario "
								+ men.get().getNombre() + " te ha enviado una petición de mentorizacion.", MotivosNotificacion.PETICION);
							return new ResponseEntity<>(null, HttpStatus.OK);
						}
						else {
							System.out.println("No se envia notificacion porque el usuario esta disabled");
							return new ResponseEntity<>(null, HttpStatus.OK);//Aqui un codigo de error para indicar que ha sido imposible enviar la peticion
						}
					} else {// Si ya la hay, la actualizamos
						System.out.println("Se va a actualizar la peticion");
						Peticion p = peticiones.get(0);// En teoria, solo podria haber como mucho una peticion pendiente
														// entre ambas partes
						System.out.println(p.toString());
						p.setEstado(EstadosPeticion.ENVIADA);
						p.setMotivo(peticion.getMotivo());
						System.out.println(p.toString());
						prepo.save(p);
						return new ResponseEntity<>(null, HttpStatus.OK);
					}
				} else {
					System.out.println("Ya hay una mentorizacion abierta");
					System.out.println(mentorizacion.toString());
					return new ResponseEntity<>(new MensajeError("Ya has establecido una relación de mentorización con ese mentor "),HttpStatus.CONFLICT);
				}
			} else {
				System.out.println("Fallo al acceder a los usuarios");
				return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
						"la del mentor, por favor,  si recibe este mensaje,"+
						"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar actualizar el repositorio, "+
					"por favor, vuelva a intentarlo más tarde."), HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { //Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/mentorizaciones")
	public ResponseEntity<List<MentorizacionDTO>> getMentorizaciones(@AuthenticationPrincipal UserAuth us) {
		try {
			List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
			List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
			mentorizaciones = mentorizacionrepo.obtenerMentorizacionesMentorizado(us.getUsername());
			if (mentorizaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Mentorizacion m : mentorizaciones) {
				// System.out.println(n.getEstado().toString());
				mUser.add(new MentorizacionDTO(m, uservice.getPerfilMentor(m.getMentor()), m.getMentor().getCorreo()));
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

	// Obtener mentorizaciones nuevas
	@GetMapping("/mentorizaciones/actualizar/{date}")
	public ResponseEntity<List<MentorizacionDTO>> getNewMentorizaciones(@AuthenticationPrincipal UserAuth us,
			@PathVariable("date") Long date) {
		if (date != null) {// Tambien ver si de primeras ya se puede ver si es parseable
			try {
				Timestamp fecha = new Timestamp(date);
				System.out.println(fecha);
				List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
				List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
				mentorizaciones = mentorizacionrepo.getNuevasMentorizado(us.getUsername(), fecha);
				System.out.println(mentorizaciones.toString());
				if (mentorizaciones.isEmpty()) {
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
				for (Mentorizacion m : mentorizaciones) {
					if (m.getFin() == null) {
						mUser.add(new MentorizacionDTO(m, uservice.getPerfilMentor(m.getMentor()), m.getMentor().getCorreo()));
					} else {
						mUser.add(new MentorizacionDTO(m, null, m.getMentor().getCorreo()));
					}

				}
				// System.out.println(mUser.toString());
				return new ResponseEntity<>(mUser, HttpStatus.OK);
			} catch (JDBCConnectionException | QueryTimeoutException e) {
				 System.out.println(e.getMessage());
				 return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
			 } catch (Exception e) {
				 System.out.println(e.getMessage());
				 return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			 }
		} else {
			System.out.println("Date esta mal");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	
	@PostMapping("/mentorizaciones/cerrar")
	public ResponseEntity<MensajeError> cerrarMentorizacion(@AuthenticationPrincipal UserAuth us,
			@RequestBody MentorizacionCerrar mentorizacion) {
		if(mentorizacion == null || us.getUsername() == null || mentorizacion.getMentor() == null || mentorizacion.getPuntuacion() < -1
				|| mentorizacion.getPuntuacion() > 10) {
			System.out.println("El mentorizado es null o la entrada es mala");
			//Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
					"la del mentor, por favor,  si recibe este mensaje,"+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
		}
		try {
			Optional<Mentor> m = mrepo.findById(mentorizacion.getMentor());
			Optional<Mentorizado> men = menrepo.findById(us.getUsername());
			if (m.isPresent() && men.isPresent()) {
				// Primero comprobar si ya existe una mentorizacion y si ya existe una peticion
				mentorizacionrepo.cerrarPuntuarMentorizacion(mentorizacion.getPuntuacion(), mentorizacion.getComentario(),
						mentorizacion.getMentor(), us.getUsername());
				schats.cerrarChat(m.get().getCorreo(), men.get().getCorreo(), false);
				uservice.enviarNotificacion(m.get().getUsuario(), "Mentorizacion cerrada", "El usuario "+men.get().getNombre()+
						" ha cerrado una mentorización que tenía abierta contigo.", MotivosNotificacion.CIERRE);
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				System.out.println("Fallo al acceder a los usuarios");
				return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
						"la del mentor, por favor,  si recibe este mensaje,"+
						"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar actualizar el repositorio, "+
					"por favor, vuelva a intentarlo más tarde."), HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { //Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/mentorizaciones/porpuntuar")
	public ResponseEntity<List<MentorizacionDTO>> getMentorizacionesPorPuntuar(@AuthenticationPrincipal UserAuth us) {
		try {
			List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
			List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
			mentorizaciones = mentorizacionrepo.obtenerMentorizacionesPorPuntuar(us.getUsername());
			if (mentorizaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Mentorizacion m : mentorizaciones) {
				mUser.add(new MentorizacionDTO(m, uservice.getPerfilMentor(m.getMentor()), m.getMentor().getCorreo()));
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
	
	@GetMapping("/mentorizaciones/porpuntuar/{date}")
	public ResponseEntity<List<MentorizacionDTO>> getNewMentorizacionesPorPuntuar(@AuthenticationPrincipal UserAuth us,
			@PathVariable("date") Long date) {
		if (date != null) {// Tambien ver si de primeras ya se puede ver si es parseable
			try {
				Timestamp fecha = new Timestamp(date);
				List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
				List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
				mentorizaciones = mentorizacionrepo.obtenerMentorizacionesPorPuntuarNuevas(us.getUsername(), fecha);
				if (mentorizaciones.isEmpty()) {
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
				for (Mentorizacion m : mentorizaciones) {
					mUser.add(new MentorizacionDTO(m, uservice.getPerfilMentor(m.getMentor()), m.getMentor().getCorreo()));

				}
				// System.out.println(mUser.toString());
				return new ResponseEntity<>(mUser, HttpStatus.OK);
			} catch (JDBCConnectionException | QueryTimeoutException e) {
				 System.out.println(e.getMessage());
				 return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
			 } catch (Exception e) {
				 System.out.println(e.getMessage());
				 return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			 }
		} else {
			System.out.println("Date esta mal");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}
	//Comprobar mentorizacion
	@PostMapping("/mentorizaciones/puntuar")
	public ResponseEntity<MensajeError> puntuarMentorizacion(@AuthenticationPrincipal UserAuth us,
			@RequestBody MentorizacionCerrar mentorizacion) {
		if(mentorizacion == null || us.getUsername() == null || mentorizacion.getMentor() == null || mentorizacion.getPuntuacion() < -1
				|| mentorizacion.getPuntuacion() > 10 || mentorizacion.getFechafin() == null) {
			System.out.println("El mentorizado es null o la entrada es mala");
			//Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
					"la del mentor, por favor,  si recibe este mensaje,"+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
		}
		try {
			Optional<Mentor> m = mrepo.findById(mentorizacion.getMentor());
			Optional<Mentorizado> men = menrepo.findById(us.getUsername());
			if (m.isPresent() && men.isPresent()) {
				// Primero comprobar si ya existe una mentorizacion y si ya existe una peticion
				mentorizacionrepo.puntuarMentorizacion(mentorizacion.getPuntuacion(), mentorizacion.getComentario(),
						mentorizacion.getMentor(), us.getUsername(), new Timestamp(mentorizacion.getFechafin()));
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				System.out.println("Fallo al acceder a los usuarios");
				return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
						"la del mentor, por favor,  si recibe este mensaje,"+
						"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()),HttpStatus.BAD_REQUEST);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar actualizar el repositorio, "+
					"por favor, vuelva a intentarlo más tarde."), HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { //Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "+new Date()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	 
	
}
