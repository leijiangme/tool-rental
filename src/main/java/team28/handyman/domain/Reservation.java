package team28.handyman.domain;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Reservation {
	
	private Integer reservationNumber;
	private Date startDt;
	private Date endDt;
	private List<Tool> tools = new LinkedList<Tool>();
	private String userName;
	private Integer rentalPrice;
	private int depositPrice;
	private String pickupClerk;
	private String dropoffClerk;
	private String creditCard;
	private String expirationDate;
	private String customerFirstName;
	private String customerLastName;
	
	public Integer getReservationNumber() {
		return reservationNumber;
	}
	public Reservation setReservationNumber(Integer reservationNumber) {
		this.reservationNumber = reservationNumber;
		return this;
	}
	public Date getStartDt() {
		return startDt;
	}
	public Reservation setStartDt(Date startDt) {
		this.startDt = startDt;
		return this;
	}
	public Date getEndDt() {
		return endDt;
	}
	public Reservation setEndDt(Date endDt) {
		this.endDt = endDt;
		return this;
	}
	public List<Tool> getTools() {
		return tools;
	}
	public void setTools(List<Tool> tools) {
		this.tools = tools;
	}
	
	public int getRentalPrice() {
		return rentalPrice == null && tools != null ? tools.stream().mapToInt(Tool::getRentalPrice).sum() : rentalPrice;
	}
	
	public int getDepositPrice() {
		return rentalPrice == null && tools != null ? tools.stream().mapToInt(Tool::getDepositPrice).sum() :depositPrice;
	}
	public String getUserName() {
		return userName;
	}
	public Reservation setUserName(String userName) {
		this.userName = userName;
		return this;
	}
	public Reservation setRentalPrice(Integer rentalPrice) {
		this.rentalPrice = rentalPrice;
		return this;
	}
	public Reservation setDepositPrice(int int1) {
		this.depositPrice = int1;
		return this;
	}
	
	public Reservation setPickupClerk(String pc) {
		this.pickupClerk = pc;
		return this;
	}
	
	public String getPickupClerk() {
		return pickupClerk;
	}
	public String getDropoffClerk() {
		return dropoffClerk;
	}
	public Reservation setDropoffClerk(String dropoffClerk) {
		this.dropoffClerk = dropoffClerk;
		return this;
	}
	public String getExpirationDate() {
		return expirationDate;
	}
	public Reservation setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
		return this;
	}
	public String getCreditCard() {
		return creditCard;
	}
	public Reservation setCreditCard(String creditCard) {
		this.creditCard = creditCard;
		return this;
	}
	public String getCustomerFirstName() {
		return customerFirstName;
	}
	public Reservation setCustomerFirstName(String customerFirstName) {
		this.customerFirstName = customerFirstName;
		return this;
	}
	public String getCustomerLastName() {
		return customerLastName;
	}
	public Reservation setCustomerLastName(String customerLastName) {
		this.customerLastName = customerLastName;
		return this;
	}
}
