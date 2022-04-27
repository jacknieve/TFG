package com.tfg.mentoring.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.tfg.mentoring.model.Peticion;
import com.tfg.mentoring.model.PeticionId;

public interface PeticionRepo extends JpaRepository<Peticion, PeticionId>{

	@Query(nativeQuery = true, value="SELECT * FROM peticiones WHERE mentor_usuario_mentor = ?1 AND mentorizado_usuario_username = ?2 AND estado <= 1")
	List<Peticion> comprobarPeticion(String mentor, String mentorizado);
	
	@Query(nativeQuery = true, value="SELECT * FROM peticiones WHERE mentor_usuario_mentor = ?1 AND estado <= 1")
	List<Peticion> obtenerPeticiones(String mentor);
	
	@Query(nativeQuery = true, value="SELECT * FROM peticiones WHERE mentor_usuario_mentor = ?1 AND estado = 0")
	List<Peticion> getNews(String user);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE peticiones SET estado = 2, fecha_resolucion = current_timestamp WHERE mentor_usuario_mentor = ?1 AND mentorizado_usuario_username = ?2 AND estado = 1")
	void aceptarPeticion(String mentor, String mentorizado);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE peticiones SET estado = 3, fecha_resolucion = current_timestamp WHERE mentor_usuario_mentor = ?1 AND mentorizado_usuario_username = ?2 AND estado = 1")
	void rechazarPeticion(String mentor, String mentorizado);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE peticiones SET fecha_resolucion = current_timestamp, estado = 3 WHERE mentor_usuario_mentor = ?1 AND fecha_resolucion is null")
	void borrarPeticionesMentor(String mentor);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE peticiones SET fecha_resolucion = current_timestamp, estado = 3 WHERE mentorizado_usuario_username = ?1 AND fecha_resolucion is null")
	void borrarPeticionesMentorizado(String mentorizado);
}
