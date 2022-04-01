package com.example.prototipoRegistro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.prototipoRegistro.model.Notificacion;

public interface NotificacionRepo extends JpaRepository<Notificacion, Long>{
	List<Notificacion> findByUsuario(Long usuario);
}
