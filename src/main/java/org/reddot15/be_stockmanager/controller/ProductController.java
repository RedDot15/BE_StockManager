package org.reddot15.be_stockmanager.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.request.ProductCreateRequest;
import org.reddot15.be_stockmanager.dto.request.ProductUpdateRequest;
import org.reddot15.be_stockmanager.helper.ResponseObject;
import org.reddot15.be_stockmanager.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.reddot15.be_stockmanager.helper.ResponseBuilder.buildResponse;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/products")
public class ProductController {
	ProductService productService;

	@PostMapping(value = "")
	public ResponseEntity<ResponseObject> create(@Valid @RequestBody ProductCreateRequest request) {
		return buildResponse(HttpStatus.OK, "Create new product successfully.", productService.create(request));
	}

	@GetMapping(value = "")
	public ResponseEntity<ResponseObject> getAll(
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "nextPageToken", required = false) String nextPageToken) {
		return buildResponse(HttpStatus.OK, "Get products successfully.", productService.getAll(limit, nextPageToken));
	}

	@PutMapping(value = "/{productId}")
	public ResponseEntity<ResponseObject> update(@PathVariable String productId, @Valid @RequestBody ProductUpdateRequest request) {
		return buildResponse(HttpStatus.OK, "Update product successfully.", productService.update(productId, request));
	}

	@DeleteMapping(value = "/{productId}")
	public ResponseEntity<ResponseObject> delete(@PathVariable String productId) {
		return buildResponse(HttpStatus.OK, "Delete product successfully.", productService.delete(productId));
	}
}
