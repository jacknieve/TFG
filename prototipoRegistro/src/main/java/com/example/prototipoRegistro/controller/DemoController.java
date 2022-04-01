package com.example.prototipoRegistro.controller;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.prototipoRegistro.configuration.MvcConfig;
import com.example.prototipoRegistro.model.Usuario;
import com.example.prototipoRegistro.repository.DemoRepo;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/api")
public class DemoController {

	@Autowired
	DemoRepo demorepo;
	@Autowired
	MvcConfig mvcconf;
	//@Autowired
	//UserModelAssembler uasembler;
	
	@GetMapping("/Usuarios")
	public ResponseEntity<List<Usuario>> getAllUsuarios(@RequestParam(required = false) String campo) {
		try {
			List<Usuario> Usuarios = new ArrayList<Usuario>();
			if (campo == null)
				demorepo.findAll().forEach(Usuarios::add);
			else
				demorepo.findByArea(campo).forEach(Usuarios::add);
			if (Usuarios.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(Usuarios, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@PostMapping("/Usuarios")
	public ResponseEntity<Usuario> createUsuario(@RequestBody Usuario Usuario) {
		try {
			Usuario _Usuario = demorepo.save(new Usuario(Usuario.getUsername(), Usuario.getPassword(), Usuario.getEdad(), Usuario.isMentor(), Usuario.getArea()));
			return new ResponseEntity<>(_Usuario, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@PutMapping("/Usuarios/{id}")
	public ResponseEntity<Usuario> updateTutorial(@PathVariable("nombre") String nombre, @RequestBody Usuario user) {
		Optional<Usuario> userData = Optional.of(demorepo.findByUsername(nombre));
		if (userData.isPresent()) {
			Usuario _user = userData.get();
			_user.setUsername(user.getUsername());
			_user.setEdad(user.getEdad());
			_user.setMentor(user.isMentor());
			_user.setArea(user.getArea());
			return new ResponseEntity<>(demorepo.save(_user), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	@GetMapping("/Usuarios/{id}")
	public ResponseEntity<Usuario> getUser(@PathVariable("id") Long id) {
		try {
			if (id == null) {
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}
			else {
				Usuario usuario = demorepo.findById(id);
				return new ResponseEntity<>(usuario, HttpStatus.OK);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/*
	@DeleteMapping("/Usuarios/{id}")
	public ResponseEntity<HttpStatus> deleteUsuario(@PathVariable("id") long id) {
		try {
			demorepo.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}*/
	@DeleteMapping("/Usuarios")
	public ResponseEntity<HttpStatus> deleteAllUsuarios() {
		try {
			demorepo.deleteAll();
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@GetMapping("/busqueda")
	public ResponseEntity<List<Usuario>> busqueda(@AuthenticationPrincipal Usuario us) {
		try {
			List<Usuario> Usuarios;
			if(us.isMentor()) {
				Usuarios = demorepo.findByMentor(false);
			}
			else {
				Usuarios = demorepo.findByMentor(true);
			}
			
			if (Usuarios.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(Usuarios, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/*@GetMapping("/Usuarios/mentorizados")
	public ResponseEntity<List<Usuario>> findByMentorizados() {
		try {
			List<Usuario> Usuarios = demorepo.findByMentor(false);
			if (Usuarios.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(Usuarios, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}*/
	@GetMapping("/insertar")
	public ResponseEntity<String> insertar() {
		try {
			//demorepo.save(new Usuario("pepe", secConf.getPasswordEncoder().encode("pepe"), 30, true, "Telecomunicaciones"));
			//demorepo.save(new Usuario("luis", secConf.getPasswordEncoder().encode("luis"), 30, false, "Telecomunicaciones"));
			return new ResponseEntity<>("Ok", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@GetMapping("/pruebahola")
	public ModelAndView pruebaHola(@AuthenticationPrincipal Usuario us) {
		ModelAndView model;
		if(us.isMentor()) {
		    model = new ModelAndView("holacompleto");
		    
		}
		else {
			model = new ModelAndView("holacompleto2");
		}
		model.addObject("user", us);
	    //model.addObject("notificaciones", us.getNotificaciones());
		return model;
	}
	
	
	
	
}

