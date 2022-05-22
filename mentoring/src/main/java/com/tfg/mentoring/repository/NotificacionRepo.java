package com.tfg.mentoring.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.tfg.mentoring.model.Notificacion;

public interface NotificacionRepo extends JpaRepository<Notificacion, String>{
	//Guardo aqui esto porque el fecha eliminacion si que serviria para cosas que se van a actualizar desde "fuera del usuario"
	//@Query(nativeQuery = true, value="SELECT * FROM notificaciones WHERE fechaenv >= ?1 OR fechaeliminacion >= ?1")
	
	@Query(nativeQuery = true, value="SELECT * FROM notificaciones WHERE id_user = ?1 AND estado <> 2 ORDER BY fechaenv DESC")
	List<Notificacion> getNotificaciosUser(String user);
	
	@Modifying
	@Query(nativeQuery = true, value="UPDATE notificaciones SET estado = 1 WHERE id_user = ?1 AND estado = 0")
	void actualizaEstadoNotificaciosUser(String user);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE notificaciones SET estado = 2 WHERE id = ?1")
	void borrarNotificacion(Long id);
	
	@Query(nativeQuery = true, value="SELECT * FROM notificaciones WHERE id_user = ?1 AND estado = 0")
	List<Notificacion> getNews(String user);
	
	@Query(nativeQuery = true, value="SELECT * FROM notificaciones WHERE id_user = ?1 AND motivo = 4 AND estado = 0")
	List<Notificacion> notificacionesMensajes(String user);

}
