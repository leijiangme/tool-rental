package team28.handyman.services;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import team28.handyman.domain.User;

@Repository("userService")
public class UserService {
	@Resource(name = "customerService")
	private ICustomerService customerService;
	@Resource(name = "clerkService")
	private IClerkService clerkService;
	public UserService() {}
	
	public UserService(ICustomerService customerService) {
		this.customerService = customerService;
	}
	//more than one constructor but i don't know how the creator can know which one.  the login dialog
	//has a Customer/Clerk option.  making the user specify that would eliminate this but perhaps there
	//is a better way.  i don't know the spring framework to know really what i am doing other than 
	//copying existing code.
	public UserService(IClerkService clerkService) {
		this.clerkService = clerkService;
	}
	
	/**
	 * Finds a user instance by its credentials.
	 * @param username
	 * @param password
	 * @param type 
	 * @return an instance of the user, either Customer or Clerk if the credentials worked. Otherwise null.
	 */
	public User fromCredentials(final String username, final String password, String type) {
		User u = null;
		u = loadUser(username, password, type);
		return u;
	}

	private User loadUser(final String username, final String password, String type) {
		User u;
		if(isCustomer(type)) {
			u = customerService.byId(username, password);
		} else {
			u = clerkService.byId(username, password);
		}
		return u;
	}
	
	private boolean isCustomer(String type) {
		return "customer".equals(type);
	}
}
