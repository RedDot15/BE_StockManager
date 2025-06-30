package org.reddot15.be_stockmanager.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.reddot15.be_stockmanager.dto.request.ProductCreateRequest;
import org.reddot15.be_stockmanager.dto.request.ProductUpdateRequest;
import org.reddot15.be_stockmanager.dto.response.ProductResponse;
import org.reddot15.be_stockmanager.helper.ResponseObject;
import org.reddot15.be_stockmanager.service.ProductService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.reddot15.be_stockmanager.helper.ResponseBuilder.buildResponse;

@Slf4j
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
			@RequestParam(name = "limit", defaultValue = "10") Integer limit,
			@RequestParam(name = "nextPageToken", required = false) String nextPageToken) {
		return buildResponse(
				HttpStatus.OK,
				"Get products successfully.",
				productService.getProducts(
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

		Path tempFile = null;
		try {
			// 1. Call the service to generate the file on disk
			tempFile = productService.exportProductsToExcel(keyword, categoryName, minPrice, maxPrice);

			// 2. Prepare the file for streaming
			InputStream inputStream = Files.newInputStream(tempFile);
			InputStreamResource resource = new InputStreamResource(inputStream);

			// 3. Set HTTP headers for the file download response
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.xlsx");
			headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
			headers.add(HttpHeaders.PRAGMA, "no-cache");
			headers.add(HttpHeaders.EXPIRES, "0");

			// 4. Return the ResponseEntity to start the download
			return ResponseEntity.ok()
					.headers(headers)
					.contentLength(Files.size(tempFile))
					.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(resource.getContentAsByteArray());

		} catch (IOException e) {
			// Handle exceptions, e.g., file not found after creation
			log.error("Error while preparing file for download", e);
			throw new RuntimeException("Error occurred while exporting data.", e);
		} finally {
			// 5. CRUCIAL: Delete the temporary file from the server disk after streaming.
			// The response will be sent before this `finally` block is executed.
			if (tempFile != null) {
				try {
					Files.deleteIfExists(tempFile);
				} catch (IOException e) {
					log.error("Failed to delete temporary export file: " + tempFile, e);
				}
			}
		}
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
