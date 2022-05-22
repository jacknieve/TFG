package com.tfg.mentoring.model.auxiliar;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class UsuariosActivos {

	public Map<String,InfoUsuarioActivo> users;

    public UsuariosActivos() {
        users = new HashMap<String,InfoUsuarioActivo>();
    }

	public Map<String,InfoUsuarioActivo> getUsers() {
		return users;
	}

	public void setUsers(Map<String,InfoUsuarioActivo>users) {
		this.users = users;
	}
}
