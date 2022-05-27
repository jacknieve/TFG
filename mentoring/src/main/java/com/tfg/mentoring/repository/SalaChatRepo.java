package com.tfg.mentoring.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.tfg.mentoring.model.SalaChat;
import com.tfg.mentoring.model.Ids.SalaChatId;

public interface SalaChatRepo extends JpaRepository<SalaChat, SalaChatId>{

	//Este metodo poner una query nuestra, para que traiga el ultimo chat abierto entre ambos
	//Ademas, así ya sabremos cual es el nombre que le ha puesto a las columna sen la DB
	@Query(nativeQuery = true, value="SELECT * FROM salaschat WHERE mentor_usuario_mentor = ?1 AND mentorizado_usuario_username = ?2 AND fecha_cierre is null")
	Optional<SalaChat> findByMentorAndMentorizado(String mentor, String mentorizado);
	
	@Query(nativeQuery = true, value="SELECT id_sala FROM salaschat WHERE mentor_usuario_mentor = ?1 AND mentorizado_usuario_username = ?2 AND fecha_cierre is null")
	Optional<Long> getIdByMentorAndMentorizado(String mentor, String mentorizado);
	
	/*@Query(nativeQuery = true, value="SELECT fecha_inicio FROM salaschat WHERE mentor_usuario_mentor = ?1 AND mentorizado_usuario_username = ?2 AND fecha_cierre is null")
	Optional<Timestamp> obtenerInicioUltimo(String mentor, String mentorizado);*/
	
	@Query(nativeQuery = true, value="SELECT * FROM salaschat WHERE mentor_usuario_mentor = ?1 AND fecha_cierre is null")
	List<SalaChat> findByMentor(String mentor);
	
	@Query(nativeQuery = true, value="SELECT * FROM salaschat WHERE mentorizado_usuario_username = ?1 AND fecha_cierre is null")
	List<SalaChat> findByMentorizado(String mentorizado);
	
	@Query(nativeQuery = true, value="SELECT DISTINCT s.id_sala FROM salaschat s, mensajes m WHERE s.mentor_usuario_mentor = ?1 AND s.fecha_cierre is null AND s.id_sala = m.id_sala AND m.estado = 0 AND m.dementor = false")
	List<Long> nuevosMentor(String mentor);
	
	@Query(nativeQuery = true, value="SELECT DISTINCT s.id_sala FROM salaschat s, mensajes m WHERE s.mentorizado_usuario_username = ?1 AND s.fecha_cierre is null AND s.id_sala = m.id_sala AND m.estado = 0 AND m.dementor = true")
	List<Long> nuevosMentorizado(String mentorizado);
	
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE salaschat SET fecha_cierre = current_timestamp WHERE mentor_usuario_mentor = ?1 AND mentorizado_usuario_username = ?2 AND fecha_cierre is null")
	void salirChat(String mentor, String mentorizado);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE salaschat SET fecha_cierre = current_timestamp WHERE mentor_usuario_mentor = ?1 AND fecha_cierre is null")
	void salirTodosChatsMentor(String username);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE salaschat SET fecha_cierre = current_timestamp WHERE mentorizado_usuario_username = ?1 AND fecha_cierre is null")
	void salirTodosChatsMentorizado(String username);
	
	
}
