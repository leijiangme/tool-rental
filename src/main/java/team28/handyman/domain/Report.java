package team28.handyman.domain;

public class Report{
	private Integer reportToolNumber;
	private String  reportToolAbbrDescription;
	private Integer reportToolRentalProfit;
	private Integer reportToolCost;
	private Integer reportToolTotalProfit;
	
	private String  reportCustomerFirstName;
	private String  reportCustomerLastName;
	private String  reportCustomerEmail;
	private Integer reportCustomerRentalNum;
	
	private String  reportClerkName;
	private Integer reportClerkDropOffNum;
	private Integer reportClerkPickUpNum;
	private Integer reportClerkSumOfBoth;

	public Integer getReportToolNumber() {
		return reportToolNumber;
	}
	public Report setReportToolNumber(Integer reportToolNumber) {
		this.reportToolNumber = reportToolNumber;
		return this;
	}
	public String getReportToolAbbrDescription() {
		return reportToolAbbrDescription;
	}
	public Report setReportToolAbbrDescription(String reportToolAbbrDescription) {
		this.reportToolAbbrDescription = reportToolAbbrDescription;
		return this;
	}
	public Integer getReportToolRentalProfit() {
		return reportToolRentalProfit;
	}
	public Report setReportToolRentalProfit(Integer reportToolRentalProfit) {
		this.reportToolRentalProfit = reportToolRentalProfit;
		return this;
	}
	public Integer getReportToolCost() {
		return reportToolCost;
	}
	public Report setReportToolCost(Integer reportToolCost) {
		this.reportToolCost = reportToolCost;
		return this;
	}
	public Integer getReportToolTotalProfit() {
		return reportToolTotalProfit;
	}
	public Report setReportToolTotalProfit(Integer reportToolTotalProfit) {
		this.reportToolTotalProfit = reportToolTotalProfit;
		return this;
	}
	
	public String getReportCustomerFirstName() {
		return reportCustomerFirstName;
	}
	public Report setReportCustomerFirstName(String reportCustomerFirstName) {
		this.reportCustomerFirstName = reportCustomerFirstName;
		return this;
	}
	public String getReportCustomerLastName() {
		return reportCustomerLastName;
	}
	public Report setReportCustomerLastName(String reportCustomerLastName) {
		this.reportCustomerLastName = reportCustomerLastName;
		return this;
	}
	public String getReportCustomerEmail() {
		return reportCustomerEmail;
	}
	public Report setReportCustomerEmail(String reportCustomerEmail) {
		this.reportCustomerEmail = reportCustomerEmail;
		return this;
	}
	public Integer getReportCustomerRentalNum() {
		return reportCustomerRentalNum;
	}
	public Report setReportCustomerRentalNum(Integer reportCustomerRentalNum) {
		this.reportCustomerRentalNum = reportCustomerRentalNum;
		return this;
	}
		
	public String getReportClerkName() {
		return reportClerkName;
	}
	public Report setReportClerkName(String reportClerkName) {
		this.reportClerkName = reportClerkName;
		return this;
	}
	public Integer getReportClerkDropOffNum() {
		return reportClerkDropOffNum;
	}
	public Report setReportClerkDropOffNum(Integer reportClerkDropOffNum) {
		this.reportClerkDropOffNum = reportClerkDropOffNum;
		return this;
	}
	public Integer getReportClerkPickUpNum() {
		return reportClerkPickUpNum;
	}
	public Report setReportClerkPickUpNum(Integer reportClerkPickUpNum) {
		this.reportClerkPickUpNum = reportClerkPickUpNum;
		return this;
	}
	public Integer getReportClerkSumOfBoth() {
		return reportClerkSumOfBoth;
	}
	public Report setReportClerkSumOfBoth(Integer reportClerkSumOfBoth) {
		this.reportClerkSumOfBoth = reportClerkSumOfBoth;
		return this;
	}
}

