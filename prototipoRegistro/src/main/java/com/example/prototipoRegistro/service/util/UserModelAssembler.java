package com.example.prototipoRegistro.service.util;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.example.prototipoRegistro.controller.DemoController;
import com.example.prototipoRegistro.model.Usuario;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<Usuario, EntityModel<Usuario>>{

	@Override
	public EntityModel<Usuario> toModel(Usuario usuario){
		
		return EntityModel.of(usuario, linkTo(methodOn(DemoController.class).getUser(usuario.getId())).withSelfRel());
	}
}
