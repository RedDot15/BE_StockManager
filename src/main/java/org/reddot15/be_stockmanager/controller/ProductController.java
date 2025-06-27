package org.reddot15.be_stockmanager.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.request.ProductCreateRequest;
import org.reddot15.be_stockmanager.dto.request.ProductUpdateRequest;
import org.reddot15.be_stockmanager.dto.response.ProductResponse;
import org.reddot15.be_stockmanager.helper.ResponseObject;
import org.reddot15.be_stockmanager.service.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.reddot15.be_stockmanager.helper.ResponseBuilder.buildResponse;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/products")
public class ProductController {
	ProductService productService;

	@PostMapping(value = "/import")
	public ResponseEntity<ResponseObject> importProductFromCSV(@RequestParam("file") MultipartFile file) {
		List<ProductResponse> importedProducts = productService.importProductFromCSV(file);
		return buildResponse(HttpStatus.OK,
				"CSV file uploaded and " + importedProducts.size() + " products imported successfully.",
				importedProducts);
	}

	@PostMapping(value = "")
	public ResponseEntity<ResponseObject> create(@Valid @RequestBody ProductCreateRequest request) {
		return buildResponse(HttpStatus.OK, "Create new product successfully.", productService.create(request));
	}

	@GetMapping(value = "")
	public ResponseEntity<ResponseObject> getAll(
			@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "categoryName", required = false) String categoryName,
			@RequestParam(name = "minPrice", required = false) Double minPrice,
			@RequestParam(name = "maxPrice", required = false) Double maxPrice,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "nextPageToken", required = false) String nextPageToken) {
		return buildResponse(
				HttpStatus.OK,
				"Get products successfully.",
				productService.getAll(
						keyword,
						categoryName,
						minPrice,
						maxPrice,
						limit,
						nextPageToken));
	}

	@GetMapping("/download-excel")
	public ResponseEntity<byte[]> downloadProductsExcel(
			@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "categoryName", required = false) String categoryName,
			@RequestParam(name = "minPrice", required = false) Double minPrice,
			@RequestParam(name = "maxPrice", required = false) Double maxPrice) {

		ByteArrayInputStream bis = productService.exportProductsToExcel(
				keyword,
				categoryName,
				minPrice,
				maxPrice);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=products.xlsx");
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		return new ResponseEntity<>(bis.readAllBytes(), headers, HttpStatus.OK);
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
