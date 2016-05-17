package team28.handyman.controllers;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import team28.handyman.domain.Customer;
import team28.handyman.services.ICustomerService;
import team28.handyman.services.IReservationService;

@Controller
@RequestMapping("/customers")
public class CustomerController {
	
	public CustomerController() {
		
	}
	
	public CustomerController(ICustomerService cs) {
		this.customerService = cs;
	}
	
	@Resource(name="customerService")
	private ICustomerService customerService;
	
	@Resource(name="reservationService")
	private IReservationService reservationService;
	
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView landing() {
		ModelAndView result = new ModelAndView("customers");

		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/new")
	public ModelAndView signupForm(CustomerForm form) {
		return new ModelAndView("new-customer").addObject("customerForm", form);
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/profile")
	public ModelAndView profile(@RequestParam(value = "username", required = true) String username, HttpSession session) {
		final ModelAndView next = new ModelAndView();
		Customer customer = (Customer) session.getAttribute("user");
		if(customer == null || !customer.getUsername().equalsIgnoreCase(username)) {
			next.setViewName("redirect:/customers");
		} else {
			next.addObject("reservations", reservationService.forUser(username));
			next.setViewName("profile");
		}
		return next;
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/new")
	public ModelAndView signup(
			@Valid CustomerForm form, 
			BindingResult bindingResults,
			HttpSession session) {
				
		if(bindingResults.hasErrors()) {
			return signupForm(form);
		}		
		customerService.save(form);
		session.setAttribute("user", form);
		return new ModelAndView("redirect:/customers");
	}
	
	public static final class CustomerForm extends Customer {
		private static final long serialVersionUID = 1L;
		
		@NotNull
		@Size(min=1, max=60)
		private String confirm;
		
		@NotNull
		@Size(min=3, max=3)
		private String homeAreaCode;
		
		@NotNull
		@Size(min=8, max=8)
		private String homeNumber;
		
		@NotNull
		@Size(min=3, max=3)
		private String workAreaCode;
		
		@NotNull
		@Size(min=8, max=8)
		private String workNumber;

		public String getHomeAreaCode() {
			return homeAreaCode;
		}

		public void setHomeAreaCode(String homeAreaCode) {
			this.homeAreaCode = homeAreaCode;
			fillin();
		}

		public String getHomeNumber() {
			return homeNumber;
		}

		public void setHomeNumber(String homeNumber) {
			this.homeNumber = homeNumber;

			fillin();
		}

		public String getWorkAreaCode() {
			return workAreaCode;
		}

		public void setWorkAreaCode(String workAreaCode) {
			this.workAreaCode = workAreaCode;

			fillin();
		}

		public String getWorkNumber() {
			return workNumber;
		}

		public void setWorkNumber(String workNumber) {
			this.workNumber = workNumber;
			fillin();
		}

		public String getConfirm() {
			return confirm;
		}

		public void setConfirm(String confirm) {
			this.confirm = confirm;
		}
		
		protected void fillin() {
			setHomePhone("(" + homeAreaCode + ") " + homeNumber);
			setWorkPhone("(" + workAreaCode + ") " + workNumber);
		}
	}
}
