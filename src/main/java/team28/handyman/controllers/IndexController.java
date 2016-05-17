package team28.handyman.controllers;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import team28.handyman.domain.Customer;
import team28.handyman.domain.User;
import team28.handyman.services.InvalidCredentialsException;
import team28.handyman.services.NoSuchCustomerException;
import team28.handyman.services.UserService;

@Controller
@RequestMapping("/")
public class IndexController {
	public IndexController() {
		// This is the constructor for Spring.
	}

	public IndexController(UserService userService) {
		// This is for the testing.
		super();
		this.userService = userService;
	}

	@Resource(name = "userService")
	private UserService userService;
	
	@ModelAttribute("form")
	public LoginForm getDefault() {
		return new LoginForm();
	}

	@RequestMapping(method = RequestMethod.GET)
	@Transactional
	public ModelAndView greeting(
			@ModelAttribute LoginForm form,
				BindingResult bindingResult,
			@RequestParam(name="error", defaultValue = "false", required=false) boolean error, 
			HttpSession session) {
		ModelAndView result = new ModelAndView("index");
		if(error) {
			result.addObject("error", error);
		}
		session.invalidate();
		result.addObject("form", form);
		return result;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView login(	@ModelAttribute @Valid LoginForm loginForm,
			 					BindingResult bindingResult,
			 					HttpSession session) {
		if(bindingResult.hasErrors()) {
			return new ModelAndView("index").addObject("loginForm", loginForm);
		}
		session.removeAttribute("user");
		ModelAndView result = new ModelAndView();
		
		User usr = null;
		String to = "redirect:/?error=true";
		try {
			usr = userService.fromCredentials(loginForm.getUsername(), loginForm.getPassword(), loginForm.getType());
			to = usr  instanceof Customer ? "redirect:/customers" :"redirect:/clerks";
		} catch(InvalidCredentialsException ex) {
			to = to + "&invalid_credentials=true";
		} catch(NoSuchCustomerException ex) {
			to = "redirect:/customers/new";
		}
		result.setViewName(to);
		result.addObject("user", usr);
		session.setAttribute("user", usr);
		
		return result;
	}
	
	public static final class LoginForm {
		@NotNull
		@NotEmpty
		private String username;

		@NotNull
		@NotEmpty
		private String password;

		@NotNull
		private String type;
		
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
}
