package org.reddot15.be_stockmanager.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.request.AuthenticationRequest;
import org.reddot15.be_stockmanager.dto.request.RefreshRequest;
import org.reddot15.be_stockmanager.helper.ResponseObject;
import org.reddot15.be_stockmanager.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.reddot15.be_stockmanager.helper.ResponseBuilder.buildResponse;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
	AuthenticationService authenticationService;

	@PostMapping(value = "/tokens")
	public ResponseEntity<ResponseObject> authenticate(@Valid @RequestBody AuthenticationRequest request) {
		return buildResponse(HttpStatus.OK, "Authenticate successfully.", authenticationService.authenticate(request));
	}

	@PostMapping("/tokens/refresh")
	public ResponseEntity<ResponseObject> refreshToken(@Valid @RequestBody RefreshRequest request) {
		return buildResponse(HttpStatus.OK, "Refresh token successfully.", authenticationService.refresh(request));
	}

	@GetMapping("/tokens/introspect")
	public ResponseEntity<ResponseObject> introspect() {
		return buildResponse(HttpStatus.OK, "Token valid.", null);
	}

	@DeleteMapping("/tokens")
	public ResponseEntity<ResponseObject> logout() {
		authenticationService.logout();
		return buildResponse(HttpStatus.OK, "Log out successfully.", null);
	}
}
