package com.contact.manager.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contact.manager.helper.Message;
import com.contact.manager.modal.Contact;
import com.contact.manager.modal.User;
import com.contact.manager.service.ContactService;
import com.contact.manager.service.UserService;

import javassist.expr.NewArray;
import net.bytebuddy.asm.Advice.This;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	UserService service;
	@Autowired
	ContactService contactService;
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@RequestMapping("/index")
	public String user_dashboard(Model model, Principal principal) {
		String name = principal.getName();
		System.out.println("active user name is " + name);
		User user = service.getUserByUserName(name); // get user by name
		model.addAttribute("title", "Dashboard");
		model.addAttribute("user", user);
		return "normal_user/dashboard";
	}

	@RequestMapping("/add_contact")
	public String addContactForm(Model model, Principal principal) {
		String name = principal.getName();
		User user = service.getUserByUserName(name);
		model.addAttribute("title", "add contacts");
		model.addAttribute(user);
		model.addAttribute("contact", new Contact());
		return "normal_user/add_contact";
	}

	@RequestMapping(value = "/process_contact", method = RequestMethod.POST)
	public String processAddContactForm(@Valid @ModelAttribute Contact contact, BindingResult result,
			@RequestParam("image") MultipartFile file, Model model, Principal principal, HttpSession session) {
		String name = principal.getName();
		User user = service.getUserByUserName(name);
		System.out.println(result);
		model.addAttribute(user);
		if (result.hasErrors()) {
			return "normal_user/add_contact";
		}
		try {

			String filename = file.getOriginalFilename();
			System.out.println(filename);

			if (file.isEmpty()) {
//				throw new Exception("please select file ");
				contact.setImageUrl("default.png");
			} else {
				// set it to database
				contact.setImageUrl(filename);
				// upload to folder
				File upload = new ClassPathResource("static/img").getFile();
				Files.copy(file.getInputStream(), Paths.get(upload.getAbsolutePath() + File.separator + filename),
						StandardCopyOption.REPLACE_EXISTING);
			}
//				
			contact.setUser(user); // setting the object of user to contact
			user.getContacts().add(contact);
//
			this.contactService.save(contact);
			this.service.save(user);
			model.addAttribute("title", "add contacts");

			System.out.println("i am a new user");
//			}

//		

			System.out.println(result.getFieldErrors());

			System.out.println(contact);
			session.setAttribute("msg", new Message("Contact added successfully ", "alert-success"));
		} catch (DataIntegrityViolationException e) {
			System.out.println("Something happened wrong" + e);
			session.setAttribute("msg", new Message("something went wrong...!! Email already taken", "alert-danger"));

		} catch (Exception e) {
			System.out.println("Something happened wrong" + e);
			e.printStackTrace();
			session.setAttribute("msg", new Message("something went wrong...!!" + e.getMessage(), "alert-danger"));

		}

		return "normal_user/add_contact";
	}

	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principle) {
		String name = principle.getName();
		User userByName = this.service.getUserByUserName(name);
		// current page , records per page
//		Sort sort = new Sort(null);
		PageRequest pagerequest = PageRequest.of(page, 5); // page number and records per page
		Page<Contact> contacts = this.contactService.findContactsByUserId(userByName.getId(), pagerequest);
//		contacts.forEach(e->System.out.println(e));
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentpage", page);
		model.addAttribute("totalpages", contacts.getTotalPages());
		model.addAttribute("user", userByName);
		model.addAttribute("title", "View Contacts");
		return "normal_user/show_contacts";
	}
	// handler for showing particular contact

	@GetMapping("/{c_Id}/contact/")
	public String showContactById(@PathVariable("c_Id") Integer contact_Id, Model model, Principal principle) {
		System.out.println(contact_Id);

		Optional<Contact> contactOptional = this.contactService.findById(contact_Id);
		Contact contact = contactOptional.get();
		// security check
		String username = principle.getName();
		User user = this.service.getUserByUserName(username);
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
		}
		model.addAttribute("title", "contact detail");
		return "normal_user/contact_detail.html";
	}

	@ModelAttribute
	public void commonMethod(Principal p, Model m) {
		String name = p.getName();
		User user = this.service.getUserByUserName(name);
		m.addAttribute("user", user);
	}

	public String getUserName(Principal principle) {
		return principle.getName();
	}

	// handler for delete contact
	@RequestMapping("/contact/delete/{c_Id}")
	public String deleteContact(@PathVariable("c_Id") Integer id, Model model, Principal principle,
			HttpSession session) {
		Contact contact = this.contactService.findById(id).get();
		System.out.println(contact);
		String name = getUserName(principle);
		User user = this.service.getUserByUserName(name);

		if (contact.getUser().getId() == user.getId()) {
			// delete contact
//			contact.setUser(null); //unlinking
			System.out.println(contact);
			this.contactService.deleteById(id);
			// delete image
			String fileName = contact.getImageUrl();
			if (fileName != null)
				try {
					File file = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(file.getAbsolutePath() + File.separator + fileName);
					Files.delete(path);
					contact.setImageUrl(null);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("something happened wrong");
					e.printStackTrace();
				}

			session.setAttribute("msg", new Message("contact deleted successfully", "alert-success"));
		}
		System.out.println("delete contact");
		return "redirect:/user/show_contacts/0";
	}

//	handler for update contact
	@RequestMapping("/contact/{cid}/update")
	public String updateContact(@PathVariable("cid") Integer id, Model model) {
		Contact contact = this.contactService.findById(id).get();
		model.addAttribute("title", "update contact");
		model.addAttribute("contact", contact);
		return "normal_user/update_contact.html";
	}

	// process updated contact
	@PostMapping("/contact/process_updatedcontact")
	public String processUpdateForm(@ModelAttribute("contact") Contact contact,
			@RequestParam("image") MultipartFile file, Model model, HttpSession session, Principal principal) {
//		System.out.print(contact);
		System.out.print("contact id" + contact.getcId());
		try {
			Contact oldContact = this.contactService.findById(contact.getcId()).get();
			if (!file.isEmpty()) {
				// delete old imageFile file= new ClassPathResource("static/img").getFile();
				File fileTodelete = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(fileTodelete.getAbsolutePath() + File.separator + oldContact.getImageUrl());
				Files.delete(path);

				contact.setImageUrl(file.getOriginalFilename());
//				// upload new image
				File upload = new ClassPathResource("static/img").getFile();
				Files.copy(file.getInputStream(),
						Paths.get(upload.getAbsolutePath() + File.separator + file.getOriginalFilename()),
						StandardCopyOption.REPLACE_EXISTING);
			} else {
				contact.setImageUrl(oldContact.getImageUrl());
			}
			contact.setUser(this.service.getUserByUserName(principal.getName()));

			Contact save = this.contactService.save(contact);
//			System.out.print("contact updated");
			session.setAttribute("msg", new Message("contact updated successfully..!", "alert-success"));
			System.out.println(save);
		} catch (Exception e) {
			session.setAttribute("msg", new Message("Something happened wrong..!", "alert-danger"));
		}
		return "redirect:/user/contact/" + contact.getcId() + "/update";
	}
	
	@RequestMapping("/profile")
	public String profile(Model model,Principal principal) {
		model.addAttribute("title","profile");
		model.addAttribute("numberOfContacts",this.service.findById(this.service.getUserByUserName(principal.getName()).getId()).get().getContacts().size());
		return "normal_user/profile";
	}
	@RequestMapping("/settings")
	public String settings(){
		return "normal_user/settings.html";
	}
	@RequestMapping(value = "/changepassword",method = RequestMethod.POST)
	public String changePassword(@RequestParam(value = "oldpassword",required = true)String oldPassword,@RequestParam(value = "newpassword",required = true) String newpassword,Principal principal,HttpSession session){
		User user = this.service.getUserByUserName(principal.getName());
		if(bCryptPasswordEncoder.matches(oldPassword,user.getPassword())){
			user.setPassword(bCryptPasswordEncoder.encode(newpassword));
			this.service.save(user);
			session.setAttribute("msg", new Message("password changed successfully","alert-success"));
		}
		else{
			session.setAttribute("msg", new Message("Incorrect old password","alert-danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}


}
