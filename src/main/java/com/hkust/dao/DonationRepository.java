package com.hkust.dao;

import org.springframework.data.repository.CrudRepository;

import com.hkust.domain.Donation;

public interface DonationRepository extends CrudRepository<Donation,String>{

}
