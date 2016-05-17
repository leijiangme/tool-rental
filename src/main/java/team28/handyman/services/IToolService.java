package team28.handyman.services;

import java.util.Date;
import java.util.List;

import team28.handyman.domain.Tool;
import team28.handyman.domain.ToolType;

public interface IToolService {
	Tool byId(final Integer id);

	void save(Tool form);
	
	List<ToolType> getToolTypes();

	void markForSale(final Integer id);
	void sellTool(final Integer id);
	
	/**
	 * Finds all tools available between the start and end dates.
	 * @param start
	 * @param end
	 * @return a list of tools matching that period.
	 */
	List<Tool> availableOn(Date start, Date end, int toolType);
	
	/**
	 * Finds the tool if still available.
	 * @param start
	 * @param end
	 * @param toolNumber
	 * @return the tool if it's able to be reserved. Null otherwise.
	 */
	Tool stillAvailable(Date start, Date end, int toolNumber);

	boolean createServiceOrder(Date start, Date end, int toolNumber, int estRepairCost);
}
