package com.tfg.mentoring.model.auxiliar.DTO;

public class UserAuthDTO {
	private String username;
	private boolean mentor;
	
	public UserAuthDTO(String username, boolean mentor) {
		super();
		this.username = username;
		this.mentor = mentor;
	}

	public UserAuthDTO() {
		super();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isMentor() {
		return mentor;
	}

	public void setMentor(boolean mentor) {
		this.mentor = mentor;
	}

	@Override
	public String toString() {
		return "UserAuthDTO [username=" + username + ", mentor=" + mentor + "]";
	}
	
	
}
