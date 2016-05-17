package team28.handyman.controllers;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import team28.handyman.domain.Clerk;
import team28.handyman.domain.Customer;
import team28.handyman.domain.Reservation;
import team28.handyman.domain.User;
import team28.handyman.services.CustomerService;
import team28.handyman.services.ICustomerService;
import team28.handyman.services.IReservationService;
import team28.handyman.services.UserService;

@Controller
@RequestMapping("/clerks/reservation-tool-pickup")
public class ReservationToolPickupController {
	public ReservationToolPickupController() {
		// This is the constructor for Spring.
	}

	
	public ReservationToolPickupController(IReservationService cs) {
		this.reservationService = cs;
	}
	
	public ReservationToolPickupController(ICustomerService cs) {
		this.customerService = cs;
	}
	@Resource(name="reservationService")
	private IReservationService reservationService;

	@Resource(name="customerService")
	private ICustomerService customerService;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView toolPickup(@RequestParam(required=true) Integer reservation,HttpSession session) {
		
		ModelAndView result = new ModelAndView("reservation-tool-pickup");
		Reservation r = reservationService.getById(reservation);
		session.setAttribute("reservation", r);
		
		return result;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView toolPickupComplete(
			@RequestParam(required=true) String creditcard
			,@RequestParam(required=true) String expirationdate
			,@RequestParam(required=true) String complete_pickup
			,HttpSession session) {
		
		Reservation r = (Reservation) session.getAttribute("reservation");
		Clerk c = (Clerk) session.getAttribute("user");
		r.setPickupClerk(c.getUsername());
		reservationService.reservationPickup(r.getReservationNumber(), c.getUsername(), creditcard, expirationdate);
		r.setCreditCard(creditcard)
			.setExpirationDate(expirationdate);
		return new ModelAndView("reservation-tool-pickup-contract");
	}
}
