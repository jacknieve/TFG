package com.tfg.mentoring.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.Param;

import com.tfg.mentoring.model.Mentor;

public interface MentorRepo extends JpaRepository<Mentor, String>{

	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="DELETE FROM area_mentor WHERE correo = ?1 AND area = ?2")
	void borrarArea(String username, String area);
	
	Optional<Mentor> findByUsuarioUsernameAndUsuarioEnable(String username, boolean enable);
	
	
	//Prototipo de busqueda, me trae todo por culpa de las areas
	//Se podria probar a añadir un area por defecto que tenga todos, asi no hay que hacer la comprobacion de null, y quizas funcionaria
	/*@Query(nativeQuery = true, value="SELECT DISTINCT m.* FROM usuarios u, mentores m, area_mentor a WHERE m.usuario_mentor = u.username AND u.enable = true AND u.unlocked = true AND (:i is null or m.institucion = cast(:i AS text)) AND m.horaspormes >=:h AND (:a is null or a.area =cast(:a AS text))")
	List<Mentor> buscarPrototipo(@Param("i") String institucion, @Param("h") float horas, @Param("a") String area);*/

	@Query(nativeQuery = true, value="SELECT DISTINCT m.* FROM usuarios u, mentores m, area_mentor a WHERE m.usuario_mentor = u.username AND u.enable = true AND u.unlocked = true AND (:i is null or m.institucion = cast(:i AS text)) AND m.horaspormes >=:h AND m.usuario_mentor = a.correo AND (:a is null or a.area =cast(:a AS text))")
	List<Mentor> buscarPrototipo(@Param("i") String institucion, @Param("h") float horas, @Param("a") String area);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE mentores SET feliminacion = current_timestamp WHERE usuario_mentor = ?1 ")
	void borrarMentor(String username);
	
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="DELETE FROM mentores WHERE usuario_mentor = ?1")
	void limpiarUsuario(String username);
	
	
}
