package com.tfg.mentoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.mentoring.model.Mentor;

public interface MentorRepo extends JpaRepository<Mentor, String>{

}
