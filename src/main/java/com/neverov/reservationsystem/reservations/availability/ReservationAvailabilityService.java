package com.neverov.reservationsystem.reservations.availability;

import com.neverov.reservationsystem.reservations.ReservationRepository;
import com.neverov.reservationsystem.reservations.ReservationService;
import com.neverov.reservationsystem.reservations.ReservationStatus;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReservationAvailabilityService {
	private final ReservationRepository repository;
	private final Logger log = LoggerFactory.getLogger(ReservationService.class);


	public ReservationAvailabilityService(ReservationRepository repository) {
		this.repository = repository;
	}

	public boolean isReservationAvailable(
			Long roomId,
			LocalDate startDate,
			LocalDate endDate
	) {
		if (!endDate.isAfter(startDate)) {
			throw new IllegalArgumentException("Start date must be 1 day earlier then end date");
		}
		List<Long> conflictingIds = repository.findConflictReservationIds(
				roomId,
				startDate,
				endDate,
				ReservationStatus.APPROVED
		);

		if (conflictingIds.isEmpty()) {
			return true;
		}
		log.info("Conflict with: ids =  {}", conflictingIds);
		return false;

	}
}
