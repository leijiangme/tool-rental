package team28.handyman.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import team28.handyman.domain.Reservation;
import team28.handyman.domain.Tool;

@Repository("reservationService")
public class ReservationService implements IReservationService{
	@Autowired
	private NamedParameterJdbcTemplate template;
	
	private static final String INSERT_RESERVATION = 
			"INSERT INTO handyweb.Reservations (username, start_dt,end_dt, in_process_timer) " +
			"VALUES (:username, :startDt, :endDt,:inProcessTimer)";
	
	private static final String ADD_TOOL = 
			"INSERT INTO handyweb.Reservation_Tools (reservation_number, tool_number, at_rental_price, at_deposit_price) " +
			"VALUES (:reservationNumber, :toolNumber, :rentalPrice, :depositPrice)";
	
	private static final String UPDATE_TIMEOUT = 
			"UPDATE handyweb.Reservations " +
			"SET in_process_timer = :inProcessTimer "+
			"WHERE reservation_number = :reservationNumber";
	
	private static final String REMOVE_TOOL = 
			"DELETE FROM handyweb.Reservation_Tools " + 
			"WHERE tool_number = :toolNumber AND   " +
			"reservation_number = :reservationNumber";
	
	private static final String REMOVE_MIGRATION = 
			"DELETE FROM handyweb.Reservations WHERE reservation_number = :reservationNumber";
	private static final String COMPLETE_TOOL_PICKUP = 
			"UPDATE handyweb.reservations SET pickup_clerk = :pickup_clerk, credit_card = :credit_card, exp_dt = :exp_dt WHERE reservation_number = :reservation_number";
	private static final String COMPLETE_TOOL_RETURN = 
			"UPDATE handyweb.reservations SET dropoff_clerk = :dropoff_clerk WHERE reservation_number = :reservation_number";
	private static final String GET_RESERVATION_TOOL_IDs = 
			"SELECT tool_number FROM handyweb.Reservation_tools WHERE reservation_number = :reservation_number";
	
	private static final String GET_FOR_USER = 
			"SELECT r.Reservation_Number, start_dt, end_dt                                      			" +
			"    , SUM(rt.at_rental_price * (1 + (end_dt - start_dt))) rental_price                   		" +
			"    , SUM(rt.at_deposit_price * (1 + (end_dt - start_dt))) deposit_price                 		" +
			"    , (SELECT name FROM handyweb.clerks WHERE username = r.pickup_clerk)  AS pickup_clerk		" +
			"    , (SELECT name FROM handyweb.clerks WHERE username = r.dropoff_clerk) AS dropoff_clerk     " +
			"FROM handyweb.Reservations AS r                                                    			" +
			"INNER JOIN handyweb.reservation_tools AS rt ON rt.reservation_number = r.reservation_number 	" +
			"WHERE r.username = :username AND r.in_process_timer IS NULL                                  	" +
			"GROUP BY r.reservation_number                                                     				" +
	        "ORDER BY reservation_number DESC  																";
	
	private static final String GET_FOR_RESERVATION = 
			"SELECT r.Reservation_Number, start_dt, end_dt, credit_card, exp_dt, cust.first_name, cust.last_name"+
			"	    , SUM(rt.at_rental_price * (1 + (end_dt - start_dt))) rental_price                          "+
			"	    , SUM(rt.at_deposit_price * (1 + (end_dt - start_dt))) deposit_price                        "+
			"	    , (SELECT name FROM handyweb.clerks WHERE username = r.pickup_clerk)  AS pickup_clerk       "+
			"	    , (SELECT name FROM handyweb.clerks WHERE username = r.dropoff_clerk) AS dropoff_clerk      "+
			"	FROM handyweb.Reservations AS r                                                                 "+
			"	INNER JOIN handyweb.reservation_tools AS rt ON rt.reservation_number = r.reservation_number     "+
			"	INNER JOIN handyweb.customers AS cust ON cust.username = r.username                             "+
			"	WHERE r.reservation_number = :reservationNumber                                                 "+
			"GROUP BY r.reservation_number, cust.username                                                       ";
	
	private static final String PURGE_TEMPS = 
			"DELETE FROM handyweb.reservations as r WHERE r.in_process_timer < current_timestamp";
	
	@Override
	public void delete(int id) {
		template.update(REMOVE_MIGRATION, Helper.params("reservationNumber", id));
	}

	@Override
	public Integer addToReservation (
			Integer reservationNumber, 
			String username, 
			Date startDt, 
			Date endDt, 
			Tool tool) {
		reservationNumber = create(reservationNumber, username, startDt, endDt);
		addTool(reservationNumber, tool);
		return reservationNumber;
	}

	private void addTool(Integer reservationNumber, Tool tool) {
		template.update(ADD_TOOL, 
						Helper.params("reservationNumber", reservationNumber, 
									  "toolNumber", tool.getToolNumber(),
									  "rentalPrice", tool.getRentalPrice(),
									  "depositPrice", tool.getDepositPrice()));
	}

	private Integer create(Integer reservationNumber, String username, Date startDt, Date endDt) {
		if(reservationNumber == null) {
			GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
			reservationNumber = template.update(
					INSERT_RESERVATION, 
					Helper.params("username", username, 
									"startDt", startDt, 
									"endDt", endDt, 
									"inProcessTimer", nextTimeout()),
					keyHolder);
			Map<String, Object> keys = keyHolder.getKeys();
			reservationNumber = (Integer) keys.get("reservation_number");
		} else {
			template.update(UPDATE_TIMEOUT, 
							Helper.params("reservationNumber", reservationNumber, 
									"inProcessTimer", nextTimeout()));
		}
		return reservationNumber;
	}
	
	private Date nextTimeout() {
		Calendar timeout = Calendar.getInstance();
		timeout.add(Calendar.MINUTE, 10);
		return timeout.getTime();
	}

	@Override
	public void removeFromReservation(Integer reservationNumber, Tool tool) {
		template.update(REMOVE_TOOL,
						Helper.params("toolNumber", tool.getToolNumber(),
									  "reservationNumber", reservationNumber));
	}

	@Override
	public void complete(Integer reservationNumber) {
		template.update(UPDATE_TIMEOUT, Helper.params("inProcessTimer",null, 
													  "reservationNumber", reservationNumber));
	}

	@Override
	public void reservationPickup(Integer reservationNumber, String clerkId, String creditCard, String expirationDate) {
		template.update(COMPLETE_TOOL_PICKUP
				, Helper.params(
						"pickup_clerk", clerkId,
						"credit_card", creditCard,
						"exp_dt", expirationDate,
						"reservation_number", reservationNumber));
	}
	@Override
	public void reservationDropoff(Integer reservationNumber, String clerkId) {
		template.update(COMPLETE_TOOL_RETURN
				, Helper.params(
						"dropoff_clerk", clerkId,
						"reservation_number", reservationNumber));
	}

	@Override
	public Reservation getById(Integer reservationNumber) {
		SqlParameterSource params = Helper.params("reservationNumber", reservationNumber);
		List<Reservation> c = template.query(GET_FOR_RESERVATION, params, new RowMapper<Reservation>() {
			@Override
			public Reservation mapRow(ResultSet rs, int idx) throws SQLException {
				return new Reservation()
						.setReservationNumber(reservationNumber)
						.setEndDt(rs.getDate("end_dt"))
						.setStartDt(rs.getDate("start_dt"))
						.setCreditCard(rs.getString("credit_card"))
						.setExpirationDate(rs.getString("exp_dt"))
						.setPickupClerk(rs.getString("pickup_clerk"))
						.setDropoffClerk(rs.getString("dropoff_clerk"))
						.setRentalPrice(rs.getInt("rental_price"))
						.setDepositPrice(rs.getInt("deposit_price"))
						.setCustomerFirstName(rs.getString("first_name"))
						.setCustomerLastName(rs.getString("last_name"))
						;
			}
		});
		Reservation r = c.size() == 0 ? null : c.get(0);
		if (r == null)
			return null;
		IToolService ts = new ToolService(template);
		List<Integer> rows = (List<Integer>) template.queryForList(GET_RESERVATION_TOOL_IDs
				, Helper.params("reservation_number", reservationNumber), Integer.class);
		for (Integer toolid : rows)
		{
			r.getTools().add(ts.byId(toolid));
		}
		return r;
	}

	@Override
	public List<Reservation> forUser(String username) {
		return template.query(GET_FOR_USER, Helper.params("username", username), new RowMapper<Reservation>() {

			@Override
			public Reservation mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new Reservation()
						.setEndDt(rs.getDate("end_dt"))
						.setStartDt(rs.getDate("start_dt"))
						.setReservationNumber(rs.getInt("reservation_number"))
						.setRentalPrice(rs.getInt("rental_price"))
						.setDepositPrice(rs.getInt("deposit_price"))
						.setPickupClerk(rs.getString("pickup_clerk"))
						.setDropoffClerk(rs.getString("dropoff_clerk"));
			}
		});
	}

	@Override
	public void clearTempReservations() {
		System.out.println("Clearing the expired reservations");
		int updated = template.update(PURGE_TEMPS, new HashMap<>());
		System.out.println("Removed " + updated + " expired reservations");
	}
}
