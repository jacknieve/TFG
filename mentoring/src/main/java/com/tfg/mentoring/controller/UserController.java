package com.tfg.mentoring.controller;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.model.AreaConocimiento;
import com.tfg.mentoring.model.Institucion;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.Notificacion;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.EstadosNotificacion;
import com.tfg.mentoring.model.auxiliar.IdNotificacion;
import com.tfg.mentoring.model.auxiliar.NotificacionUser;
import com.tfg.mentoring.model.auxiliar.Roles;
import com.tfg.mentoring.model.auxiliar.UsuarioPerfil;
import com.tfg.mentoring.repository.InstitucionRepo;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.NotificacionRepo;
import com.tfg.mentoring.service.UserService;
import com.tfg.mentoring.service.util.ListLoad;

//Partir este controlador en varios, al menos en uno para mentor y otro para mentorizado
@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private MentorizadoRepo menrepo;
	@Autowired
	private MentorRepo mrepo;
	@Autowired
	private NotificacionRepo notrepo;
	@Autowired
	private InstitucionRepo irepo;
	
	@Autowired
    private UserService uservice;
	
	
	@Autowired
	private ListLoad listas;
	@Autowired
	private SimpleDateFormat format;
	
/////////////////////////////////////////////////////////////////////////
////////                  Perfil de usuario                       ///////
/////////////////////////////////////////////////////////////////////////
	
	//Proveer la pagina del perfil
	@GetMapping("/perfil")
	public ModelAndView getPerfilPrivado(@AuthenticationPrincipal Usuario us) {
		try {
			//System.out.println(us.toString());
			if(us.getRol() == Roles.MENTOR) {
				Optional<Mentor> mentor = mrepo.findById(us.getUsername());
				if(mentor.isPresent()) {
					//System.out.println(mentor.get().toString());
					//System.out.println(format.format(mentor.get().getFnacimiento()));
					ModelAndView modelo = new ModelAndView("perfil");
					modelo.addObject("correo", mentor.get().getCorreo());
					modelo.addObject("rol", mentor.get().getUsuario().getRol());
					modelo.addObject("nivelEstudios", mentor.get().getNivelEstudios());
					modelo.addObject("fregistro", format.format(mentor.get().getFregistro()));
					modelo.addObject("puestos", listas.getPuestos());
				    modelo.addObject("estudios", listas.getEstudios());
				    modelo.addObject("instituciones", listas.getInstituciones());
				    modelo.addObject("areas", listas.getAreas());
					return modelo;
				}
				else {
					System.out.println("No existe");
					return new ModelAndView("error_page");
				}
			}
			else if(us.getRol() == Roles.MENTORIZADO){
				Optional<Mentorizado> mentorizado = menrepo.findById(us.getUsername());
				if(mentorizado.isPresent()) {
					//System.out.println(mentorizado.get().toString());
					//System.out.println(format.format(mentorizado.get().getFnacimiento()));
					ModelAndView modelo = new ModelAndView("perfil");
					modelo.addObject("correo", mentorizado.get().getCorreo());
					modelo.addObject("rol", mentorizado.get().getUsuario().getRol());
					modelo.addObject("nivelEstudios", mentorizado.get().getNivelEstudios());
					modelo.addObject("fregistro", format.format(mentorizado.get().getFregistro()));
					modelo.addObject("puestos", listas.getPuestos());
				    modelo.addObject("estudios", listas.getEstudios());
				    modelo.addObject("instituciones", listas.getInstituciones());
				    modelo.addObject("areas", listas.getAreas());
					return modelo;
				}
				else {
					System.out.println("No existe");
					return new ModelAndView("error_page");
				}
			}
			else {
				System.out.println("Otro rol");
				return new ModelAndView("error_page");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.toString());
			return new ModelAndView("error_page");
		}
	}
	
	//Obtener la informacion del perfil del usuario
	@GetMapping("/info")
	public ResponseEntity<UsuarioPerfil> getInfoPerfil(@AuthenticationPrincipal Usuario us) {
		try {
			if(us.getRol() == Roles.MENTOR) {
				Optional<Mentor> m = mrepo.findById(us.getUsername());
				if(m.isPresent()) {
					System.out.println(m.get().toString());
					//UsuarioPerfil up = new UsuarioPerfil(m.get());
					UsuarioPerfil up = uservice.getPerfilMentor(m.get());
					System.out.println(up.toString());
					return new ResponseEntity<>(up, HttpStatus.OK);
				}
				else {
					//Aqui tambien estaria bien hacer algo de error personalizado
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
			}else if(us.getRol() == Roles.MENTORIZADO){
				Optional<Mentorizado> m = menrepo.findById(us.getUsername());
				if(m.isPresent()) {
					//UsuarioPerfil up = new UsuarioPerfil(m.get());
					UsuarioPerfil up = uservice.getPerfilMentorizado(m.get());
					System.out.println(up.toString());
					return new ResponseEntity<>(up, HttpStatus.OK);
				}
				else {
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
			}else {
				//Esto cambiarlo a otro para decir que no es un rol permitido
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
		} catch (EntityNotFoundException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	//Actualizar la informacion del perfil del usuario
	@PostMapping("/setinfo")
	public ResponseEntity<Usuario> setInfoPerfil(@RequestBody UsuarioPerfil up, @AuthenticationPrincipal Usuario us) {
		try {
			System.out.println(up.toString());
			if(us.getRol() == Roles.MENTOR) {
				Optional<Mentor> m = mrepo.findById(us.getUsername());
				if(m.isPresent()) {
					Mentor men = m.get();
					men.setNombre(up.getNombre());
					men.setPapellido(up.getPapellido());
					men.setSapellido(up.getSapellido());
					men.setDescripcion(up.getDescripcion());
					men.setFnacimiento(up.getFnacimiento());
					men.setHoraspormes(up.getHoraspormes());
					men.setLinkedin(up.getLinkedin());
					men.setNivelEstudios(up.getNivelEstudios());
					men.setPuesto(up.getPuesto());
					men.setTelefono(up.getTelefono());
					if(!men.getInstitucion().getNombre().equals(up.getInstitucionNombre())) {
						Institucion i = irepo.getById(up.getInstitucionNombre());
						men.setInstitucion(i);
					}
					men.setAreas(up.getAreas());
					try {
						mrepo.save(men);
					}catch (Exception e) {
						// TODO: handle exception
						//Aqui una salida de fallo
						return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					return new ResponseEntity<>(null, HttpStatus.OK);
					
				}
				else {
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
			}else if(us.getRol() == Roles.MENTORIZADO){
				Optional<Mentorizado> m = menrepo.findById(us.getUsername());
				if(m.isPresent()) {
					Mentorizado men = m.get();
					men.setNombre(up.getNombre());
					men.setPapellido(up.getPapellido());
					men.setSapellido(up.getSapellido());
					men.setDescripcion(up.getDescripcion());
					men.setFnacimiento(up.getFnacimiento());
					men.setLinkedin(up.getLinkedin());
					men.setNivelEstudios(up.getNivelEstudios());
					men.setTelefono(up.getTelefono());
					if(!men.getInstitucion().getNombre().equals(up.getInstitucionNombre())) {
						Institucion i = irepo.getById(up.getInstitucionNombre());
						men.setInstitucion(i);
					}
					men.setAreas(up.getAreas());
					try {
						menrepo.save(men);
					}catch (Exception e) {
						// TODO: handle exception
						//Aqui una salida de fallo
						return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					return new ResponseEntity<>(null, HttpStatus.OK);
					
				}
				else {
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}
			}else {
				//Esto cambiarlo a otro para decir que no es un rol permitido
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	//Eliminar areas de conocimiento de un usuario
	@PostMapping("/areas/delete")
	public ResponseEntity<String> borrarAreaUsuario(@AuthenticationPrincipal Usuario us, @RequestBody AreaConocimiento area){
		System.out.println("Borrando");
		if(area == null) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		if(us.getRol() == Roles.MENTOR) {
			Optional<Mentor> m = mrepo.findById(us.getUsername());
			if(m.isPresent()) {
				try {
				mrepo.borrarArea(us.getUsername(), area.getArea());
				}catch (Exception e) {
					System.out.println(e.getMessage());
					return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				return new ResponseEntity<>(null, HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
		}else if(us.getRol() == Roles.MENTORIZADO){
			Optional<Mentorizado> m = menrepo.findById(us.getUsername());
			if(m.isPresent()) {
				try {
				menrepo.borrarArea(us.getUsername(), area.getArea());
				}catch (Exception e) {
					// TODO: handle exception
					System.out.println(e.getMessage());
					return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				return new ResponseEntity<>(null, HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
		}else {
			//Esto cambiarlo a otro para decir que no es un rol permitido
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}
	
	
/////////////////////////////////////////////////////////////////////////
////////                  Notificaciones                          ///////
/////////////////////////////////////////////////////////////////////////
	
	//Obtener notificaciones
	@GetMapping("/notificaciones")
	public ResponseEntity<List<NotificacionUser>> getAllNotificaciones(@AuthenticationPrincipal Usuario us) {
		try {
			List<Notificacion> Notificaciones = new ArrayList<Notificacion>();
			List<NotificacionUser> nUser = new ArrayList<NotificacionUser>();
			Notificaciones = notrepo.getNotificaciosUser(us.getUsername());
			if (Notificaciones.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			//notrepo.actualizaEstadoNotificaciosUser(us.getUsername());
			for(Notificacion n : Notificaciones) {
				//System.out.println(n.getEstado().toString());
				nUser.add(new NotificacionUser(n));
				if(n.getEstado() == EstadosNotificacion.ENTREGADA) {
					n.setEstado(EstadosNotificacion.LEIDA);
				}
			}
			notrepo.saveAll(Notificaciones);
			
			//Notificaciones.removeIf(n -> (n.getFechaeliminacion() != null));
			return new ResponseEntity<>(nUser, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	//Obtener notificaciones nuevas
	@GetMapping("/notificaciones/nuevas")
	public ResponseEntity<List<NotificacionUser>> getNewNotificaciones(@AuthenticationPrincipal Usuario us) {
		try {
			List<NotificacionUser> nUser = new ArrayList<NotificacionUser>();
			List<Notificacion> Notificaciones = new ArrayList<Notificacion>();
			Notificaciones = notrepo.getNews(us.getUsername());
			if (Notificaciones.isEmpty()) {
				System.out.println("Sin nuevas");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			for(Notificacion n : Notificaciones) {
				//System.out.println(n.getEstado().toString());
				nUser.add(new NotificacionUser(n));
				if(n.getEstado() == EstadosNotificacion.ENTREGADA) {
					n.setEstado(EstadosNotificacion.LEIDA);
				}
			}
			notrepo.saveAll(Notificaciones);
			return new ResponseEntity<>(nUser, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	//Borrar una notificacion
	@PostMapping("/notificaciones/delete")
	public ResponseEntity<String> borrarNotificacion(@RequestBody IdNotificacion id){
		System.out.println("Borrando");
		if(id == null) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		notrepo.borrarNotificacion(id.getId());
		return new ResponseEntity<>(null, HttpStatus.OK);
	}
	
/////////////////////////////////////////////////////////////////////////
////////             Pagina principal usuarios                    ///////
/////////////////////////////////////////////////////////////////////////
	
	@GetMapping("/principal")
	public ModelAndView getPaginaPrincipal(@AuthenticationPrincipal Usuario us) {
		try {
			//System.out.println(us.toString());
			if(us.getRol() == Roles.MENTOR) {
				Optional<Mentor> mentor = mrepo.findById(us.getUsername());
				if(mentor.isPresent()) {
					//Aqeui habria que recuperar cosas de su institucion para la pagina
					ModelAndView modelo = new ModelAndView("principalMentor");
					return modelo;
				}
				else {
					System.out.println("No existe");
					return new ModelAndView("error_page");
				}
			}
			else if(us.getRol() == Roles.MENTORIZADO){
				Optional<Mentorizado> mentorizado = menrepo.findById(us.getUsername());
				if(mentorizado.isPresent()) {
					//System.out.println(mentorizado.get().toString());
					//System.out.println(format.format(mentorizado.get().getFnacimiento()));
					ModelAndView modelo = new ModelAndView("principalMentorizado");
					modelo.addObject("instituciones", listas.getInstituciones());
				    modelo.addObject("areas", listas.getAreas());
					return modelo;
				}
				else {
					System.out.println("No existe");
					return new ModelAndView("error_page");
				}
			}
			else {
				System.out.println("Otro rol");
				return new ModelAndView("error_page");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.toString());
			return new ModelAndView("error_page");
		}
	}
	

	
	
	
	
	
	
}
