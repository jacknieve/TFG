package com.tfg.mentoring.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.tfg.mentoring.model.Mentor;

public interface MentorRepo extends JpaRepository<Mentor, String>{

	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="DELETE FROM area_mentor WHERE correo = ?1 AND area = ?2")
	void borrarArea(String username, String area);
	
	
	//Aqui habria que probar lo de https://www.javafixing.com/2021/10/fixed-spring-data-optional-parameter-in.html
	//Aqui tambien tal vez hacerlo en otro mapeado con menos datos puesto que no necesitamos todos
	@Query(nativeQuery = true, value="SELECT m.* FROM usuarios u, mentores m, area_mentor a WHERE m.usuario_mentor = u.username AND u.enable = true AND u.unlocked = true AND m.usuario_mentor = a.correo AND a.area = ?1 AND m.institucion = ?2 AND m.horaspormes >= ?3")
	List<Mentor> buscarCompleto(String area, String institucion, float horas);
	
	@Query(nativeQuery = true, value="SELECT m.* FROM usuarios u, mentores m, area_mentor a WHERE m.usuario_mentor = u.username AND u.enable = true AND u.unlocked = true AND m.usuario_mentor = a.correo AND a.area = ?1 AND m.institucion = ?2")
	List<Mentor> buscarAreaInstitucion(String area, String institucion);
	
	@Query(nativeQuery = true, value="SELECT m.* FROM usuarios u, mentores m, area_mentor a WHERE m.usuario_mentor = u.username AND u.enable = true AND u.unlocked = true AND m.usuario_mentor = a.correo AND a.area = ?1 AND m.horaspormes >= ?2")
	List<Mentor> buscarAreaHoras(String area, float horas);
	
	@Query(nativeQuery = true, value="SELECT m.* FROM usuarios u, mentores m, area_mentor a WHERE m.usuario_mentor = u.username AND u.enable = true AND u.unlocked = true AND m.usuario_mentor = a.correo AND a.area = ?1")
	List<Mentor> buscarArea(String area);
	
	@Query(nativeQuery = true, value="SELECT m.* FROM usuarios u, mentores m WHERE m.usuario_mentor = u.username AND u.enable = true AND u.unlocked = true AND m.institucion = ?1")
	List<Mentor> buscarInstitucion(String institucion);
	
	@Query(nativeQuery = true, value="SELECT m.* FROM usuarios u, mentores m WHERE m.usuario_mentor = u.username AND u.enable = true AND u.unlocked = true AND m.horaspormes >= ?1")
	List<Mentor> buscarHoras(float horas);
	
	@Query(nativeQuery = true, value="SELECT m.* FROM usuarios u, mentores m WHERE m.usuario_mentor = u.username AND u.enable = true AND u.unlocked = true AND m.institucion = ?1 AND m.horaspormes >= ?2")
	List<Mentor> buscarInstitucionHoras(String institucion, float horas);
	
	@Query(nativeQuery = true, value="SELECT m.* FROM usuarios u, mentores m WHERE m.usuario_mentor = u.username AND u.enable = true AND u.unlocked = true")
	List<Mentor> buscarTodos();
	
	
}
