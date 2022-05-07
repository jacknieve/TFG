package com.tfg.mentoring.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.tfg.mentoring.model.Usuario;


//Excepciones comunes Hibernate:
//https://www.baeldung.com/hibernate-exceptions

public interface UsuarioRepo extends JpaRepository<Usuario, String>{
	Usuario findByUsername(String nombre);
	//Usuario findById(Long id);
	
	@Query(nativeQuery = true, value="SELECT * FROM usuarios WHERE verification_code = ?1")
	public Usuario findByVerificationCode(String code);
	
	//Todas estas se les podia quitar esta etiqueta y que devolviesen un entero para comprobar que se ha actualizado
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="UPDATE usuarios SET enable = false WHERE username = ?1")
	void borrarUsuario(String username);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="DELETE FROM usuarios WHERE username = ?1")
	void limpiarUsuario(String username);
}
