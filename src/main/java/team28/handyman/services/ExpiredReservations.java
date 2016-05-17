package team28.handyman.services;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiredReservations {
	@Resource(name="reservationService")
	private IReservationService rs;
	
	@Scheduled(fixedRate= 5000)
	public void clear() {
		rs.clearTempReservations();
	}
}
