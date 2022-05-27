package com.tfg.mentoring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tfg.mentoring.model.auxiliar.UsuariosActivos;

@Service
public class ActiveUsersService {

	@Autowired
	UsuariosActivos activos;
	
	public boolean activo(String usuario) {
		return activos.getUsers().containsKey(usuario);
	}
	
	public boolean enChat(String usuario) {
		return activos.getUsers().get(usuario).isEnChat();
	}
	
	public boolean enNotificacion(String usuario) {
		return activos.getUsers().get(usuario).isEnNotificacion();
	}
	
	public void entrarChat(String usuario) {
		activos.getUsers().get(usuario).setEnChat(true);
	}
	
	public void salirChat(String usuario) {
		activos.getUsers().get(usuario).setEnChat(false);
	}
	
	public void entrarNotificacion(String usuario) {
		activos.getUsers().get(usuario).setEnNotificacion(true);
	}
	
	public void salirNotificacion(String usuario) {
		activos.getUsers().get(usuario).setEnNotificacion(false);
	}
}
