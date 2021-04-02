package com.contact.manager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.contact.manager.modal.User;
import com.contact.manager.service.UserService;

public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	UserService service;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = service.getUserByUserName(username);
		if(user == null) {
			throw new UsernameNotFoundException("No such user found");
		}
		
		return new UserDetailImpl(user);
	}

	
}
