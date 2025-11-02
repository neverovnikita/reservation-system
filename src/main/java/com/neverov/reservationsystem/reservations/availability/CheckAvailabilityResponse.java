package com.neverov.reservationsystem.reservations.availability;

public record CheckAvailabilityResponse(
		String message,
		AvailabilityStatus status
) {
}
