package com.tfg.mentoring.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="extensiones")
public class Extension {

	@Id
	@Column(name = "extension", nullable=false)
	private String extension;

	public Extension(String extension) {
		super();
		this.extension = extension;
	}

	public Extension() {
		super();
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	@Override
	public String toString() {
		return "Extension [extension=" + extension + "]";
	}
}
