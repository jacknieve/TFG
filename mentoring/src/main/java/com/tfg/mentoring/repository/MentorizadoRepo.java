package com.tfg.mentoring.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.tfg.mentoring.model.Mentorizado;

public interface MentorizadoRepo extends JpaRepository<Mentorizado, String>{
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="DELETE FROM area_mentorizado WHERE correo = ?1 AND area = ?2")
	void borrarArea(String username, String area);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE mentorizados SET feliminacion = current_timestamp WHERE usuario_username = ?1 ")
	void borrarMentorizado(String username);
	
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="DELETE FROM mentorizados WHERE usuario_username = ?1")
	void limpiarUsuario(String username);

}
