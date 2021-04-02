package com.contact.manager.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.contact.manager.modal.Contact;

public interface ContactService extends CrudRepository<Contact, Integer>{

//	@Query("select c from Contact c where c.user.id =:userId ")
//	public List<Contact> findContactsByUserId(@Param("userId")int userId);
	@Query("select c from Contact c where c.user.id =:userId ")
	public Page<Contact> findContactsByUserId(@Param("userId")int userId,Pageable pageable);
}
