package com.tfg.mentoring.model.auxiliar;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class UsuariosActivos {

	public List<String> users;

    public UsuariosActivos() {
        users = new ArrayList<String>();
    }

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}
}
