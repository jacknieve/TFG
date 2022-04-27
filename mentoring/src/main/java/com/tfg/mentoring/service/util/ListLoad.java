package com.tfg.mentoring.service.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tfg.mentoring.model.AreaConocimiento;
import com.tfg.mentoring.model.NivelEstudios;
import com.tfg.mentoring.model.Puesto;

@Component
public class ListLoad {
	
	//@Autowired
	//private AreasRepo arepo;
	//Esto quizas para traer los nombres de las instituciones
	//https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#projections
	//@Autowired
	//private InstitucionRepo irepo;
	//@Autowired
	//private ModelMapper modelMapper;

	private List<Puesto> puestos;
	private List<NivelEstudios> estudios;
	private List<AreaConocimiento> areas;
	private ArrayList<String> instituciones;
	
	public ListLoad() {
		//To Do
		//Aqui cambiar esto por una lectura de un fichero JSON o de la base de datos
		puestos = new ArrayList<>();
		/*puestos.add("Becario");
		puestos.add("Profesor");
		puestos.add("Estudiante");
		puestos.add("Empleado");
		puestos.add("Jefe");
		puestos.add("Otro");*/
		estudios = new ArrayList<>();
		/*estudios.add("Basica");
		estudios.add("Secundaria");
		estudios.add("Tecnico");
		estudios.add("Bachillerato");
		estudios.add("Graduado");
		estudios.add("Master");
		estudios.add("Doctorado");
		estudios.add("Otros");*/
		//this.areas = arepo.findAll();
	}

	public List<Puesto> getPuestos() {
		return puestos;
	}

	public List<NivelEstudios> getEstudios() {
		return estudios;
	}
	
	public List<AreaConocimiento> getAreas(){
		return areas;
	}

	public void setAreas(List<AreaConocimiento> areas) {
		this.areas = areas;
	}

	public void setPuestos(List<Puesto> list) {
		this.puestos = list;
	}

	public void setEstudios(List<NivelEstudios> estudios) {
		this.estudios = estudios;
	}

	public ArrayList<String> getInstituciones() {
		return instituciones;
	}

	public void setInstituciones(ArrayList<String> instituciones) {
		this.instituciones = instituciones;
	}

	
	
	
	
	
	
	
}
