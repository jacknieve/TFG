package com.tfg.mentoring.model.auxiliar;

import com.tfg.mentoring.model.auxiliar.enums.AsuntoMensaje;

//Esta clase conforma lo que se va a enviar en los mensajes
public class MensajeConAsunto {

	private AsuntoMensaje asunto; //Cabecera del mensaje, indica como "interpretar el cuerpo"
	private CuerpoMensaje cuerpo; //Cuerpo del mensaje, tendra una clase u otra dependiendo de la cabecera
	
	public MensajeConAsunto(AsuntoMensaje asunto, CuerpoMensaje cuerpo) {
		super();
		this.asunto = asunto;
		this.cuerpo = cuerpo;
	}

	public AsuntoMensaje getAsunto() {
		return asunto;
	}

	public void setAsunto(AsuntoMensaje asunto) {
		this.asunto = asunto;
	}

	public CuerpoMensaje getCuerpo() {
		return cuerpo;
	}

	public void setCuerpo(CuerpoMensaje cuerpo) {
		this.cuerpo = cuerpo;
	}

	@Override
	public String toString() {
		return "MensajeConAsunto [asunto=" + asunto + ", cuerpo=" + cuerpo + "]";
	}
	
	
}
