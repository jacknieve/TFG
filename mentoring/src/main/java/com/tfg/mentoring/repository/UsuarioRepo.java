package com.tfg.mentoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tfg.mentoring.model.Usuario;


public interface UsuarioRepo extends JpaRepository<Usuario, String>{
	Usuario findByUsername(String nombre);
	//Usuario findById(Long id);
	
	@Query(nativeQuery = true, value="SELECT * FROM usuarios WHERE verification_code = ?1")
	public Usuario findByVerificationCode(String code);
}
