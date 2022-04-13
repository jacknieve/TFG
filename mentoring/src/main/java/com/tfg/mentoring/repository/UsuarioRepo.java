package com.tfg.mentoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.mentoring.model.Usuario;


public interface UsuarioRepo extends JpaRepository<Usuario, String>{
	Usuario findByUsername(String nombre);
	Usuario findById(Long id);
}
