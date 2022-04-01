package com.example.prototipoRegistro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.prototipoRegistro.model.Usuario;

public interface DemoRepo extends JpaRepository<Usuario, String>{
	List<Usuario> findByMentor(boolean mentor);
	List<Usuario> findByArea(String area);
	Usuario findByUsername(String nombre);
	Usuario findById(Long id);
}
