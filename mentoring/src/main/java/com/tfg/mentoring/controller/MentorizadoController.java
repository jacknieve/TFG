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
import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.DTO.MentorDTO;
import com.tfg.mentoring.model.auxiliar.DTO.MentorInfoDTO;
import com.tfg.mentoring.model.auxiliar.DTO.MentorizacionDTO;
import com.tfg.mentoring.model.auxiliar.enums.MotivosNotificacion;
import com.tfg.mentoring.model.auxiliar.requests.CamposBusqueda;
import com.tfg.mentoring.model.auxiliar.requests.EnvioPeticion;
import com.tfg.mentoring.model.auxiliar.requests.MentorizacionCerrar;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizacionRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.service.MapeadoService;
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
	private UserService uservice;
	@Autowired
	private SalaChatServicio schats;
	@Autowired
	private MapeadoService mservice;

	@PostMapping("/busqueda")
	public ResponseEntity<List<MentorDTO>> buscar(@RequestBody CamposBusqueda campos) {
		if (campos.getHoras() < 4 || campos.getHoras() > 80) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		try {
			List<MentorDTO> resultado = uservice.buscarMentores(campos);
			if (resultado.isEmpty()) {
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
	public ResponseEntity<MentorInfoDTO> getPerfilBusqueda(@AuthenticationPrincipal UserAuth us,
			@RequestBody String mentor) {
		try {
			Optional<Mentor> m = uservice.getMentorPerfil(mentor);
			if (m.isPresent()) {
				// MentorInfoDTO up = mservice.getMentorInfoBusqueda(m.get());
				return new ResponseEntity<>(mservice.getMentorInfoBusqueda(m.get()), HttpStatus.OK);
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

	@PostMapping("/enviarsolicitud")
	public ResponseEntity<MensajeError> enviarSolicitud(@AuthenticationPrincipal UserAuth us,
			@RequestBody EnvioPeticion peticion) {
		if (peticion.getMentor() == null) {
			System.out.println("El mentorizado es null o la entrada es mala");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		try {
			int resultado = uservice.enviarSolicitud(peticion, us.getUsername());
			switch (resultado) {
			case 0:
				return new ResponseEntity<>(new MensajeError("La solicitud ha sido enviada de forma exitosa", null),
						HttpStatus.OK);
			case 1:
				return new ResponseEntity<>(new MensajeError("El contenido de la solicitud se ha actualizado.", null),
						HttpStatus.OK);
			case 2:
				System.out.println("Ya hay una mentorizacion abierta");
				return new ResponseEntity<>(
						new MensajeError("Mentorización ya establecida",
								"Ya has establecido una relación de mentorización con ese mentor "),
						HttpStatus.CONFLICT);
			case 3:
				System.out.println("No se envia notificacion porque el usuario esta disabled");
				return new ResponseEntity<>(
						new MensajeError("Usuario eliminado",
								"El usuario al que le querias enviar la solicitud acaba de eliminar su cuenta"),
						HttpStatus.NOT_FOUND);
			default:
				System.out.println("Fallo al acceder a los usuarios");
				return new ResponseEntity<>(new MensajeError("Fallo en la peticion",
						"Se ha producido un problema al intentar acceder al la información de su cuenta o de "
								+ "la del mentor, por favor,  si recibe este mensaje,"
								+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
								+ new Date()),
						HttpStatus.NOT_FOUND);
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
				mUser.add(new MentorizacionDTO(m, mservice.getMentorMentorizacion(m.getMentor()),
						m.getMentor().getCorreo(), true));
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

	// Obtener mentorizaciones nuevas
	@GetMapping("/mentorizaciones/actualizar/{date}")
	public ResponseEntity<List<MentorizacionDTO>> getNewMentorizaciones(@AuthenticationPrincipal UserAuth us,
			@PathVariable("date") Long date) {
		try {
			Timestamp fecha = new Timestamp(date);
			List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
			List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
			mentorizaciones = mentorizacionrepo.getNuevasMentorizado(us.getUsername(), fecha);
			if (mentorizaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Mentorizacion m : mentorizaciones) {
				if (m.getFin() == null) {
					mUser.add(new MentorizacionDTO(m, mservice.getMentorMentorizacion(m.getMentor()),
							m.getMentor().getCorreo(), true));
				} else {
					mUser.add(new MentorizacionDTO(m, null, m.getMentor().getCorreo(), true));
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

	@PostMapping("/mentorizaciones/cerrar")
	public ResponseEntity<MensajeError> cerrarMentorizacion(@AuthenticationPrincipal UserAuth us,
			@RequestBody MentorizacionCerrar mentorizacion) {
		if (mentorizacion.getMentor() == null || mentorizacion.getPuntuacion() < -1
				|| mentorizacion.getPuntuacion() > 10) {
			System.out.println("El mentorizado es null o la entrada es mala");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		try {
			Optional<Mentor> m = mrepo.findById(mentorizacion.getMentor());
			Optional<Mentorizado> men = menrepo.findById(us.getUsername());
			if (m.isPresent() && men.isPresent()) {
				mentorizacionrepo.cerrarPuntuarMentorizacion(mentorizacion.getPuntuacion(),
						mentorizacion.getComentario(), mentorizacion.getMentor(), us.getUsername());
				schats.cerrarChat(m.get().getCorreo(), men.get().getCorreo(), false);
				uservice.enviarNotificacion(m.get().getUsuario(), "Mentorizacion cerrada",
						"El usuario " + men.get().getNombre()
								+ " ha cerrado una mentorización que tenía abierta contigo.",
						MotivosNotificacion.CIERRE);
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				System.out.println("Fallo al acceder a los usuarios");
				return new ResponseEntity<>(new MensajeError("Fallo en la peticion",
						"Se ha producido un problema al intentar acceder al la información de su cuenta o de "
								+ "la del mentor, por favor,  si recibe este mensaje,"
								+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
								+ new Date()),
						HttpStatus.NOT_FOUND);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { // Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
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
				mUser.add(new MentorizacionDTO(m, mservice.getMentorInfo(m.getMentor()), m.getMentor().getCorreo(),
						true));
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

	@GetMapping("/mentorizaciones/porpuntuar/{date}")
	public ResponseEntity<List<MentorizacionDTO>> getNewMentorizacionesPorPuntuar(@AuthenticationPrincipal UserAuth us,
			@PathVariable("date") Long date) {
		try {
			Timestamp fecha = new Timestamp(date);
			List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
			List<MentorizacionDTO> mUser = new ArrayList<MentorizacionDTO>();
			mentorizaciones = mentorizacionrepo.obtenerMentorizacionesPorPuntuarNuevas(us.getUsername(), fecha);
			if (mentorizaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Mentorizacion m : mentorizaciones) {
				mUser.add(new MentorizacionDTO(m, mservice.getMentorInfo(m.getMentor()), m.getMentor().getCorreo(),
						true));

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

	@PostMapping("/mentorizaciones/puntuar")
	public ResponseEntity<MensajeError> puntuarMentorizacion(@AuthenticationPrincipal UserAuth us,
			@RequestBody MentorizacionCerrar mentorizacion) {
		if (mentorizacion.getMentor() == null || mentorizacion.getPuntuacion() < -1 
				|| mentorizacion.getPuntuacion() > 10 || mentorizacion.getFechafin() == null) {
			System.out.println("El mentorizado es null o la entrada es mala");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		try {
			Optional<Mentor> m = mrepo.findById(mentorizacion.getMentor());
			Optional<Mentorizado> men = menrepo.findById(us.getUsername());
			if (m.isPresent() && men.isPresent()) {
				mentorizacionrepo.puntuarMentorizacion(mentorizacion.getPuntuacion(), mentorizacion.getComentario(),
						mentorizacion.getMentor(), us.getUsername(), new Timestamp(mentorizacion.getFechafin()));
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				System.out.println("Fallo al acceder a los usuarios");
				return new ResponseEntity<>(new MensajeError("Fallo en la peticion",
						"Se ha producido un problema al intentar acceder al la información de su cuenta o de "
								+ "la del mentor, por favor,  si recibe este mensaje,"
								+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
								+ new Date()),
						HttpStatus.NOT_FOUND);
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) { // Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
