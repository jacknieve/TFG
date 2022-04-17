package com.tfg.mentoring.model.auxiliar;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class Listas {
	private ArrayList<String> puestos;
	private ArrayList<String> estudios;
	
	public Listas() {
		//To Do
		//Aqui cambiar esto por una lectura de un fichero JSON o de la base de datos
		puestos = new ArrayList<>();
		puestos.add("Becario");
		puestos.add("Profesor");
		puestos.add("Estudiante");
		puestos.add("Empleado");
		puestos.add("Jefe");
		puestos.add("Otro");
		estudios = new ArrayList<>();
		estudios.add("Basica");
		estudios.add("Secundaria");
		estudios.add("Tecnico");
		estudios.add("Bachillerato");
		estudios.add("Graduado");
		estudios.add("Master");
		estudios.add("Doctorado");
		estudios.add("Otros");
	}

	public ArrayList<String> getPuestos() {
		return puestos;
	}

	public ArrayList<String> getEstudios() {
		return estudios;
	}
	
	

}
