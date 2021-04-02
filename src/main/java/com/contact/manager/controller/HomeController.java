package com.contact.manager.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.contact.manager.helper.Message;
import com.contact.manager.modal.User;
import com.contact.manager.service.UserService;

@Controller
public class HomeController {
	@Autowired
	UserService service;
	@Autowired
	BCryptPasswordEncoder passwordEncoder;
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About");
		return "about";
	}
	@RequestMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title","SignUp");
		m.addAttribute("user", new User());
		return "signup";
	}
	@RequestMapping(value = "/register",method = RequestMethod.POST)
	public String processform(@Valid @ModelAttribute("user") User user,BindingResult result,@RequestParam( value = "agreement",defaultValue = "false") boolean isagreed,Model model,HttpSession session) {
		System.out.println(user+""+isagreed);
		
		 if(result.hasErrors()) {
			 System.out.println(result);
//			 System.out.println("inside error");
			 return "signup";
		 }
		 
		try {
			
			
				 System.out.println("inside else block");
				 if(!isagreed) {
						System.out.println("You have not agreed term and conditions");
						throw new Exception("You have not agreed term and conditions");
					}
					else {
						user.setRole("ROLE_USER");
						user.setIsenabled(true);
						user.setImageUrl("default.png");
						user.setPassword(passwordEncoder.encode(user.getPassword()));
						this.service.save(user); // save user through service;
						model.addAttribute("user", new User());
						session.setAttribute("msg",new Message("Registered Successfully","alert-success"));
					}
			
			
		}
		catch(Exception e) {
			session.setAttribute("msg",new Message("Something went wrong !! "+e.getMessage(),"alert-danger"));
			 model.addAttribute("user",user);
			e.printStackTrace();
//			return "signup";
		}
		return "signup";
	}
	@RequestMapping("/signin")
	public String login(Model m) {
		return "login";
	}
}
