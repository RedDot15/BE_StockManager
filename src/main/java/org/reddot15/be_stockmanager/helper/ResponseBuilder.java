package org.reddot15.be_stockmanager.helper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {
	public static ResponseEntity<ResponseObject> buildResponse(HttpStatus status, String message, Object data) {
		return ResponseEntity.status(status)
				.body(new ResponseObject(40, message, data));
	}

	public static ResponseEntity<ResponseObject> buildErrorResponse(HttpStatus status, Integer code, String message, Object data) {
		return ResponseEntity.status(status)
				.body(new ResponseObject(code, message, data));
	}
}
