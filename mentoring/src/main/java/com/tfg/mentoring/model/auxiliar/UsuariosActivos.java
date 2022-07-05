package com.tfg.mentoring.model.auxiliar;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class UsuariosActivos {

	public Map<String,InfoUsuarioActivo> users;
	Map<String, String> roleTargetUrlMap;
	
    public UsuariosActivos() {
        users = new HashMap<String,InfoUsuarioActivo>();
        roleTargetUrlMap = new HashMap<>();
	    roleTargetUrlMap.put("MENTOR", "/user/principal");
	    roleTargetUrlMap.put("MENTORIZADO", "/user/principal");
    }

	public Map<String,InfoUsuarioActivo> getUsers() {
		return users;
	}

	public void setUsers(Map<String,InfoUsuarioActivo>users) {
		this.users = users;
	}

	public Map<String, String> getRoleTargetUrlMap() {
		return roleTargetUrlMap;
	}
	
	
}
