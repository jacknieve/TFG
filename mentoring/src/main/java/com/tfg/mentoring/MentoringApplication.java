package com.tfg.mentoring;

import java.io.File;
import java.text.SimpleDateFormat;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MentoringApplication {

	@Bean
	public ModelMapper modelMaper() {
		return new ModelMapper();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Bean
	public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }
	
	public static void main(String[] args) {
		try {
			File folder = new File("recursos/");
			if(!folder.exists()) {
				if(folder.mkdir()) {
					System.out.println("Creado fichero");
					System.out.println(folder.getAbsolutePath());
				}
				else {
					System.out.println("No se ha podido crear el directorio de recursos");
					System.exit(-1);
				}
			}
			folder = new File("recursos/user-files/");
			if(!folder.exists()) {
				if(folder.mkdir()) {
					System.out.println("Creado fichero");
					System.out.println(folder.getAbsolutePath());
				}
				else {
					System.out.println("No se ha podido crear el directorio de ficheros de los usuarios");
					System.exit(-1);
				}
				
			}
			folder = new File("recursos/user-files/mentores/");
			if(!folder.exists()) {
				if(folder.mkdir()) {
					System.out.println("Creado fichero");
					System.out.println(folder.getAbsolutePath());
				}
				else {
					System.out.println("No se ha podido crear el directorio de ficheros de mentores");
					System.exit(-1);
				}
			}
			folder = new File("recursos/user-files/mentorizados/");
			if(!folder.exists()) {
				if(folder.mkdir()) {
					System.out.println("Creado fichero");
					System.out.println(folder.getAbsolutePath());
				}
				else {
					System.out.println("No se ha podido crear el directorio de ficheros de mentorizados");
					System.exit(-1);
				}
			}
			folder = new File("recursos/user-files/instituciones/");
			if(!folder.exists()) {
				if(folder.mkdir()) {
					System.out.println("Creado fichero");
					System.out.println(folder.getAbsolutePath());
				}
				else {
					System.out.println("No se ha podido crear el directorio de ficheros de instituciones");
					System.exit(-1);
				}
			}
		} catch (SecurityException | NullPointerException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
			// TODO: handle exception
		}
		
		SpringApplication.run(MentoringApplication.class, args);
	}

}
