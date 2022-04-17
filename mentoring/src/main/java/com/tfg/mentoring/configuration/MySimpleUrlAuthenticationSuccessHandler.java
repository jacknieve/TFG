package com.tfg.mentoring.configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.tfg.mentoring.model.auxiliar.UsuarioLogeado;
import com.tfg.mentoring.model.auxiliar.UsuariosActivos;


public class MySimpleUrlAuthenticationSuccessHandler
implements AuthenticationSuccessHandler {

//https://www.baeldung.com/spring_redirect_after_login
  private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

  @Autowired
  UsuariosActivos usuariosActivos;
  //Cambiamos el comportamiento de este método para poder redirigir según el rol del usuario
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, 
		  HttpServletResponse response, Authentication authentication) throws IOException {

      handle(request, response, authentication);
      clearAuthenticationAttributes(request, authentication);
  }
  
  protected void handle( HttpServletRequest request, HttpServletResponse response, Authentication authentication
	) throws IOException {

	    String targetUrl = determineTargetUrl(authentication);
	    redirectStrategy.sendRedirect(request, response, targetUrl);
	}
  
  protected String determineTargetUrl(final Authentication authentication) {

	    Map<String, String> roleTargetUrlMap = new HashMap<>();
	    roleTargetUrlMap.put("MENTOR", "/hello");
	    roleTargetUrlMap.put("MENTORIZADO", "/hello2");

	    final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
	    for (final GrantedAuthority grantedAuthority : authorities) {
	        String authorityName = grantedAuthority.getAuthority();
	        if(roleTargetUrlMap.containsKey(authorityName)) {
	            return roleTargetUrlMap.get(authorityName);
	        }
	    }

	    throw new IllegalStateException();
	}
  
  protected void clearAuthenticationAttributes(HttpServletRequest request, Authentication authentication) {
	    HttpSession session = request.getSession(false);
	    if (session == null) {
	        return;
	    }
	    UsuarioLogeado user = new UsuarioLogeado(authentication.getName(), usuariosActivos);
      session.setAttribute("user", user);
	    session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	}
  
  
}
