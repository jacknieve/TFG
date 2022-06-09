package com.tfg.mentoring.service;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.tfg.mentoring.model.Usuario;

@Service
public class MessagingService {

	@Autowired
	private JavaMailSender mailSender;

	// https://mail.codejava.net/frameworks/spring-boot/email-verification-example
	public void sendVerificationEmail(Usuario user, String nombre, String siteURL)
			throws MessagingException, UnsupportedEncodingException {
		
		String toAddress = user.getUsername();
		String fromAddress = "pablo.mentoring@gmx.es";
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

	public void notificarPorCorreo(Usuario u, String titulo, String descripcion)
			throws MessagingException, UnsupportedEncodingException {
		String toAddress = u.getUsername();
		String fromAddress = "pablo.mentoring@gmx.es";
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
}
