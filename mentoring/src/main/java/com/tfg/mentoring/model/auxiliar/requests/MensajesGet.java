package com.tfg.mentoring.model.auxiliar.requests;

public class MensajesGet {

	private long id;
	private boolean mentor;
	

	public MensajesGet(long id, boolean mentor) {
		super();
		this.id = id;
		this.mentor = mentor;
	}



	public MensajesGet() {
		super();
	}

	

	

	public long getId() {
		return id;
	}



	public void setId(long id) {
		this.id = id;
	}



	public boolean isMentor() {
		return mentor;
	}

	public void setMentor(boolean mentor) {
		this.mentor = mentor;
	}



	@Override
	public String toString() {
		return "MensajesGet [id=" + id + ", mentor=" + mentor + "]";
	}
	
	


	

	
	
	
}
