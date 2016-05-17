package team28.handyman.services;

import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import team28.handyman.domain.Tool;

public class Helper {

	private static final long DT_DIVISOR = 24 * 60 * 60 * 1000;
	private static final String INSERT_USER = "INSERT INTO handyweb.users (username, password) VALUES (:username , :password);";
	
	private Helper() {}
	
	public static SqlParameterSource params(Object...pairs) {
		if(notEven(pairs)) {
			throw new IllegalArgumentException("Pairs must be even: key, value");
		}
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		for(int i = 1; i < pairs.length; i += 2) {
			namedParameters.addValue((String) pairs[i-1], pairs[i]);
		}
		
		return namedParameters;
	}

	private static boolean notEven(Object[] pairs) {
		return pairs == null || pairs.length % 2 != 0;
	}
	
	public static final void insertUser(
			final NamedParameterJdbcTemplate template, 
			final String username, 
			final String password) {
		template.update(INSERT_USER, 
				params("username",username, "password", password));
	}
	
	/**
	 * Calculates the totals for a reservation.
	 * @param tools
	 * @return an array of length 2. First is the total for the rental price; second deposit price.
	 */
	public static int[] totals(List<Tool> tools, Integer numberOfDays) {
		
		return new int[]{tools.stream().mapToInt(t -> t.getRentalPrice() * numberOfDays).sum(),
						 tools.stream().mapToInt(t -> t.getDepositPrice() * numberOfDays).sum()};
	}
	
	public static int daysBetween(Date startDt, Date endDt) {
		long diff = endDt.getTime() - startDt.getTime();
		return (int) (diff / DT_DIVISOR) + 1; // because the tool could be rented for only one day.
	}
	
}
