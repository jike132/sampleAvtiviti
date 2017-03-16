package com.hkust.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="PS_ZR_DM_APPROVER")
public class Approver {
	@Id
	@Column(name="ZR_DM_USER_NAM",length=30,nullable=false)
	private String name;
	
	@ManyToOne(targetEntity=Dept.class,fetch=FetchType.LAZY)
	@JoinColumn(name="ZR_DEPTID")
	private Dept dept;

	public Approver() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Dept getDept() {
		return dept;
	}

	public void setDept(Dept dept) {
		this.dept = dept;
	}
	
}
