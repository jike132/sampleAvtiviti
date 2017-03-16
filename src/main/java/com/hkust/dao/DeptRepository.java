package com.hkust.dao;

import java.util.List;

import org.springframework.data.repository.Repository;

import com.hkust.domain.Approver;
import com.hkust.domain.Dept;

public interface DeptRepository extends Repository<Dept,String> {
	List<Dept> findByDescStartingWith(String name);
	Dept findByCode(String code);
	List<Dept> findAll();
}
