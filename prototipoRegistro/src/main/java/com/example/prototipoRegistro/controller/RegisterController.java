package com.example.prototipoRegistro.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.prototipoRegistro.configuration.MvcConfig;
import com.example.prototipoRegistro.configuration.WebSecurityConfig;
import com.example.prototipoRegistro.model.Usuario;
import com.example.prototipoRegistro.repository.DemoRepo;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/user")
public class RegisterController {
	
	@Autowired
	DemoRepo demorepo;
	MvcConfig mvcconf;
	
	
	
	@ModelAttribute("user")
	public Usuario usuario() {
		return new Usuario();
	}
	
	@GetMapping("/registration")
	public ModelAndView showRegistrationForm() {
	    Usuario user = new Usuario();
	    ModelAndView model = new ModelAndView("register");
	    model.addObject("user", user);
	    return model;
	}
	
	
	@PostMapping("/register")
	public ModelAndView registerUserAccount(@ModelAttribute("user") Usuario user) {
		System.out.println("kk");
	    user.setPassword(getPasswordEncoder().encode(user.getPassword()));
	    System.out.println(user.toString());
	    Usuario u = demorepo.save(user);
	    System.out.println(u.toString());
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
	
	@Bean
	public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
