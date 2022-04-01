package com.example.prototipoRegistro.configuration;

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

import com.example.prototipoRegistro.repository.DemoRepo;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private DemoRepo demoRepo;
	@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(new UserDetailsService() {
			
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				// TODO Auto-generated method stub
				//Usuario ud = demoRepo.findByNombre(username);
				//ud.setPassword(getPasswordEncoder().encode(ud.getPassword()));
				UserDetails ud = demoRepo.findByUsername(username);
				if(ud == null) {
					throw new UsernameNotFoundException("No se encontro "+username);
				}
				System.out.println(ud.getAuthorities());
				
				return ud;
			}
		});
    }
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers("/", "/home", "/api/insertar", "/user/**", "/css/**", "/js/**", "/api/Usuarios", "/api/Usuarios/**").permitAll()
				.antMatchers("/hello").hasAuthority("MENTOR")
				.antMatchers("/hello2").hasAuthority("MENTORIZADO")
				.antMatchers("/api/Usuarios/mentores").hasAuthority("MENTORIZADO")
				.antMatchers("/api/Usuarios/mentorizados").hasAuthority("MENTOR")
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
	}
	/*
	@Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }*/
	
	@Bean
	public AuthenticationSuccessHandler myAuthenticationSuccessHandler(){
	    return new MySimpleUrlAuthenticationSuccessHandler();
	}

}