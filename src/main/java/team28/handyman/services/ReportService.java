package team28.handyman.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import team28.handyman.domain.Report;

@Repository("reportService")
public class ReportService implements IReportService {
	@Autowired
	private NamedParameterJdbcTemplate template;
	
    private static final String GET_TOOL_REPORT = 
"SELECT t.tool_number, abbreviated_description " +
"    , COALESCE(SUM(rt.at_rental_price * (r.end_dt - r.start_dt + 1)), 0) AS rental_profit " +
"    , t.purchase_price + COALESCE(SUM(so.est_repair_price),0) as cost " +
"    , COALESCE(SUM(rt.at_rental_price * (r.end_dt - r.start_dt + 1)), 0) - (t.purchase_price + COALESCE(SUM(so.est_repair_price),0)) AS total_profit " +
"FROM handyweb.tools AS t " +
"LEFT JOIN handyweb.tags AS tg ON tg.tool_number = t.tool_number " +
"LEFT JOIN handyweb.reservation_tools AS rt ON rt.tool_number = t.tool_number " +
"LEFT JOIN handyweb.reservations AS r ON r.reservation_number = rt.reservation_number " +
" AND r.end_dt < :reportEndDt " +
"LEFT JOIN handyweb.service_order as so on so.tool_number = t.tool_number "
+ " AND so.end_dt < :reportEndDt " +
"WHERE tg.sold_dt IS NULL " +
"GROUP BY t.tool_number " +
"ORDER BY total_profit DESC;"; 
;

	
    private static final String GET_CUSTOMER_REPORT = 
"SELECT first_name, last_name, c.username, COUNT(*) tool_count " + 
"FROM handyweb.Customers AS c " + 
"INNER JOIN handyweb.reservations AS r ON r.username = c.username " + 
"INNER JOIN handyweb.reservation_tools AS rt ON rt.reservation_number = r.Reservation_number " + 
"WHERE start_dt BETWEEN :reportStartDt AND :reportEndDt  AND end_dt BETWEEN :reportStartDt AND :reportEndDt " +  
"GROUP BY c.username " + 
"ORDER BY Tool_count DESC, last_name;";
	

    private static final String GET_CLERK_REPORT =
"SELECT name, COALESCE(pu_r.pu_count,0) pu_reservations, COALESCE(do_r.do_Count,0) do_reservations, COALESCE(pu_r.pu_count,0) + COALESCE(do_r.do_Count,0) total " + 
"FROM handyweb.Clerks AS c  " +
"LEFT JOIN ( " +
"	SELECT pu_r.dropoff_clerk, COUNT(*) do_count " +
"	FROM handyweb.Reservations AS pu_r  " +
"	WHERE pu_r.start_dt <= :reportEndDt AND pu_r.end_dt >= :reportStartDt " +
"	GROUP BY pu_r.dropoff_clerk " +
") do_r ON do_r.dropoff_clerk = c.username " +
"LEFT JOIN ( " +
"	SELECT pu_r.pickup_clerk, COUNT(*) pu_count " +
"	FROM handyweb.Reservations AS pu_r  " +
"	WHERE pu_r.start_dt <= :reportEndDt AND pu_r.end_dt >= :reportStartDt " +
"	GROUP BY pu_r.pickup_clerk " +
") pu_r ON pu_r.pickup_clerk = c.username " +
"ORDER BY total DESC; ";

    @Override
    public List<Report> forTools(Date startDate, Date endDate) {
		return template.query(GET_TOOL_REPORT, Helper.params("reportEndDt", endDate), 
				new RowMapper<Report>() {

			    @Override
			    public Report mapRow(ResultSet rs, int rowNum) throws SQLException {
				    return new Report()
				            .setReportToolNumber(rs.getInt("tool_number"))
						    .setReportToolAbbrDescription(rs.getString("abbreviated_description"))
						    .setReportToolRentalProfit(rs.getInt("rental_profit"))
						    .setReportToolCost(rs.getInt("cost"))
						    .setReportToolTotalProfit(rs.getInt("total_profit"));
			}
		});
	}

	
    @Override
	public List<Report> forCustomers(Date startDate, Date endDate) {
		return template.query(GET_CUSTOMER_REPORT, Helper.params("reportStartDt", startDate, "reportEndDt", endDate), 
				new RowMapper<Report>() {

				@Override
				public Report mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new Report()
					        .setReportCustomerFirstName(rs.getString("first_name"))
							.setReportCustomerLastName(rs.getString("last_name"))
							.setReportCustomerEmail(rs.getString("username"))
							.setReportCustomerRentalNum(rs.getInt("tool_count"));
			}
		});
	}
    
    
    @Override
    public List<Report> forClerks(Date startDate, Date endDate) {
		return template.query(GET_CLERK_REPORT, Helper.params("reportStartDt", startDate, "reportEndDt", endDate), 
				new RowMapper<Report>() {

				@Override
				public Report mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new Report()
					        .setReportClerkName(rs.getString("name"))
							.setReportClerkDropOffNum(rs.getInt("do_reservations"))
							.setReportClerkPickUpNum(rs.getInt("pu_reservations"))
							.setReportClerkSumOfBoth(rs.getInt("total"));
			}
		});
	}
}
