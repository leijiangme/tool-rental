package team28.handyman.services;

import java.util.Date;
import java.util.List;

import team28.handyman.domain.Reservation;
import team28.handyman.domain.Tool;

public interface IReservationService {
	/**
	 * Deletes a reservation by id.
	 * @param id
	 */
	void delete(int id);
	
	/**
	 * Adds a tool to a registration.
	 * @param registrationNumber
	 * @param tool
	 * @return
	 */
	Integer addToReservation(Integer registrationNumber, String username, Date startDt, Date endDt, Tool tool);
	
	void removeFromReservation(Integer reservationNumber, Tool tool);
	
	/**
	 * Should mark the reservation complete.
	 * @param reservationNumber
	 */
	void complete(Integer reservationNumber);

	void reservationPickup(Integer reservationNumber, String string, String creditCard, String expirationDate);

	void reservationDropoff(Integer reservationNumber, String clerkId);
	
	Reservation getById(Integer reservationNumber);
	
	List<Reservation> forUser(String username);

	void clearTempReservations();
}
