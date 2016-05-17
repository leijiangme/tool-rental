package team28.handyman.controllers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import team28.handyman.domain.Clerk;
import team28.handyman.domain.Customer;
import team28.handyman.domain.Tool;
import team28.handyman.domain.ToolType;
import team28.handyman.services.IToolService;

@Controller
@RequestMapping("/tools")
@SessionAttributes("user")
public class ToolController {
	public ToolController() {
		
	}
	
	public ToolController(IToolService cs) {
		this.toolService = cs;
	}
	
	@Resource(name="toolService")
	private IToolService toolService;
	
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView landing(@RequestParam(name="error", defaultValue = "false", required=false) boolean error
			, @ModelAttribute("user") Clerk user) {
		ModelAndView result = new ModelAndView("tools");
		if(error) {
			result.addObject("error", error);
		}

		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/new")
	public ModelAndView signupForm(ToolForm form) {
		return new ModelAndView("new-tool").addObject("ToolForm", form);
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/new")
	public ModelAndView signup(
			@Valid ToolForm form, 
			BindingResult bindingResults,
			HttpSession session) {
				
		if(bindingResults.hasErrors()) {
			return signupForm(form);
		}
		//the binding of the accessories TextArea puts all the content in the first list entry
		//split that text on the \n char and put it back in the list
		String accessory_0 = form.getAccessories().get(0);
		form.getAccessories().clear();
		for (String acc : accessory_0.split("[\\r\\n]+"))
			form.getAccessories().add(acc);
		toolService.save(form);
		session.setAttribute("user", form);
		return new ModelAndView("redirect:/tools");
	}
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/tooldetail")
	public ModelAndView forsaleOrSold(
			@RequestParam(required=false)String forsale,
			@RequestParam(required=false)String sold,
			@RequestParam(required=false)String createserviceorder,
			HttpSession session) {
				
		//can't sell it until it is listed.  that means markforsale()
		Tool t = (Tool)session.getAttribute("tool");
		if (forsale != null)
			toolService.markForSale(t.getToolNumber());
		else if (sold != null)
			toolService.sellTool(t.getToolNumber());
		else if (createserviceorder != null)
			return new ModelAndView("redirect:/createserviceorder");
		//session.setAttribute("user", form);
		return new ModelAndView("redirect:/tools");
	}
	@RequestMapping(method = RequestMethod.GET, path="/createserviceorder")
	public ModelAndView serviceorderform(@ModelAttribute AvailabilitySearchForm availabilitySearchForm, 
			@RequestParam(required=false)String createsoerror,
								  BindingResult bindingResults,
								  HttpSession session) {
		ModelAndView mv = new ModelAndView("createserviceorder").addObject("createsoerror", createsoerror != null);
		return mv;
	}
	@RequestMapping(method = RequestMethod.POST, path = "/createserviceorder")
	public ModelAndView createServiceOrder(
			@RequestParam(required=true)Date startdate,
			@RequestParam(required=true)Date enddate,
			@RequestParam(required=true)float estrepaircost,
			@RequestParam(required=false)String createserviceorder,
			HttpSession session) {
				
		//can't sell it until it is listed.  that means markforsale()
		boolean success = false;
		Tool t = (Tool)session.getAttribute("tool");
		if (createserviceorder != null)
			success = toolService.createServiceOrder(startdate, enddate, t.getToolNumber(), (int)(estrepaircost * 100));
		//session.setAttribute("user", form);
		if (success)
			return new ModelAndView("redirect:/tools");
		else
			return new ModelAndView("createserviceorder").addObject("createsoerror", true);
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView toolDetail(@RequestParam(name = "toolNumber", required = true) Integer toolNumber,
								   @RequestParam(name="lastQuery", required = false) String lastQuery,
								   HttpSession session) {
		ModelAndView result = new ModelAndView();
		
		Tool t = toolService.byId(toolNumber);
		if (t == null)
			result.setViewName(lastQuery == null ? "redirect:/tools/?error=true" : "redirect:"+lastQuery + "&error=true");
		else 
		{
			result.setViewName("tooldetail");
			result.addObject("ToolForm", t);
			session.setAttribute("tool", t);
			result.addObject("isClerk", session.getAttribute("user") instanceof Clerk);
		}
		
		return result;
	}

	@RequestMapping(method = RequestMethod.GET, path="/availablity-search")
	public ModelAndView available(@ModelAttribute AvailabilitySearchForm availabilitySearchForm, 
								  BindingResult bindingResults,
								  HttpSession session) {
		ModelAndView mv = new ModelAndView("tools-available-search");
		mv.addObject("cancel", session.getAttribute("user") instanceof Customer ? "/customers" : "/clerks");
		availabilitySearchForm.setToolTypes(toolService.getToolTypes());
		return mv;
	}
	
	@RequestMapping(method = RequestMethod.GET, path="/availablity-search-results")
	public ModelAndView availableResults(@Valid @ModelAttribute AvailabilitySearchForm availabilitySearchForm,
			 								BindingResult bindingResults,
			 								HttpSession session,
			 								HttpServletRequest req) {
		if(bindingResults.hasErrors()) {
			return available(availabilitySearchForm, bindingResults, session);
		}
		else if (availabilitySearchForm.startDt.after(availabilitySearchForm.endDt))
		{
			bindingResults.rejectValue("startDt", "starting-too-late", "Start date must be before or on the End Date.");
			return available(availabilitySearchForm, bindingResults, session);
			
		}

		ModelAndView mv = new ModelAndView("tools-available");
		List<Tool> availableOn = toolService.availableOn(availabilitySearchForm.getStartDt(), availabilitySearchForm.endDt, availabilitySearchForm.toolType);
		mv.addObject("available", availableOn);
		mv.addObject("lastQuery", makeLastQuery(req));
	
		return mv;
	}
	
	private String makeLastQuery(HttpServletRequest request) {
		return request.getRequestURL().toString() + "?" + request.getQueryString();
	}
	
	public static final class ToolForm extends Tool {
		private static final BigDecimal AS_PENNIES = new BigDecimal(100);
		private static final long serialVersionUID = 1L;
		private BigDecimal rentalForHumans;
		private BigDecimal depositForHumans;
		private BigDecimal purchaseForHumans;
		public BigDecimal getRentalForHumans() {
			return rentalForHumans;
		}
		public void setRentalForHumans(BigDecimal rentalForHumans) {
			this.rentalForHumans = rentalForHumans;
			this.setRentalPrice(rentalForHumans.multiply(AS_PENNIES).intValue());
		}
		public BigDecimal getDepositForHumans() {
			return depositForHumans;
		}
		public void setDepositForHumans(BigDecimal depositForHumans) {
			this.depositForHumans = depositForHumans;
			this.setDepositPrice(depositForHumans.multiply(AS_PENNIES).intValue());
		}
		public BigDecimal getPurchaseForHumans() {
			return purchaseForHumans;
		}
		public void setPurchaseForHumans(BigDecimal purchaseForHumans) {
			this.purchaseForHumans = purchaseForHumans;
			this.setPurchasePrice(purchaseForHumans.multiply(AS_PENNIES).intValue());
		}
		
	}
	
	public static class AvailabilitySearchForm {
		public AvailabilitySearchForm() {}
		
		public AvailabilitySearchForm(List<ToolType> tt) {
			this.toolTypes = tt;
		}

		@NotNull
		@Min(1)
		private Integer toolType;

		@NotNull
		@DateTimeFormat(pattern = "MM/dd/yyyy")
		private Date startDt;

		@NotNull
		@DateTimeFormat(pattern = "MM/dd/yyyy")
		private Date endDt;
		
		private List<ToolType> toolTypes;

		public Integer getToolType() {
			return toolType;
		}

		public void setToolType(Integer toolType) {
			this.toolType = toolType;
		}

		public Date getStartDt() {
			return startDt;
		}

		public void setStartDt(Date startDt) {
			this.startDt = startDt;
		}

		public Date getEndDt() {
			return endDt;
		}

		public void setEndDt(Date endDt) {
			this.endDt = endDt;
		}
		
		public List<ToolType> getToolTypes() {
			return toolTypes;
		}

		public void setToolTypes(List<ToolType> toolTypes) {
			this.toolTypes = toolTypes;
		}
	}
}
