package com.hkust.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="PS_ZR_DM_DEPT_V")
public class Dept {

	@Id
	@Column(name="ZR_DEPTID",length=10,nullable=false)
	private String id;
	
	@Column(name="ZR_DESCRSHORT",length=10,nullable=false)
	private String code;
	
	@Column(name="ZR_DESCR",length=30,nullable=false)
	private String desc;
	
	@Column(name="ZR_EFF_STATUS",length=1,nullable=false)
	private String status;

	@OneToMany(targetEntity=Approver.class,mappedBy="dept",fetch=FetchType.LAZY)
	private List<Approver> approvers = new ArrayList<Approver>();
	
	public Dept() {
		super();
	}
	
	@Transient
	@JsonIgnore
	public List<Approver> getApproverList() {
		return approvers;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
