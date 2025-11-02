package com.neverov.reservationsystem.web;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
	private final static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDto> handleGenericException(
		Exception ex
	){
		log.error("Handle exception ",ex);

		var errorDto = new ErrorResponseDto(
				"Iternal Server Error",
				ex.getMessage(),
				LocalDateTime.now()
		);

		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorDto);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleEntityNotFound(
			EntityNotFoundException ex
	){
		log.error("Handle entityNotFoundException ",ex);

		var errorDto = new ErrorResponseDto(
				"Entity Not Found",
				ex.getMessage(),
				LocalDateTime.now()
		);

		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(errorDto);
	}
	@ExceptionHandler( exception = {
			IllegalArgumentException.class,
			IllegalStateException.class,
			MethodArgumentNotValidException.class,
	})
	public ResponseEntity<ErrorResponseDto> handleBadRequest(
			Exception ex
	){
		log.error("Handle handleBadRequest ",ex);

		var errorDto = new ErrorResponseDto(
				"Bad Request",
				ex.getMessage(),
				LocalDateTime.now()
		);

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(errorDto);
	}







}
