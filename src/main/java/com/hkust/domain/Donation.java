package com.hkust.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;

import javax.persistence.Id;

@Entity
@Table(name="PS_ZR_DM_DON_TBL")
public class Donation {
	public static String OPEN="0";
	public static String FINISH="1";
		
	@Id
	@Column(name="ZR_DM_DON_ID",length=10,nullable=false)
	private String id;
	
	@Column(name="ZR_DM_DNTR_NAM",length=15,nullable=false)
	private String name;
	
	//@Future(message = "The date must be greater than today")
	@NotNull(message = "Date is required.")
	@Column(name="ZR_DM_DON_DAT",nullable=true)
	private Date date;
	
	@NotNull(message = "AMT is required.")
	@Column(name="ZR_DM_AMT",precision=11,scale=2,nullable=true)
	private BigDecimal amt;
	
	@Column(name="ZR_DM_STATUS",length=1,nullable=false)
	private String status = "0";

	private String approver;
	private String dept;
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public BigDecimal getAmt() {
		return amt;
	}

	public void setAmt(BigDecimal amt) {
		this.amt = amt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getApprover() {
		return approver;
	}

	public void setApprover(String approver) {
		this.approver = approver;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	@Override
	public String toString() {
		return "Donation [id=" + id + ", name=" + name + ", date=" + date + ", amt=" + amt + ", status=" + status
				+ ", approver=" + approver + ", dept=" + dept + "]";
	}

	
}
