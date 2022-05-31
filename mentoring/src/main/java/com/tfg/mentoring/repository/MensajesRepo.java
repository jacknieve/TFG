package com.tfg.mentoring.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.tfg.mentoring.model.MensajeChat;
import com.tfg.mentoring.model.Ids.MensajeChatId;

public interface MensajesRepo extends JpaRepository<MensajeChat, MensajeChatId>{

	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE mensajes SET estado = 1 WHERE id_sala = ?1 AND dementor = ?2 AND estado = 0")
	void actualizarEstadoMensajes(long id, boolean deMentor);
	
	/*@Query(nativeQuery = true, value="SELECT DISTINCT id_mentorizado FROM mensajes WHERE id_mentor = ?1 AND estado = 0 AND dementor = false")
	List<String> nuevosMentor(String mentor);
	
	@Query(nativeQuery = true, value="SELECT DISTINCT id_mentor FROM mensajes WHERE id_mentorizado = ?1 AND estado = 0 AND dementor = true")
	List<String> nuevosMentorizado(String mentorizado);*/
	
	@Query(nativeQuery = true, value="SELECT * FROM mensajes WHERE id_sala = ?1 ORDER BY fecha_envio")
	List<MensajeChat> findBySala(long id);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE mensajes SET contenido = ?1, detexto = ?2 WHERE id_sala = ?3 AND dementor = ?4 AND detexto = ?5 AND contenido = ?6")
	void actualizarFileChat(String contenido, boolean detexto, long id, boolean mentor, boolean eraDeTexto, String contenidoOld);
	
	/*@Transactional
	@Modifying
	@Query(nativeQuery = true, value="INSERT INTO mensajes (contenido, dementor, estado, fecha_envio, id_mentor, id_mentorizado, inicio) VALUES (?1,?2,?3,current_timestamp,?4,?5,?6)")
	void insertarMensaje(String contenido, boolean deMentor, int estado, String mentor, String mentorizado, Timestamp inicio);*/
	
}
