package com.tfg.mentoring.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.tfg.mentoring.model.Fichero;
import com.tfg.mentoring.model.Ids.FicheroId;

public interface FicheroRepo extends JpaRepository<Fichero, FicheroId>{

	@Query(nativeQuery = true, value="SELECT * FROM ficheros WHERE user_username = ?1")
	List<Fichero> findByUser(String username);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="DELETE FROM ficheros WHERE user_username = ?1 AND nombre = ?2")
	void limpiarFichero(String username, String filename);
}
