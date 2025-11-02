package com.neverov.reservationsystem.reservations;

import com.neverov.reservationsystem.reservations.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {
	private final ReservationAvailabilityService availabilityService;
	private final Logger log = LoggerFactory.getLogger(ReservationService.class);
	private final ReservationMapper mapper;
	private final ReservationRepository repository;
	public ReservationService(ReservationAvailabilityService availabilityService, ReservationMapper mapper, ReservationRepository repository) {
		this.availabilityService = availabilityService;
		this.mapper = mapper;
		this.repository = repository;
	}

	public Reservation getReservationById(
			Long id
	) {
		ReservationEntity reservationEntity = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(
						"Not found reservation by id = " + id
				));
		return mapper.toDomain(reservationEntity);
	}

	public List<Reservation> searchAllByFilter(
			ReservationSearchFilter filter
	) {
		int pageSize = filter.pageSize() != null
				? filter.pageSize() : 10;
		int pageNumber = filter.pageNumber() != null
				? filter.pageNumber() : 0;

		var pageable = Pageable
				.ofSize(pageSize)
				.withPage(pageNumber);

		List<ReservationEntity> allEntities = repository.searchByFilter(
				filter.roomId(),
				filter.userId(),
				pageable
		);

		return allEntities.stream()
				.map(mapper::toDomain)
				.toList();
	}


	public Reservation createReservation(Reservation reservationToCreate) {
		if (reservationToCreate.status() != null) {
			throw new IllegalArgumentException("Status should by empty");
		}
		if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
			throw new IllegalArgumentException("Start date must be 1 day earlier then end date");
		}

		var entityToSave = mapper.toEntity(reservationToCreate);
		entityToSave.setStatus(ReservationStatus.PENDING);

		var savedEntity = repository.save(entityToSave);
		return mapper.toDomain(savedEntity);
	}

	public Reservation updateReservation(
			Long id,
			Reservation reservationToUpdate
	) {
		var reservationEntity = repository.findById(id).orElseThrow( () -> new EntityNotFoundException(
				"Not found reservation with id = " + id
		));
		if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
			throw new IllegalArgumentException("Cannot modify reservation: status = " + reservationEntity.getStatus());
		}
		if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
			throw new IllegalArgumentException("Start date must be 1 day earlier then end date");
		}
		var reservationToSave = mapper.toEntity(reservationToUpdate);
		reservationToSave.setStatus(ReservationStatus.PENDING);
		reservationToSave.setId(id);

		return mapper.toDomain(repository.save(reservationToSave));
	}

	@Transactional
	public void cancelReservation(Long id) {
		var reservationEntity = repository.findById(id)
				.orElseThrow( () -> new EntityNotFoundException("Not found reservation with id = " + id));

		if (reservationEntity.getStatus().equals(ReservationStatus.APPROVED)) {
			throw new IllegalStateException("Cannot cancel approved reservation. Contact with manager please");
		}
		if (reservationEntity.getStatus().equals(ReservationStatus.CANCELLED)) {
			throw new IllegalStateException("Cannot cancel reservation. Reservation was already cancelled");
		}
		repository.setStatus(id, ReservationStatus.CANCELLED);
		log.info("Successfully cancelled reservation, id={}", id);
	}

	public Reservation approveReservation(Long id) {
		var reservationEntity = repository.findById(id).orElseThrow( () -> new EntityNotFoundException(
				"Not found reservation with id = " + id
		));

		if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
			throw new IllegalArgumentException("Cannot approve reservation: status = " + reservationEntity.getStatus());
		}

		var isAvailableToApprove = availabilityService.isReservationAvailable(
				reservationEntity.getRoomId(),
				reservationEntity.getStartDate(),
				reservationEntity.getEndDate()
		);

		if (!isAvailableToApprove) {
			throw new IllegalArgumentException("Cannot approve reservation because of conflict");
		}

		reservationEntity.setStatus(ReservationStatus.APPROVED);
		repository.save(reservationEntity);

		return mapper.toDomain(reservationEntity);
	}
}
