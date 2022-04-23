package com.tfg.mentoring.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.tfg.mentoring.model.auxiliar.MentorBusqueda;
import com.tfg.mentoring.model.auxiliar.MentorizacionUser;
import com.tfg.mentoring.model.auxiliar.UsuarioPerfil;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizacionRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.PeticionRepo;
import com.tfg.mentoring.service.UserService;

@RestController
@RequestMapping("/user/mentorizado")
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

	// A esto hay que meterle "seguridad", es decir, try y catch para las
	// excepciones
	@GetMapping("/busqueda/{area}/{institucion}/{horas}")
	public ResponseEntity<List<MentorBusqueda>> buscar(@PathVariable("area") String area,
			@PathVariable("institucion") String institucion, @PathVariable("horas") float horas) {
		List<Mentor> mentores = new ArrayList<Mentor>();
		if (area == null || area.equals("sin")) {
			if (institucion == null || institucion.equals("sin")) {// Si no se selecciono institucion
				mentores = mrepo.buscarHoras(horas);
			} else {// Si se selecciono institucion
				mentores = mrepo.buscarInstitucionHoras(institucion, horas);
			}
		} else {
			if (institucion == null || institucion.equals("sin")) {// Si no se selecciono institucion
				mentores = mrepo.buscarAreaHoras(area, horas);
			} else {// Si se selecciono institucion
				mentores = mrepo.buscarCompleto(area, institucion, horas);
			}
		}
		try {
			List<MentorBusqueda> resultado = uservice.getMentorBusqueda(mentores);
			return new ResponseEntity<>(resultado, HttpStatus.OK);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping("/obtenermentor")
	public ResponseEntity<UsuarioPerfil> getPerfilBusqueda(@AuthenticationPrincipal Usuario us,
			@RequestBody String mentor) {
		try {
			Optional<Mentor> m = mrepo.findById(mentor);
			if (m.isPresent()) {
				UsuarioPerfil up = uservice.getPerfilMentor(m.get());
				return new ResponseEntity<>(up, HttpStatus.OK);
			} else {
				System.out.println("No hay mentor");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

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
	public ResponseEntity<String> enviarSolicitud(@AuthenticationPrincipal Usuario us,
			@RequestBody EnvioPeticion peticion) {
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
						prepo.save(p);
						// Solo vamos a enviar notificacion si es la primera vez
						uservice.enviarNotificacion(m.get().getUsuario(), "Nueva peticion", "El usuario "
								+ men.get().getNombre() + " te ha enviado una petición de mentorizacion.");
						return new ResponseEntity<>(null, HttpStatus.OK);
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
					return new ResponseEntity<>(HttpStatus.CONFLICT);
				}
			} else {
				System.out.println("Fallo al acceder a los usuarios");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
				mUser.add(new MentorizacionUser(m, uservice.getPerfilMentor(m.getMentor())));
			}

			return new ResponseEntity<>(mUser, HttpStatus.OK);
		} catch (Exception e) {
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
						mUser.add(new MentorizacionUser(m, uservice.getPerfilMentor(m.getMentor())));
					} else {
						mUser.add(new MentorizacionUser(m, null));
					}

				}
				// System.out.println(mUser.toString());
				return new ResponseEntity<>(mUser, HttpStatus.OK);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			System.out.println("Date esta mal");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	// Otro prototipo de busqueda, pero tampoco funciona bien, creo que le vuelven
	// loco las areas
	/*
	 @GetMapping("/busquedados/{area}/{institucion}/{horas}") public ResponseEntity<List<MentorBusqueda>> buscardos(@PathVariable("area") String area,
	 	@PathVariable("institucion") String institucion, @PathVariable("horas") float horas) { 
	 	List<Mentor> mentores = new ArrayList<Mentor>();
	 	if(area.equals("sin")) area=null; 
	 	if(institucion.equals("sin")) institucion=null; 
	 	mentores = mrepo.buscarPrototipo(institucion, horas, area);
	 	try { 
	 		List<MentorBusqueda> resultado = uservice.getMentorBusqueda(mentores);
	 		return new ResponseEntity<>(resultado, HttpStatus.OK); 
	 	}catch (Exception e) {
	 		// TODO: handle exception System.out.println(e.getMessage()); 
	 		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); 
	 	}
	}
	 */
	// Este es un prototipo de busqueda, pero no funciona, es mas simple, pero no
	// filtra, trae todo
	/*
	 * @GetMapping("/busqueda/{area}/{institucion}/{horas}") public
	 * ResponseEntity<List<MentorBusqueda>> buscar(@PathVariable("area") String
	 * area,
	 * 
	 * @PathVariable("institucion") String institucion, @PathVariable("horas") float
	 * horas) { System.out.println(area+" "+institucion+" "+horas); List<Mentor>
	 * mentores = new ArrayList<Mentor>(); Mentor mentor = new Mentor(new
	 * Usuario(null, null, null, true, true, null), null, null, null, null, null,
	 * null, null, null, null, null, null, null, null); if(institucion != null &&
	 * !institucion.equals("sin")) { mentor.setInstitucion(new
	 * Institucion(institucion)); } if(area != null && !area.equals("sin")) {
	 * mentor.getAreas().add(new AreaConocimiento(area)); } mentores =
	 * mrepo.findAll(Example.of(mentor)); try { List<MentorBusqueda> resultado =
	 * uservice.getMentorBusqueda(mentores); return new ResponseEntity<>(resultado,
	 * HttpStatus.OK); }catch (Exception e) { // TODO: handle exception
	 * System.out.println(e.getMessage()); return new ResponseEntity<>(null,
	 * HttpStatus.INTERNAL_SERVER_ERROR); }
	 * 
	 * }
	 */
}
