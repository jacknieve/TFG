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
import com.tfg.mentoring.model.auxiliar.EstadosPeticion;
import com.tfg.mentoring.model.auxiliar.MentorizacionUser;
import com.tfg.mentoring.model.auxiliar.PeticionUser;
import com.tfg.mentoring.model.auxiliar.UsuarioPerfil;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizacionRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.PeticionRepo;
import com.tfg.mentoring.service.UserService;

@RestController
@RequestMapping("/user/mentor")
public class MentorController {

	@Autowired
	private UserService uservice;

	@Autowired
	private MentorizadoRepo menrepo;
	@Autowired
	private MentorRepo mrepo;
	@Autowired
	private MentorizacionRepo mentorizacionrepo;

	@Autowired
	private PeticionRepo prepo;

	// Obtener peticiones
	@GetMapping("/peticiones")
	public ResponseEntity<List<PeticionUser>> getPeticiones(@AuthenticationPrincipal Usuario us) {
		try {
			List<Peticion> peticiones = new ArrayList<Peticion>();
			List<PeticionUser> pUser = new ArrayList<PeticionUser>();
			peticiones = prepo.obtenerPeticiones(us.getUsername());
			if (peticiones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Peticion p : peticiones) {
				// System.out.println(n.getEstado().toString());
				pUser.add(new PeticionUser(p));
				if (p.getEstado() == EstadosPeticion.ENVIADA) {
					p.setEstado(EstadosPeticion.RECIBIDA);
				}
			}
			prepo.saveAll(peticiones);

			// Notificaciones.removeIf(n -> (n.getFechaeliminacion() != null));
			return new ResponseEntity<>(pUser, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Obtener peticiones nuevas
	@GetMapping("/peticiones/actualizar")
	public ResponseEntity<List<PeticionUser>> getNewPeticiones(@AuthenticationPrincipal Usuario us) {
		try {
			List<Peticion> peticiones = new ArrayList<Peticion>();
			List<PeticionUser> pUser = new ArrayList<PeticionUser>();
			peticiones = prepo.getNews(us.getUsername());
			if (peticiones.isEmpty()) {
				System.out.println("Sin nuevas peticiones");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Peticion p : peticiones) {
				// System.out.println(n.getEstado().toString());
				pUser.add(new PeticionUser(p));
				if (p.getEstado() == EstadosPeticion.ENVIADA) {
					p.setEstado(EstadosPeticion.RECIBIDA);
				}
			}
			prepo.saveAll(peticiones);
			return new ResponseEntity<>(pUser, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/peticiones/perfil")
	public ResponseEntity<UsuarioPerfil> getPerfilPeticion(@AuthenticationPrincipal Usuario us,
			@RequestBody String mentorizado) {
		try {
			Optional<Mentorizado> m = menrepo.findById(mentorizado);
			if (m.isPresent()) {
				UsuarioPerfil up = uservice.getPerfilMentorizado(m.get());
				return new ResponseEntity<>(up, HttpStatus.OK);
			} else {
				System.out.println("No hay mentorizado");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/peticiones/aceptar")
	public ResponseEntity<String> aceptarPeticion(@AuthenticationPrincipal Usuario us,
			@RequestBody String mentorizado) {
		try {
			// Aqui, como en las demas, deberia hacerse una comprobacion de que el usuario
			// esta enable
			Optional<Mentorizado> men = menrepo.findById(mentorizado);
			Optional<Mentor> m = mrepo.findById(us.getUsername());
			if (m.isPresent() && men.isPresent()) {// Asuminos no hay problemas con el username del usuario, y que no
													// debe haber ya una mentorizacion
				Mentorizacion mentorizacion = new Mentorizacion(m.get(), men.get());
				mentorizacionrepo.save(mentorizacion);
				// List<Peticion> peticiones = prepo.comprobarPeticion(us.getUsername(),
				// mentorizado);
				prepo.aceptarPeticion(us.getUsername(), mentorizado);
				uservice.enviarNotificacion(men.get().getUsuario(), "Peticion aceptada",
						"El usuario " + m.get().getNombre() + " te ha aceptado una petición de mentorizacion.");
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				System.out.println("No hay usuarios");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/peticiones/rechazar")
	public ResponseEntity<String> rechazarPeticion(@AuthenticationPrincipal Usuario us,
			@RequestBody String mentorizado) {
		try {
			Optional<Mentorizado> men = menrepo.findById(mentorizado);
			Optional<Mentor> m = mrepo.findById(us.getUsername());
			if (m.isPresent() && men.isPresent()) {// Asuminos no hay problemas con el username del usuario, y que no
													// debe haber ya una mentorizacion
				// List<Peticion> peticiones = prepo.comprobarPeticion(us.getUsername(),
				// mentorizado);
				prepo.rechazarPeticion(us.getUsername(), mentorizado);
				uservice.enviarNotificacion(men.get().getUsuario(), "Peticion rechazada",
						"El usuario " + m.get().getNombre() + " te ha rechazado una petición de mentorizacion.");
				return new ResponseEntity<>(null, HttpStatus.OK);
			} else {
				System.out.println("No hay usuarios");
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
			mentorizaciones = mentorizacionrepo.obtenerMentorizacionesMentor(us.getUsername());
			if (mentorizaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for (Mentorizacion m : mentorizaciones) {
				// System.out.println(n.getEstado().toString());
				mUser.add(new MentorizacionUser(m, uservice.getPerfilMentorizado(m.getMentorizado())));
			}

			return new ResponseEntity<>(mUser, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Obtener peticiones nuevas
	@GetMapping("/mentorizaciones/actualizar/{date}")
		public ResponseEntity<List<MentorizacionUser>> getNewMentorizaciones(@AuthenticationPrincipal Usuario us, @PathVariable("date") Long date) {
			if(date != null) {//Tambien ver si de primeras ya se puede ver si es parseable
			try {
				Timestamp fecha = new Timestamp(date);
				List<Mentorizacion> mentorizaciones = new ArrayList<Mentorizacion>();
				List<MentorizacionUser> mUser = new ArrayList<MentorizacionUser>();
				mentorizaciones = mentorizacionrepo.getNuevasMentor(us.getUsername(), fecha);
				if (mentorizaciones.isEmpty()) {
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
				for(Mentorizacion m : mentorizaciones) {
					if(m.getFin() == null) {
						mUser.add(new MentorizacionUser(m, uservice.getPerfilMentorizado(m.getMentorizado())));
					}
					else {
						mUser.add(new MentorizacionUser(m, null));
					}
					
				}
				//System.out.println(mUser.toString());
				return new ResponseEntity<>(mUser, HttpStatus.OK);
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

}
