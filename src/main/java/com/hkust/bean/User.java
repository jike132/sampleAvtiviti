package com.hkust.bean;

import java.io.Serializable;

import org.springframework.stereotype.Component;

@Component
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private int role;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User() {
		super();
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

}
