package team28.handyman.controllers;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import team28.handyman.domain.Customer;
import team28.handyman.domain.Tool;
import team28.handyman.domain.ToolType;
import team28.handyman.services.Helper;
import team28.handyman.services.IReservationService;
import team28.handyman.services.IToolService;

@Controller
@RequestMapping("/reservations")
@SessionAttributes("reservationForm")
public class ReservationController {
	
	@Resource(name="toolService")
	private IToolService toolService;
	
	@Resource(name="reservationService")
	private IReservationService reservationService;
	
	@ModelAttribute
	public ReservationForm getDefault() {
		return new ReservationForm();
	}
	
	@RequestMapping(method = RequestMethod.GET, path="/complete")
	public ModelAndView complete(@ModelAttribute ReservationForm reservationForm,
								 BindingResult bindingResult, 
								 HttpSession session,
								 SessionStatus status) {
		status.setComplete();
		reservationService.complete(reservationForm.getReservationId());
		ModelAndView result = new ModelAndView("reservation-complete");
		result.addObject("reservation", reservationForm);
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, path="/summary")
	public ModelAndView summarize (@ModelAttribute ReservationForm reservationForm) {
		int[] totals = Helper.totals(reservationForm.getSelectedTools(), 
									 Helper.daysBetween(reservationForm.getStartDt(), reservationForm.getEndDt()));
		reservationForm.setTotalRentalPrice(totals[0]);
		reservationForm.setTotalDepositPrice(totals[1]);
		return new ModelAndView("reservation-summary");
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView get(
			@ModelAttribute ReservationForm reservationForm, 
			BindingResult bindingResult, 
			HttpSession session,
			SessionStatus status) {
		reservationForm.setToolTypes(toolService.getToolTypes());
		ModelAndView modelAndView = new ModelAndView("reservation-form");
		return modelAndView;
	}
	
	@RequestMapping(method=RequestMethod.POST)
	@Transactional
	public Object post(
			@Valid @ModelAttribute ReservationForm reservationForm, 
			BindingResult bindingResult, 
			HttpSession session,
			SessionStatus status) {
		reservationForm.setBadDates(badDates(reservationForm));
		if((reservationForm.isBadDates() || bindingResult.hasErrors()) && !"cancel".equalsIgnoreCase(reservationForm.getState())) {
			reservationForm.setSelectedTool(0);
			reservationForm.setToolType(0);
			return get(reservationForm, bindingResult, session, status);
		}
		
		String redirect = 
				reactToForm(reservationForm, 
					((Customer) session.getAttribute("user")).getUsername(),
					status);
		
		// Make sure that the tool list is populated.
		bindToolList(reservationForm);
		
		// Redirect back to the form.
		RedirectView redirectView = new RedirectView(redirect);
		
		return redirectView;
	}
	
	private boolean badDates(ReservationForm reservationForm) {
		return reservationForm.getStartDt() != null && reservationForm.getEndDt() != null && reservationForm.getEndDt().before(reservationForm.startDt) ;
	}

	private void resetToolSelector(ReservationForm r) {
		r.setSelectedTool(0);
		r.setToolType(0);
	}
	
	private String reactToForm(ReservationForm reservationForm, String username, SessionStatus status) {
		String state = reservationForm.getState();
		String nextPage = "/reservations";
		if(state == null) return nextPage;
		switch(state.toLowerCase()) {
		case "add a tool":
			boolean add = addTool(reservationForm, username);
			reservationForm.setLastAddFailed(!add);
			resetToolSelector(reservationForm);
			break;
		case "remove last tool":
			removeTool(reservationForm);
			resetToolSelector(reservationForm);
			break;
		case "cancel":
			if(reservationForm.getReservationId() != null) {
				reservationService.delete(reservationForm.getReservationId());
			}
			status.setComplete();
			nextPage = "/customers";
			break;
		case "calculate total":
			nextPage = "/reservations/summary";
		}
		reservationForm.setState(null);
		
		return nextPage;
	}

	private void removeTool(ReservationForm reservationForm) {
		List<Tool> selectedTools = reservationForm.getSelectedTools();
		if(!selectedTools.isEmpty()) {
			int index = selectedTools.size() - 1;
			Tool tool = selectedTools.get(index);
			reservationService.removeFromReservation(reservationForm.reservationId, tool);
			selectedTools.remove(index);
		}
	}

	private boolean addTool(ReservationForm reservationForm, String username) {
		Tool stillAvailable = toolService.stillAvailable(reservationForm.getStartDt(), 
														 reservationForm.getEndDt(), 
														 reservationForm.getSelectedTool());
		if(stillAvailable == null) {
			return false;
		}
		Integer reservationNumber = reservationService.addToReservation(reservationForm.getReservationId(),
																		 username, 
																		 reservationForm.getStartDt(),
																		 reservationForm.getEndDt(),
																		 stillAvailable);
		reservationForm.getSelectedTools().add(stillAvailable);
		reservationForm.setReservationId(reservationNumber);
		return true;
	}

	private void bindToolList(ReservationForm reservationForm) {
		int toolType = reservationForm.getToolType();
		Date startDt = reservationForm.getStartDt();
		Date endDt = reservationForm.getEndDt();
		List<Tool> availableOn = this.toolService.availableOn(startDt, endDt, toolType);
		reservationForm.setTools(availableOn);
	}

	public static class ReservationForm {
		private boolean badDates;
		private boolean lastAddFailed;
		public boolean isLastAddFailed() {
			return lastAddFailed;
		}

		public void setLastAddFailed(boolean lastAddFailed) {
			this.lastAddFailed = lastAddFailed;
		}

		private int toolType;
		
		@NotNull
		@DateTimeFormat(pattern = "MM/dd/yyyy")
		private Date startDt;
		
		@NotNull
		@DateTimeFormat(pattern = "MM/dd/yyyy")
		private Date endDt;
		private Integer reservationId;
		private Integer selectedTool;
		private String state;
		private List<ToolType> toolTypes;
		private List<Tool> tools;
		private List<Tool> selectedTools;
		
		private Integer totalRentalPrice;
		private Integer totalDepositPrice;
		
		public ReservationForm() {
			this.tools = new LinkedList<>();
			this.selectedTools = new LinkedList<>();
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
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}

		public List<Tool> getTools() {
			return tools;
		}

		public void setTools(List<Tool> tools) {
			this.tools = tools;
		}

		public void setToolTypes(List<ToolType> toolTypes) {
			this.toolTypes = toolTypes;
		}
		public List<ToolType> getToolTypes() {
			return toolTypes;
		}
		public int getToolType() {
			return toolType;
		}
		public void setToolType(int toolType) {
			this.toolType = toolType;
		}
		public Integer getReservationId() {
			return reservationId;
		}
		public void setReservationId(Integer reservationId) {
			this.reservationId = reservationId;
		}
		public Integer getSelectedTool() {
			return selectedTool;
		}
		public void setSelectedTool(Integer selectedTool) {
			this.selectedTool = selectedTool;
		}

		public List<Tool> getSelectedTools() {
			return selectedTools;
		}

		public void setSelectedTools(List<Tool> selectedTools) {
			this.selectedTools = selectedTools;
		}

		public Integer getTotalRentalPrice() {
			return totalRentalPrice;
		}

		public void setTotalRentalPrice(Integer totalRentalPrice) {
			this.totalRentalPrice = totalRentalPrice;
		}

		public Integer getTotalDepositPrice() {
			return totalDepositPrice;
		}

		public void setTotalDepositPrice(Integer totalDepositPrice) {
			this.totalDepositPrice = totalDepositPrice;
		}
		
		public boolean isAllowedToAddMore() {
			return this.getSelectedTools().size() < 50;
		}

		public boolean isBadDates() {
			return badDates;
		}

		public void setBadDates(boolean badDates) {
			this.badDates = badDates;
		}
	}
}
