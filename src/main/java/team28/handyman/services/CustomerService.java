package team28.handyman.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import team28.handyman.domain.Customer;

@Service("customerService")
public class CustomerService implements ICustomerService {
	private static final String LOAD = "Select customers.*, users.password " +
										"FROM handyweb.customers NATURAL JOIN handyweb.users " +
										"WHERE customers.username = :username";
	private static final String INSERT = "INSERT INTO handyweb.customers (username,first_name,last_name,home_phone,work_phone,address)"
									   + "VALUES (:username, :firstName, :lastName,:homePhone,:workPhone,:address)";
	@Autowired
	private NamedParameterJdbcTemplate template;
	
	@Override
	public Customer byId(String userName, String password) {
		Customer c = byId2(userName);
		if (c == null) {throw new NoSuchCustomerException();}
		//trim() because there are trailing spaces on the pwd.  could fix that in the sql too.  easier here.
		if (c.getPassword().trim().equals(password))
			return c;
		else
			throw new InvalidCredentialsException();
	}

	@Override
	public void save(Customer form) {
		Helper.insertUser(template, form.getUsername(), form.getPassword());
		template.update(INSERT, Helper.params("username",form.getUsername(),
												"firstName", form.getFirstName(),
												"lastName", form.getLastName(),
												"homePhone", form.getHomePhone(),
												"workPhone", form.getWorkPhone(),
												"address", form.getAddress()));
	}

	@Override
	public Customer byId2(String userName) {
		SqlParameterSource params = Helper.params("username", userName);
		// The reason for the query vs queryForObject is that if no row is found with
		// queryForObject, an exception is thrown. This is annoying, so query works.
		List<Customer> c = template.query(LOAD, params, new RowMapper<Customer>() {
			@Override
			public Customer mapRow(ResultSet rs, int idx) throws SQLException {
				return new Customer()
						.setAddress(rs.getString("address"))
						.setFirstName(rs.getString("first_name"))
						.setHomePhone(rs.getString("home_phone"))
						.setWorkPhone(rs.getString("work_phone"))
						.setLastName(rs.getString("last_name"))
						.setPassword(rs.getString("password"))
						.setUsername(userName);
			}
		});
		return CollectionUtils.isEmpty(c) ? null : c.get(0);
	}

}
