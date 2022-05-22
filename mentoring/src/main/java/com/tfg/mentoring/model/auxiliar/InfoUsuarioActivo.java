package com.tfg.mentoring.model.auxiliar;

//Información que se guarda de cada usuario que esta activo
public class InfoUsuarioActivo {
	private boolean enChat;//Indica si el usuario esta en la ventana de chat o no, esto evitará que le lleguen notificaciones acerca del chat cuando esta dentro
	//Y evitará que el servidor envie mensajes cuando el usuario no esta en el chat
	

	public InfoUsuarioActivo() {
		super();
		this.enChat = false;
	}


	public boolean isEnChat() {
		return enChat;
	}


	public void setEnChat(boolean enChat) {
		this.enChat = enChat;
	}


	@Override
	public String toString() {
		return "InfoUsuarioActivo [enChat=" + enChat + "]";
	}


	
	
	
	
	
}
