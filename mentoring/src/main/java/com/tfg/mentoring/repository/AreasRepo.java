package com.tfg.mentoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.mentoring.model.AreaConocimiento;

@Repository
public interface AreasRepo extends JpaRepository<AreaConocimiento, String>{

}
