package com.hkust.dao;

import java.util.List;

import org.springframework.data.repository.Repository;

import com.hkust.domain.Approver;

public interface ApproverRepository extends Repository<Approver,String>{
	public List<Approver> findAll();
}
