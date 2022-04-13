package com.tfg.mentoring.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.configuration.MvcConfig;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.repository.UsuarioRepo;

//@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/user")
public class RegisterController {
	
	@Autowired
	private UsuarioRepo urepo;
	@Autowired
	MvcConfig mvcconf;
	
	@ModelAttribute("user")
	public Usuario usuario() {
		return new Usuario();
	}
	
	@GetMapping("/registration/mentor")
	public ModelAndView showRegistrationMentorForm() {
	    Usuario user = new Usuario();
	    ModelAndView model = new ModelAndView("register");
	    model.addObject("user", user);
	    return model;
	}
	
	@GetMapping("/registration/mentorizado")
	public ModelAndView showRegistrationMentorizadoForm() {
	    Usuario user = new Usuario();
	    ModelAndView model = new ModelAndView("register_mentorizado");
	    model.addObject("user", user);
	    return model;
	}
	
	
	@PostMapping("/register/mentor")
	public ModelAndView registerUserAccountMentor(@ModelAttribute("user") Usuario user) {
	    user.setPassword(getPasswordEncoder().encode(user.getPassword()));
	    //System.out.println(user.toString());
	    //Usuario u = demorepo.save(user);
	    urepo.save(user);
	    //System.out.println(u.toString());
	    return new ModelAndView("login");
	}
	
	@PostMapping("/register/mentorizado")
	public ModelAndView registerUserAccountMentorizado(@ModelAttribute("user") Usuario user) {
	    user.setPassword(getPasswordEncoder().encode(user.getPassword()));
	    //System.out.println(user.toString());
	    //Usuario u = demorepo.save(user);
	    urepo.save(user);
	    //System.out.println(u.toString());
	    return new ModelAndView("login");
	}
	
	@GetMapping("/logout")
	public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {
		 Authentication auth = SecurityContextHolder.getContext().getAuthentication();  
		 System.out.println(auth.getDetails().toString());
	        if (auth != null){      
	           new SecurityContextLogoutHandler().logout(request, response, auth);  
	        }  
	    return new ModelAndView("afterlogout");
	}
	
	/*@GetMapping("/autologout")
	public ResponseEntity<String> autologout(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("kk");
		 Authentication auth = SecurityContextHolder.getContext().getAuthentication();  
		 System.out.println(auth.getDetails().toString());
	        if (auth != null){      
	           new SecurityContextLogoutHandler().logout(request, response, auth);  
	        }  
	        return new ResponseEntity<>(null, HttpStatus.OK);
	}*/
	
	@Bean
	public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
	

}
