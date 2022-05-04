package com.tfg.mentoring.service.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.tfg.mentoring.model.Institucion;
import com.tfg.mentoring.repository.AreasRepo;
import com.tfg.mentoring.repository.EstudiosRepo;
import com.tfg.mentoring.repository.InstitucionRepo;

@Component
public class AppReadyListener implements ApplicationListener<ApplicationReadyEvent>{

	@Autowired
	private ListLoad listas;
	@Autowired
	private AreasRepo arepo;
	@Autowired
	private EstudiosRepo erepo;
	@Autowired
	private InstitucionRepo irepo;
	
	@Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
		listas.setAreas(arepo.findAll());
		listas.setEstudios(erepo.findAll());
		List<Institucion> instituciones = irepo.findAll();
		ArrayList<String> _instituciones = new ArrayList<>();
		for(Institucion i : instituciones) {
			_instituciones.add(i.getNombre());
			//System.out.println(i.toString());
		}
		
		//Aqui mejor obtener solo la lista de instituciones
		//Map<String,Institucion> mapaInstituciones = instituciones.stream().collect(Collectors.toMap(Institucion::getNombre, Function.identity()));
		listas.setInstituciones(_instituciones);
		
		//listas.getEstudios().add("kakota");
        // code here
    }
	
	
	
}
