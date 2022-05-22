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
	
	public void entrarChat(String usuario) {
		activos.getUsers().get(usuario).setEnChat(true);
	}
	
	public void salirChat(String usuario) {
		activos.getUsers().get(usuario).setEnChat(false);
	}
}
