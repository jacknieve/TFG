package com.tfg.mentoring.model.auxiliar.requests;

//Clase que se encarga de almacenar el id de una notificacion cuando este se emvia en el RequestBody, para ser eliminada
public class IdNotificacion {

private Long id;
	
	public IdNotificacion(Long id) {
		this.id=id;
	}
	public IdNotificacion() {
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
