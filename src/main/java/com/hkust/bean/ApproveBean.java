package com.hkust.bean;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

@Component
public class ApproveBean {
	
	private String id;
	
	@NotNull(message = "Result is required.")
	private String result;
	
	private String dept;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public ApproveBean() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
