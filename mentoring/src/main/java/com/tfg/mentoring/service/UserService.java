package com.tfg.mentoring.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.tfg.mentoring.exceptions.ExcepcionDB;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.Notificacion;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.MentorBusqueda;
import com.tfg.mentoring.model.auxiliar.Roles;
import com.tfg.mentoring.model.auxiliar.UserAux;
import com.tfg.mentoring.model.auxiliar.UsuarioPerfil;
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
	private ModelMapper maper;
    
	@Autowired
	private ListLoad listas;
    
    
    public void register(UserAux useraux, String siteURL) throws UnsupportedEncodingException, MessagingException, ExcepcionDB{
    	String random = RandomString.make(64);
    	Usuario user = new Usuario(useraux.getCorreo(), passwordEncoder.encode(useraux.getPassword()), false, random);
    	//System.out.println(user.toString());
    	//System.out.println(useraux.toString());
	    if(useraux.getMentor()) {
	    	user.setRol(Roles.MENTOR);
	    	Mentor mentor = new Mentor(user, useraux, irepo.findByNombre(useraux.getInstitucion()).get(0));
	    	try {
	    	mrepo.save(mentor);
	    	}catch(DataIntegrityViolationException e) {
	    		throw new ExcepcionDB("Clave duplicada");
	    	}
	    	sendVerificationEmail(user, mentor.getNombre(), siteURL);
	    	//Aqui, los titulos y la descripcion se podría extraer de la base de datos al arrancar el servidor y tenerlo en un
	    	//hashmap o similar, dado que en principio no deberian cambiar, y así el administrador podría llegar a cambiarlo desde
	    	//su interfaz de control
	    	enviarNotificacion(user, "Bienvenido/a "+mentor.getNombre(), 
	    			"Te damos la bienvenida a nuestra aplicación, esperemos que le sea de utilidad.\n Por favor, no olvide "
	    			+ "rellenar los campos extra en su perfíl, como las áreas de conocimiento en las que podría ayudar, o su descripción.");
	    }
	    else {
	    	user.setRol(Roles.MENTORIZADO);
	    	Mentorizado mentorizado = new Mentorizado(user, useraux, irepo.findByNombre(useraux.getInstitucion()).get(0));
	    	try {
	    	menrepo.save(mentorizado);
	    	}catch(DataIntegrityViolationException e) {
	    		throw new ExcepcionDB("Clave duplicada");
	    	}
	    	sendVerificationEmail(user, mentorizado.getNombre(), siteURL);
	    	enviarNotificacion(user, "Bienvenido/a "+mentorizado.getNombre(), 
	    			"Te damos la bienvenida a nuestra aplicación, esperemos que le sea de utilidad.\n Por favor, no olvide"
	    			+ "rellenar los campos extra en su perfíl, como las áreas de conocimiento en las que quiere ser mentorizado, o su descripción.");
	    }
    }
     //https://mail.codejava.net/frameworks/spring-boot/email-verification-example
    private void sendVerificationEmail(Usuario user, String nombre, String siteURL) throws MessagingException, UnsupportedEncodingException{
    	String toAddress = user.getUsername();
        String fromAddress = "mentoring.pablo@gmail.com";
        String senderName = "Mentoring";
        String subject = "Por favor, verifique su registro";
        String content = "Saludos [[name]],<br>"
                + "Por favor, haga click en el link para verificar su registro:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFICAR</a></h3>"
                + "Muchas gracias,<br>"
                + "Mentoring.";
         
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
         
        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
         
        content = content.replace("[[name]]", nombre);
        String verifyURL = siteURL + "/auth/verify?code=" + user.getVerificationCode();
         
        content = content.replace("[[URL]]", verifyURL);
         
        helper.setText(content, true);
         
        //Aqui crear una excepcion personalizada en caso de excepcion
        mailSender.send(message);
    }
    
    public boolean verify(String verificationCode) {
        Usuario user = urepo.findByVerificationCode(verificationCode);
         //Si no se encuentra al usuario o este ya esta verificado
        if (user == null || user.isEnabled()) {
            return false;
        } else {
            user.setVerificationCode(null);
            user.setEnable(true);
            urepo.save(user);
             
            return true;
        }
         
    }
    
    
    public void enviarNotificacion(Usuario u, String titulo, String descripcion) {
    	Notificacion notificacion = new Notificacion(u, titulo, descripcion);
    	nrepo.save(notificacion);
    }
    
    
    
    public List<MentorBusqueda> getMentorBusqueda(List<Mentor> mentores){
		return mentores
				.stream()
				.map(this::convertMentortoMentorBusqueda)
				.collect(Collectors.toList());
	}
	
	private MentorBusqueda convertMentortoMentorBusqueda(Mentor m) {
		MentorBusqueda user = new MentorBusqueda();
		user = maper.map(m, MentorBusqueda.class);
		return user;
	}
	
	public UsuarioPerfil getPerfilMentor(Mentor mentor){
		UsuarioPerfil user = new UsuarioPerfil();
		user = maper.map(mentor, UsuarioPerfil.class);
		//System.out.println(user.toString());
		return user;
	}
	
	public UsuarioPerfil getPerfilMentorizado(Mentorizado mentorizado){
		UsuarioPerfil user = new UsuarioPerfil();
		user = maper.map(mentorizado, UsuarioPerfil.class);
		//System.out.println(user.toString());
		return user;
	}
	
	public void limpiarUsuario(UserAux user) {
		if(user.getMentor()) {
			mrepo.deleteById(user.getCorreo());
		}
		else {
			menrepo.deleteById(user.getCorreo());
		}
		//Quizas solo haga falta quitar esta o de error, no estoy seguro
		urepo.deleteById(user.getCorreo());
	}
	
	public void addListasModelo(ModelAndView modelo) {
		modelo.addObject("puestos", listas.getPuestos());
	    modelo.addObject("estudios", listas.getEstudios());
	    modelo.addObject("instituciones", listas.getInstituciones());
	    modelo.addObject("areas", listas.getAreas());
	}
	
	public void addListasModeloSinAreas(ModelAndView modelo) {
		modelo.addObject("puestos", listas.getPuestos());
	    modelo.addObject("estudios", listas.getEstudios());
	    modelo.addObject("instituciones", listas.getInstituciones());
	}
	
	public boolean comprobarPassword(String password, Usuario u) {
		return passwordEncoder.matches(password, u.getPassword());
	}
	
    
    
}
