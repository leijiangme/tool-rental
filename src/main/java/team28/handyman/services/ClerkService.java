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

import team28.handyman.domain.Clerk;

@Service("clerkService")
public class ClerkService implements IClerkService {
	private static final String LOAD = "Select Clerks.*, users.password " +
										"FROM handyweb.Clerks NATURAL JOIN handyweb.users " +
										"WHERE Clerks.username = :username AND " +
										"users.password = :password";
	
	@Autowired
	private NamedParameterJdbcTemplate template;
	
	@Override
	public Clerk byId(String id, String password) {
		SqlParameterSource params = Helper.params("username", id, "password", password);
		// The reason for the query vs queryForObject is that if no row is found with
		// queryForObject, an exception is thrown. This is annoying, so query works.
		List<Clerk> c = template.query(LOAD, params, new RowMapper<Clerk>() {
			@Override
			public Clerk mapRow(ResultSet rs, int idx) throws SQLException {
				return new Clerk()
						.setName(rs.getString("name"))
						.setUsername(rs.getString("username"))
						.setPassword(rs.getString("password"));
			}
		});
		return CollectionUtils.isEmpty(c) ? null : c.get(0);
	}

}
