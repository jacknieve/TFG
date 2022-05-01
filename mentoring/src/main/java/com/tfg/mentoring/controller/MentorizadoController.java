package com.tfg.mentoring.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
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
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.EnvioPeticion;
import com.tfg.mentoring.model.auxiliar.EstadosPeticion;
import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.MentorBusqueda;
import com.tfg.mentoring.model.auxiliar.MentorizacionCerrar;
import com.tfg.mentoring.model.auxiliar.MentorizacionUser;
import com.tfg.mentoring.model.auxiliar.UsuarioPerfil;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizacionRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.PeticionRepo;
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
	
	// A esto hay que meterle "seguridad", es decir, try y catch para las excepciones y demas
	@GetMapping("/busqueda/{area}/{institucion}/{horas}") public ResponseEntity<List<MentorBusqueda>> buscar(@PathVariable("area") String area,
		 	@PathVariable("institucion") String institucion, @PathVariable("horas") float horas) { 
		 	List<Mentor> mentores = new ArrayList<Mentor>();
		 	if(area == null || area.equals("sin")) area=null; 
		 	if(institucion == null || institucion.equals("sin")) institucion=null; 
		 	try { 
		 		mentores = mrepo.buscarPrototipo(institucion, horas, area);
		 		List<MentorBusqueda> resultado = uservice.getMentorBusqueda(mentores);
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
	public ResponseEntity<UsuarioPerfil> getPerfilBusqueda(@AuthenticationPrincipal Usuario us,
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
				UsuarioPerfil up = uservice.getPerfilMentor(m.get());
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
	public ResponseEntity<MensajeError> enviarSolicitud(@AuthenticationPrincipal Usuario us,
			@RequestBody EnvioPeticion peticion) {
		if(peticion == null || us.getUsername() == null || peticion.getMentor() == null) {
			System.out.println("El mentorizado es null o la entrada es mala");
			//Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
					"la del mentor, por favor,  si recibe este mensaje,"+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "+
					"intente ser lo más preciso posible al indicar la hora en la que ocurrió."),HttpStatus.BAD_REQUEST);
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
								+ men.get().getNombre() + " te ha enviado una petición de mentorizacion.");
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
						"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "+
						"intente ser lo más preciso posible al indicar la hora en la que ocurrió."),HttpStatus.BAD_REQUEST);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar actualizar el repositorio, "+
					"por favor, vuelva a intentarlo más tarde."), HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { //Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "+
					"intente ser lo más preciso posible al indicar la hora en la que ocurrió."), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/mentorizaciones")
	public ResponseEntity<List<MentorizacionUser>> getMentorizaciones(@AuthenticationPrincipal Usuario us) {
		try {
			List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
			List<MentorizacionUser> mUser = new ArrayList<MentorizacionUser>();
			mentorizaciones = mentorizacionrepo.obtenerMentorizacionesMentorizado(us.getUsername());
			if (mentorizaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Mentorizacion m : mentorizaciones) {
				// System.out.println(n.getEstado().toString());
				mUser.add(new MentorizacionUser(m, uservice.getPerfilMentor(m.getMentor()), m.getMentor().getCorreo()));
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
	public ResponseEntity<List<MentorizacionUser>> getNewMentorizaciones(@AuthenticationPrincipal Usuario us,
			@PathVariable("date") Long date) {
		if (date != null) {// Tambien ver si de primeras ya se puede ver si es parseable
			try {
				Timestamp fecha = new Timestamp(date);
				List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
				List<MentorizacionUser> mUser = new ArrayList<MentorizacionUser>();
				mentorizaciones = mentorizacionrepo.getNuevasMentorizado(us.getUsername(), fecha);
				if (mentorizaciones.isEmpty()) {
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
				for (Mentorizacion m : mentorizaciones) {
					if (m.getFin() == null) {
						mUser.add(new MentorizacionUser(m, uservice.getPerfilMentor(m.getMentor()), m.getMentor().getCorreo()));
					} else {
						mUser.add(new MentorizacionUser(m, null, m.getMentor().getCorreo()));
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
	public ResponseEntity<MensajeError> cerrarMentorizacion(@AuthenticationPrincipal Usuario us,
			@RequestBody MentorizacionCerrar mentorizacion) {
		if(mentorizacion == null || us.getUsername() == null || mentorizacion.getMentor() == null || mentorizacion.getPuntuacion() < -1
				|| mentorizacion.getPuntuacion() > 10) {
			System.out.println("El mentorizado es null o la entrada es mala");
			//Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
					"la del mentor, por favor,  si recibe este mensaje,"+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "+
					"intente ser lo más preciso posible al indicar la hora en la que ocurrió."),HttpStatus.BAD_REQUEST);
		}
		try {
			Optional<Mentor> m = mrepo.findById(mentorizacion.getMentor());
			Optional<Mentorizado> men = menrepo.findById(us.getUsername());
			if (m.isPresent() && men.isPresent()) {
				// Primero comprobar si ya existe una mentorizacion y si ya existe una peticion
				mentorizacionrepo.cerrarPuntuarMentorizacion(mentorizacion.getPuntuacion(), mentorizacion.getComentario(),
						mentorizacion.getMentor(), us.getUsername());
				uservice.enviarNotificacion(m.get().getUsuario(), "Mentorizacion cerrada", "El usuario "+men.get().getNombre()+
						" ha cerrado una mentorización que tenía abierta contigo.");
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				System.out.println("Fallo al acceder a los usuarios");
				return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
						"la del mentor, por favor,  si recibe este mensaje,"+
						"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "+
						"intente ser lo más preciso posible al indicar la hora en la que ocurrió."),HttpStatus.BAD_REQUEST);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar actualizar el repositorio, "+
					"por favor, vuelva a intentarlo más tarde."), HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { //Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "+
					"intente ser lo más preciso posible al indicar la hora en la que ocurrió."), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/mentorizaciones/porpuntuar")
	public ResponseEntity<List<MentorizacionUser>> getMentorizacionesPorPuntuar(@AuthenticationPrincipal Usuario us) {
		try {
			List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
			List<MentorizacionUser> mUser = new ArrayList<MentorizacionUser>();
			mentorizaciones = mentorizacionrepo.obtenerMentorizacionesPorPuntuar(us.getUsername());
			if (mentorizaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Mentorizacion m : mentorizaciones) {
				mUser.add(new MentorizacionUser(m, uservice.getPerfilMentor(m.getMentor()), m.getMentor().getCorreo()));
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
	public ResponseEntity<List<MentorizacionUser>> getNewMentorizacionesPorPuntuar(@AuthenticationPrincipal Usuario us,
			@PathVariable("date") Long date) {
		if (date != null) {// Tambien ver si de primeras ya se puede ver si es parseable
			try {
				Timestamp fecha = new Timestamp(date);
				List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
				List<MentorizacionUser> mUser = new ArrayList<MentorizacionUser>();
				mentorizaciones = mentorizacionrepo.getNuevasMentorizado(us.getUsername(), fecha);
				if (mentorizaciones.isEmpty()) {
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
				for (Mentorizacion m : mentorizaciones) {
					mUser.add(new MentorizacionUser(m, uservice.getPerfilMentor(m.getMentor()), m.getMentor().getCorreo()));

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
	public ResponseEntity<MensajeError> puntuarMentorizacion(@AuthenticationPrincipal Usuario us,
			@RequestBody MentorizacionCerrar mentorizacion) {
		if(mentorizacion == null || us.getUsername() == null || mentorizacion.getMentor() == null || mentorizacion.getPuntuacion() < -1
				|| mentorizacion.getPuntuacion() > 10 || mentorizacion.getFechafin() == null) {
			System.out.println("El mentorizado es null o la entrada es mala");
			//Esto no deberia pasar nunca si se llama desde la aplicacion
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar acceder al la información de su cuenta o de "+
					"la del mentor, por favor,  si recibe este mensaje,"+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "+
					"intente ser lo más preciso posible al indicar la hora en la que ocurrió."),HttpStatus.BAD_REQUEST);
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
						"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "+
						"intente ser lo más preciso posible al indicar la hora en la que ocurrió."),HttpStatus.BAD_REQUEST);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un problema al intentar actualizar el repositorio, "+
					"por favor, vuelva a intentarlo más tarde."), HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { //Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "+
					"pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error, e "+
					"intente ser lo más preciso posible al indicar la hora en la que ocurrió."), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	 
	
}
