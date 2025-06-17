package org.reddot15.be_stockmanager.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.request.VendorCreateRequest;
import org.reddot15.be_stockmanager.dto.request.VendorUpdateRequest;
import org.reddot15.be_stockmanager.helper.ResponseObject;
import org.reddot15.be_stockmanager.service.VendorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.reddot15.be_stockmanager.helper.ResponseBuilder.buildResponse;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/vendors")
public class VendorController {
	VendorService vendorService;

	@PostMapping(value = "")
	public ResponseEntity<ResponseObject> create(@Valid @RequestBody VendorCreateRequest request) {
		return buildResponse(HttpStatus.OK, "Create new vendor successfully.", vendorService.create(request));
	}

	@GetMapping(value = "")
	public ResponseEntity<ResponseObject> getAll(
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "nextPageToken", required = false) String nextPageToken) {
		return buildResponse(HttpStatus.OK, "Get vendors successfully.", vendorService.getAll(limit, nextPageToken));
	}

	@PutMapping(value = "/{vendorId}")
	public ResponseEntity<ResponseObject> update(@PathVariable String vendorId, @Valid @RequestBody VendorUpdateRequest request) {
		return buildResponse(HttpStatus.OK, "Update vendor successfully.", vendorService.update(vendorId, request));
	}

	@DeleteMapping(value = "/{vendorId}")
	public ResponseEntity<ResponseObject> delete(@PathVariable String vendorId) {
		return buildResponse(HttpStatus.OK, "Delete vendor successfully.", vendorService.delete(vendorId));
	}
}
