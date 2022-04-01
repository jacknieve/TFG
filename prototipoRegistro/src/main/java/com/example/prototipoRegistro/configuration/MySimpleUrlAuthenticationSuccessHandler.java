package com.example.prototipoRegistro.configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class MySimpleUrlAuthenticationSuccessHandler
implements AuthenticationSuccessHandler {

//https://www.baeldung.com/spring_redirect_after_login
  private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

  //Anulamos este método
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, 
		  HttpServletResponse response, Authentication authentication) throws IOException {

      handle(request, response, authentication);
      clearAuthenticationAttributes(request);
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
  
  protected void clearAuthenticationAttributes(HttpServletRequest request) {
	    HttpSession session = request.getSession(false);
	    if (session == null) {
	        return;
	    }
	    session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	}
  
  
}