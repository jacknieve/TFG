package com.example.prototipoRegistro.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.prototipoRegistro.model.Notificacion;

public interface NotificacionRepo extends JpaRepository<Notificacion, Long>{
	List<Notificacion> findByUsuario(Long usuario);
	
	@Query(nativeQuery = true, value="SELECT * FROM notificaciones WHERE fechaenv >= ?1 OR fechaeliminacion >= ?1")
	List<Notificacion> getNews(Timestamp date);
}
