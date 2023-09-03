package com.smart.controller;


import java.io.File;
import com.razorpay.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;



@Controller
@ComponentScan
public class HomeController {
	
	@Autowired
	private EmailService emailService;
	
	Random random=new Random();
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home - Smart Contact Manager");
		
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About - Smart Contact Manager");
		
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	//This handler for registering user
	@RequestMapping(value="/do_register",method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,
			@RequestParam(value="agreement",defaultValue="false") boolean agreement,
			Model model, HttpSession session) {
		try {
			if(!agreement) {
				System.out.println("You h not argeed terms & cond");
				throw new Exception("You h not argeed terms & cond");
			}
			if(result1.hasErrors()) {
				System.out.println("ERROR"+result1.toString());
				model.addAttribute("user",user);
				return "singup";
			}
			
			user.setRole("Role_User");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement "+agreement);
			System.out.println("User "+user);
			
			User result=this.userRepository.save(user);
			
			
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfully Register !!", "alert-success"));
			return "do_register";
			
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
			return "signup";
		}
		
		
		
	}

	//Handler for Custom-Login
	@GetMapping("/signin")
	public String customeLogin(Model model) {
		model.addAttribute("title","Login Page");
		
		return "login";
	}
	
	//User Dashboard
	@RequestMapping("/dashboard")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","User Dashboard");
		String userName=principal.getName();
		System.out.println("UserName "+userName);
		
		//Get the user using DB
		User user=userRepository.getUserByUserName(userName);
		
		System.out.println("USER "+user);
		
		model.addAttribute("user",user);
				
		return "user_dashboard";
	}
	
	//Open Add Form Handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model,Principal principal) {
		model.addAttribute("title","Add Contact");
		String userName=principal.getName();
		System.out.println("UserName "+userName);
		
		User user=userRepository.getUserByUserName(userName);	
		model.addAttribute("user ",user);
		model.addAttribute("contact",new Contact());
		
		return "add_contact_form";
	}
	
	//Processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, 
			Principal principal, HttpSession session) {
		try {
			String name=principal.getName();
			
			User user=this.userRepository.getUserByUserName(name);
			
			//Proceesing and uploading file
			if(file.isEmpty()) {
				System.out.println("File empty");
				contact.setImage("madhav.jpg");
			}else {
				contact.setImage(file.getOriginalFilename());
				
				File savefile=new ClassPathResource("static/image").getFile();
				
				Path path=Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is uploaded");
			}
			user.getContacts().add(contact);
			contact.setUser(user);
			this.userRepository.save(user);
			System.out.println("Added to Data Base");			
			System.out.println("DATA "+contact);
			//Success Message.....
			session.setAttribute("meassge", new Message("Your 1-Contact is Saved !!Add more contact","success"));
			
		} catch (Exception e) {
			e.printStackTrace();
			//Error Message.....
			session.setAttribute("meassge", new Message("somethignwent wrong !! Try again","danger"));
		}
		return "add_contact_form";
	}
	
	//Show Contact Handler
	//Per Page =5[n]
	//Current Page =0[page]
	@GetMapping("/show-contact/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title","View Contact Details");
		//Contact list send from DB Way 1 & Way 2
//		String userName=principal.getName();
//		User user=this.userRepository.getUserByUserName(userName);
//		List<Contact> contacts= user.getContacts();
		String userName = principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		//CurrentPage-page
		//Congtact Perpage -5
		Pageable pageable= PageRequest.of(page, 4);
		
		Page<Contact> contacts= this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "show_contacts";
	}
	
	//Showing Particular Details
	
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		System.out.println("CID"+cId);
		
		Optional<Contact> contactOptional= this.contactRepository.findById(cId);
		
		Contact contact= contactOptional.get();
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		if(user.getId()==contact.getUser().getId()) {
			
			model.addAttribute("contact",contact);
		}	
		
		
		return "contact_detail";
	}
	
	//Delete Contact Handler
	@GetMapping("/delete/{cId}")
	@Transactional
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,HttpSession session, Principal principal) {
		
		Contact contact= this.contactRepository.findById(cId).get();
		System.out.println("Contact "+contact.getcId());		
		
//		contact.setUser(null);
//		
//		this.contactRepository.delete(contact);
		
		User user=this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		System.out.println("Deleted");
		session.setAttribute("message", new Message("Contact Deleted Sccessfully....","success"));
		
		return "redirect:/show-contact/0";
	}
	
	//Update Contact Handler
	
	@PostMapping("/update-contact/{cId}")
	public String updateContact(@PathVariable("cId")Integer cId,Model m) {
		
		m.addAttribute("title","Update Contact");
		Contact contact= this.contactRepository.findById(cId).get();
		m.addAttribute("contact",contact);
		
		return "redirect:update_form";
	}

	//Update Contact Handler 
	@PostMapping("process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal) {
		
		try {
			Contact oldContactDetail= this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
				
				File deletefile=new ClassPathResource("static/image").getFile();
				File file1=new File(deletefile,oldContactDetail.getImage());
				file1.delete();
				
				File savefile=new ClassPathResource("static/image").getFile();
				
				Path path=Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user=userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message",new Message("Your Contact is Updated....","success"));
						
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Contact Name"+contact.getName());
		System.out.println("Contact Id"+contact.getcId());
		
		return "redirect:/"+contact.getcId()+"/contact";
	}
	
	//User Profile Stting
	@GetMapping("/profile")
	public String yourProfile(Model model,Principal principal) {
		String userName=principal.getName();
		User user=userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		model.addAttribute("title","Your Profile Details");
		
		return "profile";
	}
	
	//Search Contact Handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal ){
		System.out.println(query);
		User user= this.userRepository.getUserByUserName(principal.getName());
		List<Contact> contact= this.contactRepository.findByNameContainingAndUser(query, user);
		
		return ResponseEntity.ok(contact);
	}
	
	//Open Setting Handler
	@GetMapping("/settings")
	public String openSettings() {
		
		return "settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal,HttpSession session) {
//		System.out.println("old"+oldPassword);
//		System.out.println("new"+newPassword);
		String userName=principal.getName();
		User currentUser=this.userRepository.getUserByUserName(userName);
		//System.out.println(currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password change","alert-success"));
			
		}else {
			
			session.setAttribute("message", new Message("Please Enter correct old password","alert-warning"));
			return "settings";
		}
		
		return "user_dashboard";
		//return "redirect:/index";
	}
	
	//Email id for Forget Handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		
		return "forgot_email_form";
	}
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		System.out.println("Email "+email);
		
		//Generating otp of 4 digit
		
		
		int otp=random.nextInt(999999);	
		System.out.println("OTP "+otp);
		
		//Write code for sent otp to email
		String subject="OTP from SCM";
		String message=""
				+"<div style='border:1px solid #e2e2e2; padding:20px'>"
				
				+"OTP is "
				+"<h2>"
				+"<b>"+otp
				+"</n>"
				+"</h2>"
				+"</div>";
		String to=email;
		boolean flag=this.emailService.sendEmail(subject, message, to);
		if(flag) {
			
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			
			return "verify_otp";
		}else {
			//session.setAttribute("message", "Check your email id !!");
			
			return "forgot_email_form";
		}
		
	}
	//Verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session) {
		int myOtp=(int)session.getAttribute("myotp");
		String email=(String)session.getAttribute("email");
		if(myOtp==otp) {
			//Password Change From
			User user=this.userRepository.getUserByUserName(email);
			if(user==null) {
				//Send Error Message
				session.setAttribute("message", "User doesnot Exists with this email");
				
				return "forgot_email_form";
				
			}else {
				//Send change password form
				
			}
			return "password_change_form";
		}else {
			session.setAttribute("message", "You have entered wrong otp");
			return "verify_otp";
		}
		
	}
	//Change Password
	@PostMapping("/change-passwords")
	public String changeUserPassword(@RequestParam("newpassword") String newpassword,HttpSession session) {
		
		String email=(String)session.getAttribute("email");
		User user= this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		
		session.setAttribute("message", "You have entered wrong otp");
		return "redirect:/signin?change=password changed successfuly..";
		
	}
	//Creating Order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data) throws Exception {
		System.out.println(data);
		int amt= Integer.parseInt(data.get("amount").toString());
		
		var client= new RazorpayClient("rzp_test_pyPtfg1kQTV9EK", "qffYcoi4QGE9vYW62BoAzefa");
		JSONObject ob=new JSONObject();
		ob.put("amount", amt*100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_235425");
		
		//Creating new order
		
		Order order=client.orders.create(ob);
		System.out.println(order);
		
		return order.toString();
	}
		
}
