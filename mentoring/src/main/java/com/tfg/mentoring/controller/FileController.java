package com.tfg.mentoring.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tfg.mentoring.exceptions.ExcepcionDB;
import com.tfg.mentoring.exceptions.ExcepcionFichero;
import com.tfg.mentoring.exceptions.ExcepcionFileNotFound;
import com.tfg.mentoring.exceptions.ExcepcionRecursos;
import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.service.FileService;

@RestController
@RequestMapping("/file")
public class FileController {

	@Autowired
	private FileService fservice;

	@PostMapping("/fotoperfil")
	public ResponseEntity<MensajeError> setFotoPerfil(@RequestParam("imagen") MultipartFile image,
			@AuthenticationPrincipal UserAuth u) {
		System.out.println("Subiendo imagen");
		System.out.println(image);
		if (image == null) {// Esta comprobacion es innecesaria, habria que tratar en el frontend el error
							// 400 sin mensaje
			return new ResponseEntity<>(
					new MensajeError("Fallo en la peticion", "No se ha seleccionado ninguna imagen."),
					HttpStatus.BAD_REQUEST);
		}
		try {
			String path = "";
			switch (u.getRol()) {
			case MENTOR:
				path = fservice.guardarImagen(image, u.getUsername(), "mentores");
				break;
			case MENTORIZADO:
				path = fservice.guardarImagen(image, u.getUsername(), "mentorizados");
				break;
			case INSTITUCION:
				path = fservice.guardarImagen(image, u.getUsername(), "instituciones");
				break;

			default:
				return new ResponseEntity<>(new MensajeError("Sin autorización", "No tienes permiso para hacer esto"),
						HttpStatus.UNAUTHORIZED);
			}
			return new ResponseEntity<>(new MensajeError(path, ""), HttpStatus.OK);

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			// Aqui faltaria limpiar la extension en la DB
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (IOException | SecurityException e) {
			fservice.clearImage(u.getUsername());
			e.printStackTrace();
			return new ResponseEntity<>(new MensajeError("Fallo al almacenar la imagen",
					"Se ha producido un fallo al tratar de almacenar la imagen."), HttpStatus.BAD_REQUEST);
		} catch (ExcepcionFichero e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError(e.getTitulo(), e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping("/uploadfile")
	public ResponseEntity<MensajeError> subirFichero(@RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal UserAuth u) {
		System.out.println("Subiendo fichero");
		//System.out.println(file);
		if (file == null) {
			return new ResponseEntity<>(
					new MensajeError("Fallo en la peticion", "No se ha seleccionado ningún fichero."),
					HttpStatus.BAD_REQUEST);
		}
		try {
			String nombre = "";
			switch (u.getRol()) {
			case MENTOR:
				nombre = fservice.guardarFichero(file, u.getUsername(), "mentores");
				break;
			case MENTORIZADO:
				nombre = fservice.guardarFichero(file, u.getUsername(), "mentorizados");
				break;
			case INSTITUCION:
				nombre = fservice.guardarFichero(file, u.getUsername(), "instituciones");
				break;

			default:
				return new ResponseEntity<>(new MensajeError("Sin autorización", "No tienes permiso para hacer esto"),
						HttpStatus.UNAUTHORIZED);
			}
			return new ResponseEntity<>(new MensajeError(nombre, ""), HttpStatus.OK);

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			// Aqui faltaria limpiar el fichero en la DB
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
			fservice.clearFile(u.getUsername(), file.getOriginalFilename());
			return new ResponseEntity<>(new MensajeError("Fallo al almacenar el fichero",
					"Se ha producido un fallo al tratar de almacenar el fichero."), HttpStatus.BAD_REQUEST);
		} catch (ExcepcionFichero e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError(e.getTitulo(), e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fichero vacío", e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (ExcepcionDB e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Usuario no encontrado", e.getMessage()),
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) { // Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	// https://o7planning.org/11673/spring-boot-file-upload-with-angularjs
	@GetMapping("/download/mentor/{username}/{filename}")
	public ResponseEntity<Resource> descargarFicheroMentor(@PathVariable("username") String username,
			@PathVariable("filename") String filename) throws MalformedURLException, ExcepcionFileNotFound  {
		try {
			System.out.println(username + " " + filename);
			Resource recurso = fservice.getFile("recursos/user-files/mentores/" + username + "/perfil/" + filename);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(recurso);
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFileNotFound(filename);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/download/mentorizado/{username}/{filename}")
	public ResponseEntity<Resource> descargarFicheroMentorizado(@PathVariable("username") String username,
			@PathVariable("filename") String filename) throws MalformedURLException, ExcepcionFileNotFound {
		try {
			System.out.println(username + " " + filename);
			Resource recurso = fservice.getFile("recursos/user-files/mentorizados/" + username + "/perfil/" + filename);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(recurso);
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFileNotFound(filename);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/download/my/{filename}")
	public ResponseEntity<Resource> descargarMiFichero(@PathVariable("filename") String filename,
			@AuthenticationPrincipal UserAuth u) throws ExcepcionFileNotFound{
		try {
			System.out.println(filename);
			Resource recurso;
			switch (u.getRol()) {
			case MENTOR:
				recurso = fservice.getFile("recursos/user-files/mentores/" + u.getUsername() + "/perfil/" + filename);
				break;
			case MENTORIZADO:
				recurso = fservice
						.getFile("recursos/user-files/mentorizados/" + u.getUsername() + "/perfil/" + filename);
				break;// Aqui se podria añadir tambien a institucion
			default:
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(recurso);
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFileNotFound(filename);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/deletefile")
	public ResponseEntity<MensajeError> borrarFichero(@AuthenticationPrincipal UserAuth us,
			@RequestBody String filename) {
		System.out.println("Borrando fichero");
		try {
			String nombre = "";
			switch (us.getRol()) {
			case MENTOR:
				fservice.borrarFile(filename, us.getUsername(), "mentores");
				break;
			case MENTORIZADO:
				fservice.borrarFile(filename, us.getUsername(), "mentorizados");
				break;
			case INSTITUCION:
				fservice.borrarFile(filename, us.getUsername(), "instituciones");
				break;

			default:
				return new ResponseEntity<>(new MensajeError("Sin autorización", "No tienes permiso para hacer esto"),
						HttpStatus.UNAUTHORIZED);
			}
			return new ResponseEntity<>(new MensajeError(nombre, ""), HttpStatus.OK);

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			// Aqui faltaria limpiar el fichero en la DB
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio para borrar el fichero, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
			fservice.restoreFile(us.getUsername(), filename);
			return new ResponseEntity<>(new MensajeError("Fallo al borrar el fichero",
					"Se ha producido un fallo al tratar de borrar el fichero."), HttpStatus.BAD_REQUEST);
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fichero vacío", e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) { // Otro fallo
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error interno",
					"Se ha producido un error interno en el servidor al intentar borrar el fichero, por favor, si recibe este mensaje, "
							+ "pongasé en contacto con nosotros y detalle el contexto en el que ocurrió el error. Hora del suceso: "
							+ new Date()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
