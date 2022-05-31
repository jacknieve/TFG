package com.tfg.mentoring.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.QueryTimeoutException;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.exceptions.ExcepcionDB;
import com.tfg.mentoring.exceptions.ExcepcionRecursos;
import com.tfg.mentoring.model.Institucion;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.Notificacion;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.MensajeConAsunto;
import com.tfg.mentoring.model.auxiliar.UserAuth;
import com.tfg.mentoring.model.auxiliar.DTO.NotificacionDTO;
import com.tfg.mentoring.model.auxiliar.enums.AsuntoMensaje;
import com.tfg.mentoring.model.auxiliar.enums.EstadosNotificacion;
import com.tfg.mentoring.model.auxiliar.enums.MotivosNotificacion;
import com.tfg.mentoring.model.auxiliar.enums.Roles;
import com.tfg.mentoring.model.auxiliar.requests.UserAux;
import com.tfg.mentoring.repository.InstitucionRepo;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.NotificacionRepo;
import com.tfg.mentoring.repository.UsuarioRepo;
import com.tfg.mentoring.service.util.ListLoad;

import net.bytebuddy.utility.RandomString;

@Service
public class UserService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private MentorizadoRepo menrepo;

	@Autowired
	private MentorRepo mrepo;

	@Autowired
	private UsuarioRepo urepo;

	@Autowired
	private NotificacionRepo nrepo;

	@Autowired
	private InstitucionRepo irepo;
	

	
	@Autowired
	private ListLoad listas;
	
	@Autowired
	private SimpMessagingTemplate messagingTemplate; //Esto quizas mejor un servicio de envio de mensajes
	

	
	
	@Autowired
	private ActiveUsersService acservice;
	

	public void register(UserAux useraux, String siteURL) throws UnsupportedEncodingException, MessagingException,
			ExcepcionDB, JDBCConnectionException, QueryTimeoutException, SecurityException, ExcepcionRecursos, FileNotFoundException{
		String random = RandomString.make(64);
		Usuario user = new Usuario(useraux.getCorreo(), passwordEncoder.encode(useraux.getPassword()), false, random);
		System.out.println(user.toString());
		System.out.println(useraux.toString());
		if (useraux.getMentor()) {
			user.setRol(Roles.MENTOR);
			Mentor mentor = new Mentor(user, useraux, irepo.findByNombre(useraux.getInstitucion()).get(0));
			try {
				mrepo.save(mentor);
			} catch (DataIntegrityViolationException e) {
				throw new ExcepcionDB("Clave duplicada");
			}
			File filesUsers = new File("recursos/user-files/mentores/"+useraux.getCorreo()+"/");
			File filesUsersPerfil = new File("recursos/user-files/mentores/"+useraux.getCorreo()+"/perfil/");
			File filesUsersChat = new File("recursos/user-files/mentores/"+useraux.getCorreo()+"/chat/");
			if(!filesUsers.mkdir() || !filesUsersPerfil.mkdir() || !filesUsersChat.mkdir()) {
				throw new ExcepcionRecursos("No ha sido posible crear el directorio para el mentor");
			}
			String path = ResourceUtils.getFile("classpath:static/images/usuarios/mentores/").getAbsolutePath()+"/"+useraux.getCorreo()+"/";
			File imagen = new File(path);
			if(!imagen.mkdir()) {
				throw new ExcepcionRecursos("No ha sido posible crear el directorio de foto de perfil para el mentor");
			}
			sendVerificationEmail(user, mentor.getNombre(), siteURL);

		} else {
			user.setRol(Roles.MENTORIZADO);
			Mentorizado mentorizado = new Mentorizado(user, useraux,
					irepo.findByNombre(useraux.getInstitucion()).get(0));
			try {
				menrepo.save(mentorizado);
			} catch (DataIntegrityViolationException e) {
				throw new ExcepcionDB("Clave duplicada");
			}
			File filesUsers = new File("recursos/user-files/mentorizados/"+useraux.getCorreo()+"/");
			File filesUsersPerfil = new File("recursos/user-files/mentorizados/"+useraux.getCorreo()+"/perfil/");
			File filesUsersChat = new File("recursos/user-files/mentorizados/"+useraux.getCorreo()+"/chat/");
			if(!filesUsers.mkdir() || !filesUsersPerfil.mkdir() || !filesUsersChat.mkdir()) {
				throw new ExcepcionRecursos("No ha sido posible crear el directorio para el mentorizado");
			}
			String path = ResourceUtils.getFile("classpath:static/images/usuarios/mentorizados/").getAbsolutePath()+"/"+useraux.getCorreo()+"/";
			File imagen = new File(path);
			if(!imagen.mkdir()) {
				throw new ExcepcionRecursos("No ha sido posible crear el directorio de foto de perfil para el mentorizado");
			}
			sendVerificationEmail(user, mentorizado.getNombre(), siteURL);

		}
	}

	// https://mail.codejava.net/frameworks/spring-boot/email-verification-example
	private void sendVerificationEmail(Usuario user, String nombre, String siteURL)
			throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getUsername();
		String fromAddress = "mentoring.pablo@gmail.com";
		String senderName = "Mentoring";
		String subject = "Por favor, verifique su registro";
		String content = "Saludos [[name]],<br>" + "Por favor, haga click en el link para verificar su registro:<br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFICAR</a></h3>" + "Muchas gracias,<br>" + "Mentoring.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", nombre);
		String verifyURL = siteURL + "/auth/verify?code=" + user.getVerificationCode();

		content = content.replace("[[URL]]", verifyURL);

		helper.setText(content, true);

		// Aqui crear una excepcion personalizada en caso de excepcion
		mailSender.send(message);
	}

	public boolean verify(String verificationCode) throws JDBCConnectionException, QueryTimeoutException {
		Usuario user = urepo.findByVerificationCode(verificationCode);
		// Si no se encuentra al usuario o este ya esta verificado
		if (user == null || user.isEnabled()) {
			return false;
		} else {
			user.setVerificationCode(null);
			user.setEnable(true);
			urepo.save(user);
			if (user.getRol() == Roles.MENTOR) {
				// Aqui, los titulos y la descripcion se podría extraer de la base de datos al
				// arrancar el servidor y tenerlo en un
				// hashmap o similar, dado que en principio no deberian cambiar, y así el
				// administrador podría llegar a cambiarlo desde
				// su interfaz de control
				enviarNotificacion(user, "Bienvenido/a ",
						"Te damos la bienvenida a nuestra aplicación, esperemos que le sea de utilidad.\n Por favor, no olvide "
								+ "rellenar los campos extra en su perfíl, como las áreas de conocimiento en las que podría ayudar, o su descripción.",
						MotivosNotificacion.SISTEMA);
			} else if (user.getRol() == Roles.MENTORIZADO) {
				enviarNotificacion(user, "Bienvenido/a ",
						"Te damos la bienvenida a nuestra aplicación, esperemos que le sea de utilidad.\n Por favor, no olvide "
								+ "rellenar los campos extra en su perfíl, como las áreas de conocimiento en las que quiere ser mentorizado, o su descripción.",
						MotivosNotificacion.SISTEMA);
			}
			user.setNotificar_correo(false);
			return true;
		}

	}

	public void enviarNotificacion(Usuario u, String titulo, String descripcion, MotivosNotificacion motivo) {
		try {
			Notificacion notificacion = new Notificacion(u, titulo, descripcion, motivo);
			
			if (!acservice.activo(u.getUsername())) {
				if (u.isNotificar_correo()) {
					Thread thread = new Thread() {
						public void run() {
							try {
								notificarPorCorreo(u, titulo, descripcion);
								System.out.println("Se envio el correo");
							} catch (UnsupportedEncodingException | MessagingException e) {
								// TODO Auto-generated catch block
								System.out.println(e.getMessage());
								e.printStackTrace();
							}
						}
					};
					thread.start();
				}
			} else if (acservice.enNotificacion(u.getUsername())){
				if(!acservice.enChat(u.getUsername())) notificacion.setEstado(EstadosNotificacion.ENTREGADA);
				messagingTemplate.convertAndSendToUser(u.getUsername(), "/queue/messages",
						new MensajeConAsunto(AsuntoMensaje.NOTIFICACION, new NotificacionDTO(notificacion)));
			}
			nrepo.save(notificacion);
		} catch (JDBCConnectionException | QueryTimeoutException e) {// Si falla el acceso a la base de datos o tarda
																		// mucho
			// TODO: handle exception
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public void enviarNotificacionMensaje(String username, boolean porCorreo) {
		try {
			Optional<Usuario> u = urepo.findById(username);
			if (u.isPresent()) {
				if (nrepo.notificacionesMensajes(username).isEmpty()) {
					String titulo = "Mensajes nuevos";
					String descripcion = "Has recibido nuevos mensajes";
					Notificacion notificacion = new Notificacion(u.get(), titulo, descripcion, MotivosNotificacion.MENSAJE);
					nrepo.save(notificacion);
					if (porCorreo) {
						if (u.get().isNotificar_correo()) {

							try {
								notificarPorCorreo(u.get(), titulo, descripcion);
								System.out.println("Se envio el correo");
							} catch (UnsupportedEncodingException | MessagingException e) {
								// TODO Auto-generated catch block
								System.out.println(e.getMessage());
								e.printStackTrace();
							}
						}
					} else if (acservice.enNotificacion(username)){
						messagingTemplate.convertAndSendToUser(u.get().getUsername(), "/queue/messages",
								new MensajeConAsunto(AsuntoMensaje.NOTIFICACION, new NotificacionDTO(notificacion)));
					}
				} // En caso contrario, nada
			} else {
				// Esto se registraria en un log
				System.out.println("Se ha intentado enviar una notificacion a un usuario que no existe");
			}

		} catch (JDBCConnectionException | QueryTimeoutException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public void notificarPorCorreo(Usuario u, String titulo, String descripcion)
			throws MessagingException, UnsupportedEncodingException {
		String toAddress = u.getUsername();
		String fromAddress = "mentoring.pablo@gmail.com";
		String senderName = "Mentoring";
		String subject = titulo;
		String content = "Saludos,<br>" + descripcion + "<br>Muchas gracias por su atención,<br>" + "Mentoring.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		helper.setText(content, true);

		// Aqui crear una excepcion personalizada en caso de excepcion
		mailSender.send(message);
	}

	
	public void limpiarUsuario(UserAux user) throws JDBCConnectionException, QueryTimeoutException {
		if (user.getMentor()) {
			mrepo.limpiarUsuario(user.getCorreo());
		} else {
			menrepo.limpiarUsuario(user.getCorreo());
		}
		urepo.limpiarUsuario(user.getCorreo());
	}

	public void addListasModelo(ModelAndView modelo) {
		modelo.addObject("estudios", listas.getEstudios());
		modelo.addObject("instituciones", listas.getInstituciones());
		modelo.addObject("areas", listas.getAreas());
	}

	public void addListasModeloSinAreas(ModelAndView modelo) {
		modelo.addObject("estudios", listas.getEstudios());
		modelo.addObject("instituciones", listas.getInstituciones());
	}

	public boolean comprobarPassword(String password, UserAuth u) {
		return passwordEncoder.matches(password, u.getPassword());
	}

	public void addInstitucionUtils(ModelAndView modelo, Institucion i) {
		if (!i.getNombre().equals("Otra")) {
			if (i.getColor() != null && !i.getColor().equals("")) {
				modelo.addObject("color", i.getColor());
			} else {
				modelo.addObject("color", "#5AB9EA");
			}
			if (i.getColorB() != null && !i.getColorB().equals("")) {
				modelo.addObject("colorB", i.getColorB());
			} else {
				modelo.addObject("colorB", "#5AB9EA");
			}
			modelo.addObject("letrasBB", i.isLetrasBB());
			modelo.addObject("letrasBlancas", i.isLetrasBlancas());
			if(i.getUsuario().getFoto() != null) {
				modelo.addObject("logo", "/images/usuarios/instituciones/" + i.getUsuario().getUsername() + "/" + i.getUsuario().getFoto());
			}
		} else {
			modelo.addObject("letrasBB", false);
			modelo.addObject("letrasBlancas", false);
		}
	}

}
