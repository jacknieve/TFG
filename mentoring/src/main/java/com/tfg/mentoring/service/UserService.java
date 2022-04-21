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

import com.tfg.mentoring.exceptions.ExcepcionDB;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;
import com.tfg.mentoring.model.Notificacion;
import com.tfg.mentoring.model.Usuario;
import com.tfg.mentoring.model.auxiliar.MentorBusqueda;
import com.tfg.mentoring.model.auxiliar.Roles;
import com.tfg.mentoring.model.auxiliar.UserAux;
import com.tfg.mentoring.repository.InstitucionRepo;
import com.tfg.mentoring.repository.MentorRepo;
import com.tfg.mentoring.repository.MentorizadoRepo;
import com.tfg.mentoring.repository.NotificacionRepo;
import com.tfg.mentoring.repository.UsuarioRepo;

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
    
    
    
    
    public void register(Usuario user, UserAux useraux, String siteURL) throws UnsupportedEncodingException, MessagingException, ExcepcionDB{
    	user.setPassword(passwordEncoder.encode(user.getPassword()));
    	String random = RandomString.make(64);
    	user.setEnable(false);
    	user.setVerificationCode(random);
    	System.out.println(user.toString());
    	System.out.println(useraux.toString());
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
				.map(this::convertEntitytoDto)
				.collect(Collectors.toList());
	}
	
	private MentorBusqueda convertEntitytoDto(Mentor m) {
		MentorBusqueda user = new MentorBusqueda();
		user = maper.map(m, MentorBusqueda.class);
		return user;
	}
    
    
}
