package com.tfg.mentoring.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	public void addViewControllers(ViewControllerRegistry registry) {
		//registry.addViewController("/home").setViewName("home");
		//registry.addViewController("/").setViewName("home");
		registry.addViewController("/principalMentor").setViewName("principalMentor");
		registry.addViewController("/principalMentorizado").setViewName("principalMentorizado");
		registry.addViewController("/login").setViewName("login");
		//registry.addViewController("/register").setViewName("register");
		//registry.addViewController("/holacompleto").setViewName("holacompleto");
		//Esta solo son para verlas
		registry.addViewController("/vs").setViewName("verify_success");
		registry.addViewController("/vf").setViewName("verify_fail");
		registry.addViewController("/rs").setViewName("register_success");
		//registry.addViewController("/ep").setViewName("error_page");
		//registry.addViewController("/epl").setViewName("error_page_logued");
	}

}
