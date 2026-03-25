package com.todo.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// shape of error response sent to client
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

	private String error;
	private String message;
	private LocalDateTime timestamp;

}