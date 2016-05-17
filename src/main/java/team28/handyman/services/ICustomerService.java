package team28.handyman.services;

import team28.handyman.domain.Customer;

public interface ICustomerService {
	/**
	 * Checks the credentials for a user.
	 * @param id
	 * @param password
	 * @return the customer associated with the credentials.
	 * 
	 * @throws NoSuchCustomerException 
	 * @throws InvalidCredentialsException
	 */
	Customer byId(final String id, String password);

	void save(Customer form);

	Customer byId2(String userName);
}
