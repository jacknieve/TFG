package com.tfg.mentoring.repository;

import java.sql.Timestamp;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
	
	@Query(nativeQuery = true, value="SELECT * FROM mentorizaciones WHERE mentorizado_usuario_username = ?1 AND fecha_fin is not null AND calificacion is null")
	List<Mentorizacion> obtenerMentorizacionesPorPuntuar(String mentorizado);
	
	@Query(nativeQuery = true, value="SELECT * FROM mentorizaciones WHERE mentorizado_usuario_username = ?1 AND fecha_fin >= ?2 AND calificacion is null")
	List<Mentorizacion> obtenerMentorizacionesPorPuntuarNuevas(String mentorizado, Timestamp date);
	
	@Query(nativeQuery = true, value="SELECT * FROM mentorizaciones WHERE mentor_usuario_mentor = ?1 AND (creada_en >= ?2 OR fecha_fin >= ?2)")
	List<Mentorizacion> getNuevasMentor(String mentor, Timestamp date);
	
	@Query(nativeQuery = true, value="SELECT * FROM mentorizaciones WHERE mentorizado_usuario_username = ?1 AND (creada_en >= ?2 OR fecha_fin >= ?2)")
	List<Mentorizacion> getNuevasMentorizado(String mentorizado, Timestamp date);
	
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE mentorizaciones SET fecha_fin = current_timestamp, calificacion = ?1, comentario = ?2  WHERE mentor_usuario_mentor = ?3 AND mentorizado_usuario_username = ?4 AND fecha_fin is null")
	void cerrarPuntuarMentorizacion(int calificacion, String comentario, String mentor, String mentorizado);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE mentorizaciones SET calificacion = ?1, comentario = ?2  WHERE mentor_usuario_mentor = ?3 AND mentorizado_usuario_username = ?4 AND fecha_fin = ?5")
	void puntuarMentorizacion(int calificacion, String comentario, String mentor, String mentorizado, Timestamp date);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE mentorizaciones SET fecha_fin = ?3 WHERE mentor_usuario_mentor = ?1 AND mentorizado_usuario_username = ?2 AND fecha_fin is null")
	void cerrarMentorizacion(String mentor, String mentorizado, Timestamp date);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE mentorizaciones SET fase = ?3 WHERE mentor_usuario_mentor = ?1 AND mentorizado_usuario_username = ?2 AND fecha_fin is null")
	void cambiarFase(String mentor, String mentorizado, int fase);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE mentorizaciones SET fecha_fin = current_timestamp WHERE mentor_usuario_mentor = ?1 AND fecha_fin is null")
	void borrarMentorizacionesMentor(String mentor);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE mentorizaciones SET fecha_fin = current_timestamp, calificacion = -1 WHERE mentorizado_usuario_username = ?1 AND fecha_fin is null")
	void borrarMentorizacionesMentorizado(String mentorizado);
	
}
