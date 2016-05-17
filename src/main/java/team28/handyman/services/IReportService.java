package team28.handyman.services;

import java.util.Date;
import java.util.List;

import team28.handyman.domain.Report;

public interface IReportService {
	
	List<Report> forTools(Date startDate, Date endDate);
	
	List<Report> forCustomers(Date startDate, Date endDate);
	
	List<Report> forClerks(Date startDate, Date endDate);
}
