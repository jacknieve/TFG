package com.tfg.mentoring.model.auxiliar;

import java.util.List;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.springframework.stereotype.Component;


@Component
public class UsuarioLogeado implements HttpSessionBindingListener{

	 private String username; 
	    private UsuariosActivos usuariosActivos;
	    
	    public UsuarioLogeado(String username, UsuariosActivos usuariosActivos) {
	        this.username = username;
	        this.usuariosActivos = usuariosActivos;
	    }
	    
	    public UsuarioLogeado() {}

	    @Override
	    public void valueBound(HttpSessionBindingEvent event) {
	        List<String> users = usuariosActivos.getUsers();
	        UsuarioLogeado user = (UsuarioLogeado) event.getValue();
	        if (!users.contains(user.getUsername())) {
	        	System.out.println("Añadido usuario");
	            users.add(user.getUsername());
	        }
	    }

	    @Override
	    public void valueUnbound(HttpSessionBindingEvent event) {
	        List<String> users = usuariosActivos.getUsers();
	        UsuarioLogeado user = (UsuarioLogeado) event.getValue();
	        if (users.contains(user.getUsername())) {
	        	System.out.println("Quitado usuario");
	            users.remove(user.getUsername());
	        }
	    }

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public UsuariosActivos getUsuariosActivos() {
			return usuariosActivos;
		}

		public void setUsuariosActivos(UsuariosActivos usuariosActivos) {
			this.usuariosActivos = usuariosActivos;
		}
}
