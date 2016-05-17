package team28.handyman.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/clerks")
public class ClerkController {
	public ClerkController() {
		
	}
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView landing(HttpSession session) {

		ModelAndView result = new ModelAndView();

		return result;
	}
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView reservationCheckout(
			@RequestParam(required=false) Integer reservationNumber
			,@RequestParam(required=false) Integer reservationNumber2
			,@RequestParam(required=false) String check_out
			,@RequestParam(required=false) String check_in
			,HttpSession session) 
	{
		String to = "redirect:/clerks";
		if (check_out != null) {
			to = reservationNumber != null ? "redirect:/clerks/reservation-tool-pickup?reservation=" + reservationNumber : to + "?err=noPickupReservation";
		}
		else if (check_in != null) {
			to = reservationNumber2 != null ? "redirect:/clerks/reservation-tool-dropoff?reservation=" + reservationNumber2 :to + "?err=noDropoffReservation";
		}
		
		return new ModelAndView(to);
	}
}
