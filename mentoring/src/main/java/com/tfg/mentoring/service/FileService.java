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
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tfg.mentoring.exceptions.ExcepcionDB;
import com.tfg.mentoring.exceptions.ExcepcionFichero;
import com.tfg.mentoring.exceptions.ExcepcionRecursos;
import com.tfg.mentoring.model.Fichero;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.repository.FicheroRepo;
import com.tfg.mentoring.repository.UsuarioRepo;

@Service
public class FileService {

	@Autowired
	private UsuarioRepo urepo;

	@Autowired
	private FicheroRepo frepo;

	public String guardarImagen(MultipartFile imagen, String username, String rol)
			throws IOException, ExcepcionFichero, SecurityException, JDBCConnectionException, QueryTimeoutException {

		String path = ResourceUtils.getFile("classpath:static/images/usuarios/" + rol + "/" + username + "/")
				.getAbsolutePath();
		if (imagen.isEmpty()) {
			throw new ExcepcionFichero("Imagen vacía", "La imagen facilitada estaba vacía.");
		}
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
		return "/images/usuarios/" + rol + "/" + username + "/" + filename;
		// Si aqui falla, habria que hacer una consulta de limpieza en la DB, es decir,
		// si salta una IO exception
	}

	// https://www.baeldung.com/java-file-extension#:~:text=java%E2%80%9C.,returns%20extension%20of%20the%20filename.
	public Optional<String> getExtensionByStringHandling(String filename) {
		return Optional.ofNullable(filename).filter(f -> f.contains("."))
				.map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}

	public boolean checkFormat(String extension) {
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

	public String guardarFichero(MultipartFile file, String username, String rol)
			throws IOException, ExcepcionFichero, SecurityException, ExcepcionRecursos, ExcepcionDB, JDBCConnectionException, QueryTimeoutException{

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
		// https://www.baeldung.com/java-folder-size

		Path directorio = Paths.get(path);
		long size = Files.walk(directorio).filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();
		float tam = ((size + file.getSize()) / 1024) / 1024;
		if (tam > 50.0) {// Si es mas que 10MB
			throw new ExcepcionFichero("Cuota superada",
					"Has superado la cuota de almacenamiento de 10MB, patra subir un nuevo archivo, debe eliminar otros.");
		}
		Optional<Usuario> u = urepo.findById(username);
		String filePath = "";
		if (u.isPresent()) {
			frepo.save(new Fichero(u.get(), nombre));
			filePath = path + "" + nombre;
		} else {
			// Aqui poner otra excepcion (la excepcion de ficheros podria tener otro
			// atributo donde meterle el titulo)
			// Lo mismo en subir imagen
			throw new ExcepcionDB("No se ha podido encontrar la información de perfil del usuario.");
		}

		byte[] bytes = file.getBytes();
		Path pathFichero = Paths.get(filePath);
		Files.write(pathFichero, bytes);
		return nombre;
	}

	public List<String> getFicherosUser(String username) {
		List<String> files = new ArrayList<>();
		files = frepo.findByUser(username).stream().map(Fichero::getNombre).collect(Collectors.toList());
		return files;
	}

	public void borrarImage(String filename, String username, String rol) throws FileNotFoundException {
			String path = ResourceUtils.getFile("classpath:static/images/usuarios/" + rol + "/" + username + "/")
					.getAbsolutePath();
			String filePath = path + "/" + filename;
			File f = new File(filePath);
			if (f.exists()) {
				f.delete();
			}
		
	}

	public void borrarFile(String filename, String username, String rol) 
			throws JDBCConnectionException, QueryTimeoutException, ExcepcionRecursos, SecurityException, IOException{
		String path = "recursos/user-files/" + rol + "/" + username + "/perfil/";
		File dir = new File(path);
		if (dir.exists()) {
			String filePath = path + "" + filename;
			File f = new File(filePath);
			if (f.exists()) {
				frepo.limpiarFichero(username, filename);
				f.delete();
			}
			else {
				throw new ExcepcionRecursos("El fichero indicado no existe.");
			}
		}
		else {
			throw new ExcepcionRecursos("No ha sido posible acceder al directorio del usuario para borrar la imagen.");
		}
	}
	
	public void clearImage(String username){
		try {
			urepo.clearFoto(username);
		}
		catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			System.out.println("No ha sido posible limpiar la imagen en la base de datos");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("No ha sido posible limpiar la imagen en la base de datos");
		}
	}
	
	public void clearFile(String username, String filename) {
		try {
			frepo.limpiarFichero(username, filename);
		}
		catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			System.out.println("No ha sido posible limpiar el fichero en la base de datos");
		}
		catch (Exception e) {
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
				System.out.println("No ha sido posible restaurar el fichero en la base de datos, debido a que no se ha encontrado el usuario");
			}
		}
		catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
			System.out.println("No ha sido posible restaurar el fichero en la base de datos");
		}
		catch (Exception e) {
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
}
