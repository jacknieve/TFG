package com.tfg.mentoring.configuration;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.repository.UsuarioRepo;



@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	
	@Autowired
	private UsuarioRepo urepo;
	@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(new UserDetailsService() {
			
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				//Usuario ud = demoRepo.findByNombre(username);
				//ud.setPassword(getPasswordEncoder().encode(ud.getPassword()));
				//System.out.println(username);
				Usuario u = urepo.findByUsername(username);
				if(u == null) {
					throw new UsernameNotFoundException("No se encontro "+username);
				}
				//UserDetails ud = xmpps.autentificacion(u);
				UserDetails ud = new UserAuth(u);
				//System.out.println(ud.getAuthorities());
				
				return ud;
			}
		});
    }
    
	//Yo creo que aqui tambien se necesita el prebind
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().and()
			.authorizeRequests()
				.antMatchers("/", "/home", "/auth/**", "/css/**", "/js/**","/images/**", "/cierre", "/info",
						"/verify_success", "/verify_fail", "/register_success", "/error_page", "/vs", "/vf", "/rs", "/perror", "/perrorl").permitAll()
				.antMatchers("/principalMentor", "/mentor/**").hasAuthority("MENTOR")
				.antMatchers("/principalMentorizado", "/mentorizado/**").hasAuthority("MENTORIZADO")
				.antMatchers("/user/**").hasAnyAuthority("MENTOR","MENTORIZADO")
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.loginPage("/login")
				.failureUrl("/login?error=true")
				.successHandler(myAuthenticationSuccessHandler())
				.permitAll()
				.and()
			.logout().logoutUrl("/user/logout")
				.permitAll();
		//.logoutSuccessUrl("/afterlogout.html")
		http.csrf().disable();
	}
	
	@Bean
	public AuthenticationSuccessHandler myAuthenticationSuccessHandler(){
	    return new MySimpleUrlAuthenticationSuccessHandler();
	}
	
	@Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
