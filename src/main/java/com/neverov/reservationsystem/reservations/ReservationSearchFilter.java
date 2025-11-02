package com.neverov.reservationsystem.reservations;

public record ReservationSearchFilter(
		Long roomId,
		Long userId,
		Integer pageSize,
		Integer pageNumber
){
}
