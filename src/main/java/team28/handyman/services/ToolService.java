package team28.handyman.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import team28.handyman.domain.Tool;
import team28.handyman.domain.ToolType;

@Service("toolService")
public class ToolService implements IToolService {
	/**
	 * Maps the basic information about a tool from a record.
	 * @author J Patrick Davenport
	 *
	 */
	private final class SimpleToolMapper implements RowMapper<Tool> {
		@Override
		public Tool mapRow(ResultSet rs, int arg1) throws SQLException {
			
			return new Tool()
					.setAbbreviatedDescription(rs.getString("abbreviated_description"))
					.setToolNumber(rs.getInt("tool_number"))
					.setRentalPrice(rs.getInt("rental_price"))
					.setDepositPrice(rs.getInt("deposit_price"));
		}
	}
	private static final List<ToolType> TOOL_TYPES;

	static {
		TOOL_TYPES = new LinkedList<ToolType>() {
			private static final long serialVersionUID = 1L;

			{
				this.add(new ToolType(1, "Hand tools"));
				this.add(new ToolType(2, "Construction equipment"));
				this.add(new ToolType(3, "Power tools"));
			}
		};
	}

	private static final String LOAD = "Select t.*,g.listed_dt, g.sold_dt, t.purchase_price / 2 AS sale_price "
			+ " FROM handyweb.tools AS t "
			+ " LEFT JOIN handyweb.tags AS g ON g.tool_number = t.tool_number "
			+ " WHERE t.tool_number = :tool_number";
	private static final String LOAD_ACCESSORY = "SELECT accessory FROM handyweb.accessories WHERE tool_number = :tool_number;";
	private static final String INSERT_TOOL = " INSERT INTO handyweb.tools (tool_number,abbreviated_description,full_description"
			+ ",rental_price,deposit_price,purchase_price, tool_type)"
			+ "VALUES ( nextval('handyweb.tool_numbers'), :abbreviated_description, :full_description, :rental_price"
			+ ", :deposit_price, :purchase_price, :tool_type);";
	private static final String INSERT_TOOL_FOR_SALE = " INSERT INTO handyweb.tags (tool_number, listed_dt)"
			+ "VALUES ( :tool_number, now());";
	private static final String INSERT_TOOL_SOLD = " UPDATE handyweb.tags SET sold_dt = now() WHERE tool_number = :tool_number;";

	private static final String INSERT_ACCESSORY = "INSERT INTO handyweb.accessories (tool_number, accessory) VALUES (:tool_number, :accessory);";
	
	private static final String FIND_AVAILABLE = 
			"SELECT abbreviated_description, t.tool_number, t.rental_price, t.deposit_price            			"+
			"FROM handyweb.Tools AS t                                                                  			"+
			"LEFT OUTER JOIN handyweb.Tags ON tags.tool_number = t.tool_number          "+
			"WHERE t.tool_type = :toolType  AND tags.sold_dt IS NULL	"+
			"	AND NOT EXISTS (                                                                       			"+
			"		SELECT *                                                                           			"+
			"		FROM handyweb.Service_Order AS so                                                           "+
			"		WHERE so.tool_number = t.tool_number                                               			"+
			"			AND so.start_dt <= :endDt AND so.end_dt >= :startDt                          			"+
			"		)                                                                                  			"+
			"	AND NOT EXISTS (                                                                       			"+
			"		SELECT *                                                                           			"+
			"		FROM handyweb.Reservations AS r                                                             "+
			"		INNER JOIN handyweb.Reservation_Tools AS rt ON rt.reservation_number = r.reservation_number "+
			"		WHERE rt.tool_number = t.tool_number                                               			"+
			"			AND r.start_dt <= :endDt AND r.end_dt >= :startDt                            			"+
			"		) ORDER BY t.tool_number;                                                                                 			";
	
	private static final String STILL_AVAILABLE = 
			"SELECT abbreviated_description, t.tool_number, t.rental_price, t.deposit_price            			 " +
			"FROM handyweb.Tools AS t                                                                  			 " +
			"LEFT OUTER JOIN handyweb.Tags ON tags.tool_number = t.tool_number  " +                             
			"WHERE t.tool_number = :toolNumber  AND tags.sold_dt IS NULL " +
			"	AND NOT EXISTS (                                                                       			 " +
			"		SELECT *                                                                           			 " +
			"		FROM handyweb.Service_Order AS so                                                            " +
			"		WHERE so.tool_number = t.tool_number                                               			 " +
			"			AND so.start_dt <= :endDt AND so.end_dt >= :startDt                         			 " +
			"		)                                                                                  			 " +
			"	AND NOT EXISTS (                                                                       			 " +
			"		SELECT *                                                                           			 " +
			"		FROM handyweb.Reservations AS r                                                              " +
			"		INNER JOIN handyweb.Reservation_Tools AS rt ON rt.reservation_number = r.reservation_number  " +
			"		WHERE rt.tool_number = t.tool_number                                               			 " +
			"			AND r.start_dt <= :endDt AND r.end_dt >= :startDt                           			 " +
			"		)";
	public static final String INSERT_SERVICE_ORDER = "INSERT INTO handyweb.Service_Order (tool_number, start_dt, end_dt, est_repair_price) "
			+ " VALUES (:tool_number, :start_dt, :end_dt, :est_repair_price)";
	@Autowired
	private NamedParameterJdbcTemplate template;
	public ToolService (NamedParameterJdbcTemplate template)
	{
		this.template = template;
	}
	public ToolService()
	{
		
	}
	@Override
	public Tool byId(Integer id) {
		SqlParameterSource params = Helper.params("tool_number", id);
		// The reason for the query vs queryForObject is that if no row is found
		// with
		// queryForObject, an exception is thrown. This is annoying, so query
		// works.
		List<Tool> c = template.query(LOAD, params, new RowMapper<Tool>() {
			@Override
			public Tool mapRow(ResultSet rs, int idx) throws SQLException {
				return new Tool().setToolNumber(rs.getInt("tool_number"))
						.setAbbreviatedDescription(rs.getString("abbreviated_description"))
						.setFullDescription(rs.getString("full_description"))
						.setPurchasePrice(rs.getInt("purchase_price")).setRentalPrice(rs.getInt("rental_price"))
						.setDepositPrice(rs.getInt("deposit_price")).setToolType(rs.getInt("tool_type"))
						.setListDate(rs.getDate("listed_dt"))
						.setSoldDate(rs.getDate("sold_dt"))
						.setSalePrice(rs.getInt("sale_price"))
						;
			}
		});
		Tool t = CollectionUtils.isEmpty(c) ? null : c.get(0);
		if (t == null)
			return null;
		if (t.getToolType() == Tool.TOOL_TYPE_POWERTOOL)
		{
			List<String> rows = (List<String>) template.queryForList(LOAD_ACCESSORY
					, Helper.params("tool_number", t.getToolNumber()), String.class);
			t.getAccessories().addAll(rows);
		}
		return t;
	}

	@Override
	public void save(Tool form) {
		// Helper.insertUser(template, form.getUsername(), form.getPassword());

		KeyHolder keyHolder = new GeneratedKeyHolder();
		template.update(INSERT_TOOL,
				Helper.params("abbreviated_description", form.getAbbreviatedDescription(), "full_description",
						form.getFullDescription(), "purchase_price", form.getPurchasePrice(), "rental_price",
						form.getRentalPrice(), "deposit_price", form.getDepositPrice(), "tool_type",
						form.getToolType()),
				keyHolder);
		int tool_number = (int) keyHolder.getKeys().get("tool_number");
		if (form.getToolType() == Tool.TOOL_TYPE_POWERTOOL) {
			for (String s : form.getAccessories())
				template.update(INSERT_ACCESSORY, Helper.params("tool_number", tool_number, "accessory", s));
		}
	}

	@Override
	public List<ToolType> getToolTypes() {
		return TOOL_TYPES;
	}

	@Override
	public void markForSale(Integer id) {
		template.update(INSERT_TOOL_FOR_SALE, Helper.params("tool_number", id));
		
	}

	@Override
	public void sellTool(Integer id) {
		template.update(INSERT_TOOL_SOLD, Helper.params("tool_number", id));
		
	}

	@Override
	public List<Tool> availableOn(Date start, Date end, int toolType) {
		return template.query(FIND_AVAILABLE, Helper.params("startDt", start, 
															"endDt", end, 
															"toolType", toolType), 
				new SimpleToolMapper());
	}

	@Override
	public Tool stillAvailable(Date start, Date end, int toolNumber) {
		List<Tool> result = template.query(
				STILL_AVAILABLE, 
				Helper.params("startDt", start, 
				"endDt", end, 
				"toolNumber", toolNumber), 
				new SimpleToolMapper());
		return result.isEmpty() ? null : result.get(0);
	}

	@Override
	public boolean createServiceOrder(Date start, Date end, int toolNumber, int estRepairCost) {
		//form may be stale by the time the user hits the button.  check one more time to ensure the 
		//tool is still available.  return false if it isn't
		if (stillAvailable(start, end, toolNumber) == null) return false;
		template.update(INSERT_SERVICE_ORDER, 
				Helper.params("tool_number", toolNumber
						, "start_dt", start
						, "end_dt", end
						, "est_repair_price", estRepairCost)
				);
		return true;
	}
	
}
