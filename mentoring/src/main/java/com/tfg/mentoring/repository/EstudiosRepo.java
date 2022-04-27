package com.tfg.mentoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.mentoring.model.NivelEstudios;

public interface EstudiosRepo extends JpaRepository<NivelEstudios, String>{

}
