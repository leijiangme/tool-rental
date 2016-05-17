package team28.handyman.domain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Customer extends User {
	private static final long serialVersionUID = 1L;
	@Size(min=1, max=35)
	@NotNull
	private String firstName;
	@Size(min=1, max=35)
	@NotNull
	private String lastName;
	
	@Size(min=14, max=14)
	@NotNull
	private String homePhone;
	@Size(min=14, max=14)
	@NotNull
	private String workPhone;
	@Size(min=1, max=512)
	@NotNull
	private String address;
	
	public String getFirstName() {
		return firstName;
	}
	public Customer setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}
	public String getLastName() {
		return lastName;
	}
	public Customer setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}
	public String getHomePhone() {
		return homePhone;
	}
	public Customer setHomePhone(String homePhone) {
		this.homePhone = homePhone;
		return this;
	}
	public String getWorkPhone() {
		return workPhone;
	}
	public Customer setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
		return this;
	}
	public String getAddress() {
		return address;
	}
	public Customer setAddress(String address) {
		this.address = address;
		return this;
	}
}
