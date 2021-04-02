package com.contact.manager.service;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.contact.manager.modal.User;

public interface UserService extends CrudRepository<User, Integer>{

	@Query("select u from User u where u.email =:email")
	public User getUserByUserName(@Param("email") String email);
}
