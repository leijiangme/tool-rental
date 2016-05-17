package team28.handyman.controllers;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import team28.handyman.domain.Report;
import team28.handyman.services.IReportService;
import team28.handyman.services.IToolService;


@Controller
@RequestMapping("/reports")
@SessionAttributes("reportForm")
public class ReportController {
	
	public ReportController() {
	}
	
	public ReportController(IReportService cs) {
		this.reportService = cs;
	}

	@Resource(name="reportService")
	IReportService reportService ;
	@ModelAttribute
	public ReportForm getDefault() {
		return new ReportForm();
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView reportPage (HttpSession session) {
	   ModelAndView result = null;
	   result = new ModelAndView("reports");
	   return result;
    }
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView createReport (@ModelAttribute ReportForm reportForm,
			@RequestParam(required=true) String reportType,
			                          BindingResult bindingResult, 
			                          HttpSession session) {
	   ModelAndView result = null;
	   List<Report> rpt = null;
	   switch(reportType) {
	     case "tool":
	    	rpt = reportService.forTools(reportForm.getStartDt(), reportForm.getEndDt());
			result = new ModelAndView("report-tool");
			break;
	     case "customer":
	    	rpt = reportService.forCustomers(reportForm.getStartDt(), reportForm.getEndDt());
			result = new ModelAndView("report-customer");
			break;
		 case "clerk":
			 rpt = reportService.forClerks(reportForm.getStartDt(), reportForm.getEndDt());
			result = new ModelAndView("report-clerk");
			break;
		}
		result.addObject("report", reportForm);
		result.addObject("reports", rpt);
		return result;
    }
	
	public static class ReportForm {	
		@NotNull
		@DateTimeFormat(pattern = "MM/dd/yyyy")
		private Date startDt;
		
		@NotNull
		@DateTimeFormat(pattern = "MM/dd/yyyy")
		private Date endDt;
		
		private String reportType;
				
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
		public String getReportType() {
			return reportType;
		}
		public void setReportTypes(String reportType) {
			this.reportType = reportType;
		}		
	}
}



