package com.example.prototipoRegistro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.prototipoRegistro.model.NotificacionInv;

public interface NotInvRepo extends JpaRepository<NotificacionInv, Long>{
	List<NotificacionInv> findByUsuario(Long usuario);
	
	@Query(nativeQuery = true, value="SELECT * FROM notificaciones WHERE id_user = ?1")
	List<NotificacionInv> getNotificaciosUser(Long id);
	
	/*@Query(nativeQuery = true, value="SELECT u.id AS iduser, u.username AS username, u.mentor AS mentor, n.descripcion AS descripcion, "
			+ "n.estado AS estado, n.fechaenv AS fechaenv, n.fechaeliminacion AS fechaeliminacion"
			+ "FROM usuarios AS u LEFT JOIN notificaciones AS n ON u.id=n.id_user WHERE u.id = ?1")*/
}
