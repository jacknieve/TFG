package com.tfg.mentoring.controller;

import java.sql.Timestamp;
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
import org.springframework.web.bind.annotation.PathVariable;
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
import com.tfg.mentoring.model.auxiliar.MentorBusqueda;
import com.tfg.mentoring.model.auxiliar.NotificacionUser;
import com.tfg.mentoring.model.auxiliar.Roles;
import com.tfg.mentoring.model.auxiliar.UsuarioPerfil;
import com.tfg.mentoring.repository.InstitucionRepo;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.NotificacionRepo;
import com.tfg.mentoring.service.UserService;
import com.tfg.mentoring.service.util.ListLoad;


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
	
	@GetMapping("/info")
	public ResponseEntity<UsuarioPerfil> getInfoPerfil(@AuthenticationPrincipal Usuario us) {
		try {
			if(us.getRol() == Roles.MENTOR) {
				Optional<Mentor> m = mrepo.findById(us.getUsername());
				if(m.isPresent()) {
					System.out.println(m.get().toString());
					UsuarioPerfil up = new UsuarioPerfil(m.get());
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
					UsuarioPerfil up = new UsuarioPerfil(m.get());
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
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
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
					if(!men.getInstitucion().getNombre().equals(up.getInstitucion())) {
						Institucion i = irepo.getById(up.getInstitucion());
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
					if(!men.getInstitucion().getNombre().equals(up.getInstitucion())) {
						Institucion i = irepo.getById(up.getInstitucion());
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
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
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
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/notificaciones/{date}")
	public ResponseEntity<List<Notificacion>> getNewNotificaciones(@AuthenticationPrincipal Usuario us, @PathVariable("date") Long date) {
		try {
			List<Notificacion> Notificaciones = new ArrayList<Notificacion>();
			Timestamp fecha = new Timestamp(date);
			Notificaciones = notrepo.getNews(us.getUsername(),fecha);
			if (Notificaciones.isEmpty()) {
				System.out.println("Sin nuevas");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(Notificaciones, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/notificaciones/delete")
	public ResponseEntity<String> borrarNotificacion(@RequestBody IdNotificacion id){
		System.out.println("Borrando");
		if(id == null) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		notrepo.borrarNotificacion(id.getId());
		return new ResponseEntity<>(null, HttpStatus.OK);
	}
	
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
	
	//A esto hay que meterle "seguridad", es decir, try y catch para las excepciones
	@GetMapping("/busqueda/{area}/{institucion}/{horas}")
	public ResponseEntity<List<MentorBusqueda>> buscar(@PathVariable("area") String area,  
			@PathVariable("institucion") String institucion,  @PathVariable("horas") float horas) {
		List<Mentor> mentores = new ArrayList<Mentor>();
		if(area == null || area.equals("sin")) {
			if(institucion == null || institucion.equals("sin")) {//Si no se selecciono institucion
				if(horas == 0.0) {//Ni horas
					mentores = mrepo.buscarTodos();
				}
				else {//Pero si horas
					mentores = mrepo.buscarHoras(horas);
				}
			}
			else {//Si se selecciono institucion
				if(horas == 0.0) {//Pero no horas
					mentores = mrepo.buscarInstitucion(institucion);
				}
				else {//Y horas
					mentores = mrepo.buscarInstitucionHoras(institucion, horas);
				}
			}
		}
		else {
			if(institucion == null || institucion.equals("sin")) {//Si no se selecciono institucion
				if(horas == 0.0) {//Ni horas
					mentores = mrepo.buscarArea(area);
				}
				else {//Pero si horas
					mentores = mrepo.buscarAreaHoras(area, horas);
				}
			}
			else {//Si se selecciono institucion
				if(horas == 0.0) {//Pero no horas
					mentores = mrepo.buscarAreaInstitucion(area, institucion);
				}
				else {//Y horas
					mentores = mrepo.buscarCompleto(area, institucion, horas);
				}
			}
			
		}
		try {
			List<MentorBusqueda> resultado = uservice.getMentorBusqueda(mentores);
			return new ResponseEntity<>(resultado, HttpStatus.OK);
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@PostMapping("/obtenermentor")
	public ResponseEntity<Mentor> getPerfilBusqueda(@AuthenticationPrincipal Usuario us, @RequestBody String mentor) {
		try {
			Optional<Mentor> m = mrepo.findById(mentor);
			if(m.isPresent()) {
				return new ResponseEntity<>(m.get(), HttpStatus.OK);
			}
			else {
				System.out.println("No hay mentor");
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	
}
