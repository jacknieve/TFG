package com.tfg.mentoring.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tfg.mentoring.model.Mentorizacion;
import com.tfg.mentoring.model.MentorizacionId;

public interface MentorizacionRepo extends JpaRepository<Mentorizacion, MentorizacionId>{

	@Query(nativeQuery = true, value="SELECT * FROM mentorizaciones WHERE mentor_usuario_mentor = ?1 AND mentorizado_usuario_username = ?2 AND fecha_fin is null")
	List<Mentorizacion> comprobarSiHayMentorizacion(String mentor, String mentorizado);
	
	@Query(nativeQuery = true, value="SELECT * FROM mentorizaciones WHERE mentor_usuario_mentor = ?1 AND fecha_fin is null")
	List<Mentorizacion> obtenerMentorizacionesMentor(String mentor);
	
	@Query(nativeQuery = true, value="SELECT * FROM mentorizaciones WHERE mentorizado_usuario_username = ?1 AND fecha_fin is null")
	List<Mentorizacion> obtenerMentorizacionesMentorizado(String mentorizado);
	
	@Query(nativeQuery = true, value="SELECT * FROM mentorizaciones WHERE mentor_usuario_mentor = ?1 AND (creada_en >= ?2 OR fecha_fin >= ?2)")
	List<Mentorizacion> getNuevasMentor(String mentor, Timestamp date);
	
	@Query(nativeQuery = true, value="SELECT * FROM mentorizaciones WHERE mentorizado_usuario_username = ?1 AND (creada_en >= ?2 OR fecha_fin >= ?2)")
	List<Mentorizacion> getNuevasMentorizado(String mentorizado, Timestamp date);
}
