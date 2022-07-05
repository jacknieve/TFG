package com.tfg.mentoring.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.QueryTimeoutException;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tfg.mentoring.exceptions.ExcepcionDB;
import com.tfg.mentoring.exceptions.ExcepcionFichero;
import com.tfg.mentoring.exceptions.ExcepcionRecursos;
import com.tfg.mentoring.model.Fichero;
import com.tfg.mentoring.model.MensajeChat;
import com.tfg.mentoring.model.SalaChat;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.repository.FicheroRepo;
import com.tfg.mentoring.repository.MensajesRepo;
import com.tfg.mentoring.repository.UsuarioRepo;

@Service
public class FileService {

	@Autowired
	private UsuarioRepo urepo;
	@Autowired
	private FicheroRepo frepo;
	@Autowired
	private MensajesRepo mrepo;

	/*public void crearDirectoriosUsuario(String username, String rol) throws FileNotFoundException, ExcepcionRecursos {
		File filesUsers = new File("recursos/user-files/" + rol + "/" + username + "/");
		File filesUsersPerfil = new File("recursos/user-files/" + rol + "/" + username + "/perfil/");
		if (!filesUsers.mkdir() || !filesUsersPerfil.mkdir()) {
			throw new ExcepcionRecursos("No ha sido posible crear el directorio para el usuario");
		}
		String path = ResourceUtils.getFile("classpath:static/images/usuarios/" + rol + "/").getAbsolutePath() + "/"
				+ username + "/";
		File imagen = new File(path);
		if (!imagen.mkdir()) {
			throw new ExcepcionRecursos("No ha sido posible crear el directorio de foto de perfil para el usuario");
		}
	}*/
	
	//V2
	
	public void crearDirectoriosUsuario(String username, String rol) throws FileNotFoundException, ExcepcionRecursos {
		File filesUsers = new File("recursos/user-files/" + rol + "/" + username + "/");
		File filesUsersPerfil = new File("recursos/user-files/" + rol + "/" + username + "/perfil/");
		File filesUsersFoto = new File("imagenes/" + rol + "/" + username + "/");
		if (!filesUsers.mkdir() || !filesUsersPerfil.mkdir() || !filesUsersFoto.mkdir()) {
			throw new ExcepcionRecursos("No ha sido posible crear el directorio para el usuario");
		}
		/*String path = ResourceUtils.getFile("classpath:static/images/usuarios/" + rol + "/").getAbsolutePath() + "/"
				+ username + "/";
		File imagen = new File(path);
		if (!imagen.mkdir()) {
			throw new ExcepcionRecursos("No ha sido posible crear el directorio de foto de perfil para el usuario");
		}*/
	}

	public void crearDirectoriosChat(long id) throws ExcepcionRecursos, SecurityException {
		File salaChat = new File("recursos/user-files/salaschat/" + id + "/");
		File salaCahtMentor = new File("recursos/user-files/salaschat/" + id + "/mentor/");
		File salaCahtMentorizado = new File("recursos/user-files/salaschat/" + id + "/mentorizado/");
		if (!salaChat.mkdir() || !salaCahtMentor.mkdir() || !salaCahtMentorizado.mkdir()) {
			throw new ExcepcionRecursos("No ha sido posible crear los directorios para la sala de chat");
		}
	}

	public String guardarImagen(MultipartFile imagen, String username, String rol)
			throws IOException, ExcepcionFichero, SecurityException, JDBCConnectionException, QueryTimeoutException {

		/*String path = ResourceUtils.getFile("classpath:static/images/usuarios/" + rol + "/" + username + "/")
				.getAbsolutePath();
		if (imagen.isEmpty()) {
			throw new ExcepcionFichero("Imagen vacía", "La imagen facilitada estaba vacía.");
		}*/
		String path = "imagenes/" + rol + "/" + username + "/";
		String filename = imagen.getOriginalFilename();
		Optional<String> extension = getExtensionByStringHandling(filename);
		if (filename.length() > 250) {
			throw new ExcepcionFichero("Nombre demasiado grande",
					"El nombre de la imagen es demasiado grande, no puede exceder los 250 caracteres.");
		}
		if (!checkFormat(extension.get())) {
			throw new ExcepcionFichero("Formato no soportado",
					"El formato de la imagen no es soportado por esta aplicación.");
		}
		String imagenPath = "";
		File dir = new File(path);
		FileUtils.cleanDirectory(dir);// Para borrar las otras imagenes
		if (extension.isPresent()) {
			urepo.actualizaFoto(filename, username);
			imagenPath = path + "/" + filename;
		} else {
			throw new ExcepcionFichero("Sin extensión", "La imagen facilitada no tiene extensión.");
		}

		byte[] bytes = imagen.getBytes();
		Path pathImagen = Paths.get(imagenPath);
		Files.write(pathImagen, bytes);
		return "/imagenes/" + rol + "/" + username + "/" + filename;
		// Si aqui falla, habria que hacer una consulta de limpieza en la DB, es decir,
		// si salta una IO exception
	}

	// https://www.baeldung.com/java-file-extension#:~:text=java%E2%80%9C.,returns%20extension%20of%20the%20filename.
	private Optional<String> getExtensionByStringHandling(String filename) {
		return Optional.ofNullable(filename).filter(f -> f.contains("."))
				.map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}

	private boolean checkFormat(String extension) {
		String format = extension.toLowerCase();
		switch (format) {
		case "png":
		case "jpg":
		case "jpeg":
		case "gif":
		case "jfif":
		case "pjpeg":
		case "pjp":
		case "ico":
		case "svg":
		case "cur":
		case "apng":

			return true;

		default:
			return false;
		}

	}

	public String guardarFichero(MultipartFile file, String username, String rol) throws IOException, ExcepcionFichero,
			SecurityException, ExcepcionRecursos, ExcepcionDB, JDBCConnectionException, QueryTimeoutException {

		String path = "recursos/user-files/" + rol + "/" + username + "/perfil/";
		File dir = new File(path);
		if (!dir.exists()) {
			throw new ExcepcionRecursos(
					"No existe el directorio del usuario, por favor, si recibe este error, pongase en contacto con nosotros."
							+ "Hora del suceso: " + new Date());
		}
		if (file.isEmpty()) {
			throw new ExcepcionFichero("Fichero vacío", "El fichero facilitado estaba vacío.");
		}
		String nombre = file.getOriginalFilename();
		if (nombre.length() > 250) {
			throw new ExcepcionFichero("Nombre demasiado largo",
					"El nombre del fichero excede la longitud máxima permitida de 250 caracteres.");
		}

		Path directorio = Paths.get(path);
		// https://www.baeldung.com/java-folder-size
		long size = Files.walk(directorio).filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();
		float tam = ((size + file.getSize()) / 1024) / 1024;
		if (tam > 50.0) {// Si es mas que 10MB
			throw new ExcepcionFichero("Cuota superada",
					"Has superado la cuota de almacenamiento de 50MB, patra subir un nuevo archivo, debe eliminar otros.");
		}
		Optional<Usuario> u = urepo.findById(username);
		String filePath = "";
		if (u.isPresent()) {
			frepo.save(new Fichero(u.get(), nombre));
			filePath = path + "" + nombre;
		} else {
			throw new ExcepcionDB("No se ha podido encontrar la información de perfil del usuario.");
		}

		byte[] bytes = file.getBytes();
		Path pathFichero = Paths.get(filePath);
		Files.write(pathFichero, bytes);
		return nombre;
	}

	public MensajeChat guardarFicheroSend(MultipartFile file, String rol, SalaChat sala, boolean deMentor)
			throws IOException, ExcepcionFichero, SecurityException, ExcepcionRecursos, ExcepcionDB,
			JDBCConnectionException, QueryTimeoutException {

		String path = "recursos/user-files/salaschat/" + sala.getId_sala() + "/" + rol + "/";
		File dir = new File(path);
		if (!dir.exists()) {
			throw new ExcepcionRecursos(
					"No existe el directorio del usuario en el chat, por favor, si recibe este error, pongase en contacto con nosotros."
							+ "Hora del suceso: " + new Date());
		}
		if (file.isEmpty()) {
			throw new ExcepcionFichero("Fichero vacío", "El fichero facilitado estaba vacío.");
		}
		String nombre = file.getOriginalFilename();
		if (nombre.length() > 250) {
			throw new ExcepcionFichero("Nombre demasiado largo",
					"El nombre del fichero excede la longitud máxima permitida de 250 caracteres.");
		}
		// https://www.baeldung.com/java-folder-size

		Path directorio = Paths.get(path);
		long size = Files.walk(directorio).filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();
		float tam = ((size + file.getSize()) / 1024) / 1024;
		if (tam > 30.0) {
			throw new ExcepcionFichero("Cuota superada",
					"Has superado la cuota de almacenamiento de 30MB en el chat, para enviar un nuevo archivo, debe eliminar otros.");
		}
		MensajeChat msg = new MensajeChat(nombre, sala, deMentor, false);
		mrepo.save(msg);
		String filePath = path + "" + nombre;

		byte[] bytes = file.getBytes();
		Path pathFichero = Paths.get(filePath);
		Files.write(pathFichero, bytes);
		return msg;
	}

	public List<String> getFicherosUser(String username) {
		List<String> files = new ArrayList<>();
		files = frepo.findByUser(username).stream().map(Fichero::getNombre).collect(Collectors.toList());
		return files;
	}

	public List<String> getFicherosUserChat(String username) {
		List<String> files = new ArrayList<>();
		files = frepo.findByUser(username).stream().map(Fichero::getNombre).collect(Collectors.toList());
		return files;
	}

	public void borrarImage(String filename, String username, String rol) throws FileNotFoundException {
		String filePath = "imagenes/" + rol + "/" + username + "/" + filename;
		File f = new File(filePath);
		if (f.exists()) {
			f.delete();
		}

	}

	public void borrarFile(String filename, String username, String rol)
			throws JDBCConnectionException, QueryTimeoutException, ExcepcionRecursos, SecurityException, IOException {
		String path = "recursos/user-files/" + rol + "/" + username + "/perfil/";
		File dir = new File(path);
		if (dir.exists()) {
			String filePath = path + "" + filename;
			File f = new File(filePath);
			if (f.exists()) {
				frepo.limpiarFichero(username, filename);
				f.delete();
			} else {
				throw new ExcepcionRecursos("El fichero indicado no existe.");
			}
		} else {
			throw new ExcepcionRecursos("No ha sido posible acceder al directorio del usuario para borrar el fichero.");
		}
	}

	public void borrarFileSend(String filename, long id, String rol)
			throws JDBCConnectionException, QueryTimeoutException, ExcepcionRecursos, SecurityException, IOException {
		String path = "recursos/user-files/salaschat/" + id + "/" + rol + "/";
		File dir = new File(path);
		if (dir.exists()) {
			String filePath = path + "" + filename;
			File f = new File(filePath);
			if (f.exists()) {
				f.delete();
			} else {
				throw new ExcepcionRecursos("El fichero indicado no existe.");
			}
		} else {
			throw new ExcepcionRecursos("No ha sido posible acceder al directorio del usuario para borrar el fichero.");
		}
	}

	public void clearImage(String username) {
		try {
			urepo.clearFoto(username);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			System.out.println("No ha sido posible limpiar la imagen en la base de datos");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("No ha sido posible limpiar la imagen en la base de datos");
		}
	}

	public void clearFile(String username, String filename) {
		try {
			frepo.limpiarFichero(username, filename);
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			// Aqui habria que registrar esto de tener un log, porque es algo que se tendria
			// que arrgelar una vez estuviese disponible
			System.out.println("No ha sido posible limpiar el fichero en la base de datos");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("No ha sido posible limpiar el fichero en la base de datos");
		}
	}

	public void restoreFile(String username, String filename) {
		try {
			Optional<Usuario> u = urepo.findById(username);
			if (u.isPresent()) {
				frepo.save(new Fichero(u.get(), filename));
			} else {
				System.out.println(
						"No ha sido posible restaurar el fichero en la base de datos, debido a que no se ha encontrado el usuario");
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			System.out.println("No ha sido posible restaurar el fichero en la base de datos");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("No ha sido posible restaurar el fichero en la base de datos");
		}
	}

	public Resource getFile(String path) throws ExcepcionRecursos, MalformedURLException {
		File file = new File(path);
		if (!file.exists()) {
			throw new ExcepcionRecursos("El fichero no existe");
		}
		return new UrlResource(file.toURI());
	}

	public void limpiarFilesSala(long sala) throws IOException, ExcepcionRecursos {
		File dirChat = new File("recursos/user-files/salaschat/" + sala + "/");
		if (dirChat.exists()) {
			if(dirChat.list().length > 0) FileUtils.cleanDirectory(dirChat);
			dirChat.delete();
		} else {
			throw new ExcepcionRecursos("No ha sido posible limpiar los ficheros del chat " + sala + ".");
		}
	}

	public void borrarTodosUsuario(String username, String rol){
		try {
			frepo.limpiarDeUsuario(username);
			urepo.borrarFoto(username);
			
			String path = "imagenes/" + rol + "/" + username + "/";
			File dirFoto = new File(path);
			if (dirFoto.exists() && dirFoto.list().length > 0) {
				FileUtils.cleanDirectory(dirFoto);
			} else {
				System.out.println("No ha sido posible acceder al directorio de la foto de perfil del usuario " + username
						+ "para borrar el fichero.");
			}
			
			path = "recursos/user-files/" + rol + "/" + username + "/";
			File dirPerfil = new File(path + "perfil/");
			if (dirPerfil.exists() && dirPerfil.list().length > 0) {
				FileUtils.cleanDirectory(dirPerfil);
			} else {
				System.out.println("No ha sido posible acceder al directorio de perfil del usuario " + username
						+ "para borrar el fichero.");
			}
		} catch (JDBCConnectionException | QueryTimeoutException e) {//Esto se registraría para que fuese solucionado por el administrador
			System.out
					.println("No ha sido posible acceder al repositorio para poder borrar los ficheros de " + username + ".");
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out
					.println("Se ha producido un error al tratar de borrar los ficheros del usuario " + username + ".");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out
					.println("Se ha producido un error al tratar de borrar los ficheros del usuario " + username + ".");
		}
	}
}
