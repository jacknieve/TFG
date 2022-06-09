package com.tfg.mentoring.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
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
import com.tfg.mentoring.exceptions.ExcepcionFalloAccesoFile;
import com.tfg.mentoring.exceptions.ExcepcionFichero;
import com.tfg.mentoring.exceptions.ExcepcionFileNotFound;
import com.tfg.mentoring.exceptions.ExcepcionNoAutorizado;
import com.tfg.mentoring.exceptions.ExcepcionRecursos;
import com.tfg.mentoring.model.auxiliar.MensajeError;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.service.FileService;
import com.tfg.mentoring.service.ChatService;

@RestController
@RequestMapping("/file")
public class FileController {

	@Autowired
	private FileService fservice;
	@Autowired
	private ChatService sservice;

	@PostMapping("/fotoperfil")
	public ResponseEntity<MensajeError> setFotoPerfil(@RequestParam("imagen") MultipartFile image,
			@AuthenticationPrincipal UserAuth u) {
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
			// System.out.println("Foto de perfil subida: " + path + " por " +
			// u.getUsername());
			return new ResponseEntity<>(new MensajeError(path, ""), HttpStatus.OK);

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (IOException | SecurityException e) {
			fservice.clearImage(u.getUsername());
			e.printStackTrace();
			return new ResponseEntity<>(
					new MensajeError("Fallo al almacenar la imagen",
							"Se ha producido un fallo al tratar de almacenar la imagen."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ExcepcionFichero e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError(e.getTitulo(), e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
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
			// System.out.println("Fichero subido: " + nombre + " por " + u.getUsername());
			return new ResponseEntity<>(new MensajeError(nombre, ""), HttpStatus.OK);

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
			fservice.clearFile(u.getUsername(), file.getOriginalFilename());
			return new ResponseEntity<>(
					new MensajeError("Fallo al almacenar el fichero",
							"Se ha producido un fallo al tratar de almacenar el fichero."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ExcepcionFichero e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError(e.getTitulo(), e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fichero vacío", e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
		} catch (ExcepcionDB e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Usuario no encontrado", e.getMessage()),
					HttpStatus.NOT_FOUND);
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
	@GetMapping("/download/{quien}/{username}/{filename}")
	public ResponseEntity<Resource> descargarFichero(@PathVariable("quien") String quien, @PathVariable("username") String username,
			@PathVariable("filename") String filename, @AuthenticationPrincipal UserAuth u)
			throws MalformedURLException, ExcepcionFileNotFound, ExcepcionFalloAccesoFile, NotFoundException {
		try {
			if (!quien.equals("mentores") && !quien.equals("mentorizados")) {
				throw new NotFoundException();
			}

			Resource recurso = fservice.getFile("recursos/user-files/" + quien + "/" + username + "/perfil/" + filename);
			// System.out.println("Fichero descargado: " + filename + " por " +
			// u.getUsername() + " de " + username);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(recurso);
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFileNotFound(filename);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFalloAccesoFile(filename);
		}
	}

	@GetMapping("/download/{quien}/chat/{sala}/{filename}")
	public ResponseEntity<Resource> descargarFicheroChat(@PathVariable("quien") String quien,@PathVariable("sala") String sala,
			 @PathVariable("filename") String filename, @AuthenticationPrincipal UserAuth u) throws MalformedURLException, ExcepcionFileNotFound,
			ExcepcionFalloAccesoFile, NotFoundException, ExcepcionNoAutorizado {
		try {
			if (!quien.equals("mentor") && !quien.equals("mentorizado")) {
				throw new NotFoundException();
			}

			long id = Long.parseLong(sala);
			if (sservice.comprobarSalaUsuario(id, u.getUsername(), quien)) {

				Resource recurso = fservice
						.getFile("recursos/user-files/salaschat/" + id + "/" + quien + "/" + filename);
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
						.body(recurso);
			} else {
				throw new ExcepcionNoAutorizado(
						"No tienes permiso para descargar ese fichero, no es de un chat en el que participes");
			}
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFileNotFound(filename);
		} catch (NumberFormatException e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFileNotFound(filename);
		} catch (ExcepcionNoAutorizado e) {
			System.out.println(e.getMessage());
			throw e;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFalloAccesoFile(filename);
		}
	}

	@GetMapping("/download/my/{filename}")
	public ResponseEntity<Resource> descargarMiFichero(@PathVariable("filename") String filename,
			@AuthenticationPrincipal UserAuth u)
			throws ExcepcionFileNotFound, ExcepcionFalloAccesoFile, NotFoundException {
		try {
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
			// System.out.println("Fichero propio descargado: " + filename + " por " +
			// u.getUsername());
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(recurso);
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFileNotFound(filename);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFalloAccesoFile(filename);
		}
	}

	@GetMapping("/download/chat/my/{sala}/{filename}")
	public ResponseEntity<Resource> descargarMiFicheroChat(@PathVariable("sala") String sala, @PathVariable("filename") String filename,
			@AuthenticationPrincipal UserAuth u)
			throws ExcepcionFileNotFound, ExcepcionFalloAccesoFile, NotFoundException, ExcepcionNoAutorizado {
		try {
			Resource recurso;
			long id = Long.parseLong(sala);
			
			switch (u.getRol()) {
			case MENTOR:
				if (sservice.comprobarSalaUsuario(id, u.getUsername(), "mentorizado")) {//Si mentorizado esta aqui a proposito
				recurso = fservice.getFile("recursos/user-files/salaschat/" + id + "/mentor/" + filename);
				}
				else {
					throw new ExcepcionNoAutorizado(
							"No tienes permiso para descargar ese fichero, no es tuyo");
				}
				break;
			case MENTORIZADO:
				if (sservice.comprobarSalaUsuario(id, u.getUsername(), "mentor")) {
					recurso = fservice.getFile("recursos/user-files/salaschat/" + id + "/mentorizado/" + filename);
					}
					else {
						throw new ExcepcionNoAutorizado(
								"No tienes permido para descargar ese fichero, no es tuyo");
					}
				break;
			default:
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			// System.out.println("Fichero propio descargado: " + filename + " por " +
			// u.getUsername());
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(recurso);
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFileNotFound(filename);
		} catch (NumberFormatException e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFileNotFound(filename);
		} catch (ExcepcionNoAutorizado e) {
			System.out.println(e.getMessage());
			throw e;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new ExcepcionFalloAccesoFile(filename);
		}
	}

	@PostMapping("/deletefile")
	public ResponseEntity<MensajeError> borrarFichero(@RequestBody String filename, @AuthenticationPrincipal UserAuth us) {
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
			// System.out.println("Fichero borrado: " + filename + " por " +
			// us.getUsername());
			return new ResponseEntity<>(new MensajeError(nombre, ""), HttpStatus.OK);

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Fallo en el repositorio",
					"Se ha producido un problema al intentar actualizar el repositorio para borrar el fichero, "
							+ "por favor, vuelva a intentarlo más tarde."),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
			fservice.restoreFile(us.getUsername(), filename);
			return new ResponseEntity<>(
					new MensajeError("Fallo al borrar el fichero",
							"Se ha producido un fallo al tratar de borrar el fichero."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ExcepcionRecursos e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new MensajeError("Error de recurso", e.getMessage()),
					HttpStatus.NOT_ACCEPTABLE);
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
