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
@RequestMapping("/clerks/reservation-tool-dropoff")
public class ReservationToolDropoffController {
	public ReservationToolDropoffController() {
		// This is the constructor for Spring.
	}

	public ReservationToolDropoffController(ICustomerService cs) {
		this.customerService = cs;
	}
	
	public ReservationToolDropoffController(IReservationService cs) {
		this.reservationService = cs;
	}
	@Resource(name="customerService")
	private ICustomerService customerService;
	
	@Resource(name="reservationService")
	private IReservationService reservationService;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView toolDropoff(@RequestParam(required=true) Integer reservation,HttpSession session) {
		
		ModelAndView result = new ModelAndView("reservation-tool-dropoff");
		Reservation r = reservationService.getById(reservation);
		session.setAttribute("reservation", r);
		return result;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView toolDropoffComplete(
			@RequestParam(required=true) String complete_dropoff
			,HttpSession session) {
		
		Reservation r = (Reservation) session.getAttribute("reservation");
		Clerk c = (Clerk) session.getAttribute("user");
		reservationService.reservationDropoff(r.getReservationNumber(), c.getUsername());
		return new ModelAndView("reservation-tool-dropoff-receipt");
	}
}
