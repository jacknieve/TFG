package com.tfg.mentoring.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.mentoring.model.Institucion;


public interface InstitucionRepo extends JpaRepository<Institucion, String>{
	ArrayList<Institucion> findByNombre(String nombre);
	
	
	
}
