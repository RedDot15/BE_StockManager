package org.reddot15.be_stockmanager.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.reddot15.be_stockmanager.dto.request.ProductCreateRequest;
import org.reddot15.be_stockmanager.dto.request.ProductUpdateRequest;
import org.reddot15.be_stockmanager.dto.response.ProductResponse;
import org.reddot15.be_stockmanager.dto.response.pagination.DDBPageResponse;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.mapper.ProductMapper;
import org.reddot15.be_stockmanager.repository.ProductRepository;
import org.reddot15.be_stockmanager.repository.VendorRepository;
import org.reddot15.be_stockmanager.util.TimeValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class ProductService {
	ProductRepository productRepository;
	ProductMapper productMapper;
	VendorRepository vendorRepository;

	@PreAuthorize("hasAuthority('IMPORT_PRODUCT')")
	public List<ProductResponse> importProductFromCSV(MultipartFile file) {
		// File empty exception
		if (file.isEmpty()) {
			throw new AppException(ErrorCode.EMPTY_FILE);
		}

		// Initialize result variable
		List<Product> importedProducts = new ArrayList<>();
		// Parse file
		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
			 CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
			// Get records
			Iterable<CSVRecord> csvRecords = csvParser.getRecords();

			for (CSVRecord csvRecord : csvRecords) {
				try {
					// Get product ID
					String productId = csvRecord.get("entity_id");
					// Get product
					Optional<Product> foundOptionalProduct = productRepository.findProductById(productId);

					// If not exists
					if (foundOptionalProduct.isEmpty()) {
						// Initial new product
						// Map CSV columns to Product fields
						Product newProduct = Product.builder()
								.pk("Products")
								.entityId(csvRecord.get("entity_id"))
								.vendorId(csvRecord.get("vendor_id"))
								.name(csvRecord.get("name"))
								.categoryName(csvRecord.get("category_name"))
								.importPrice(Double.parseDouble(csvRecord.get("import_price")))
								.salePrice(Double.parseDouble(csvRecord.get("sale_price")))
								.amount(Integer.parseInt(csvRecord.get("amount")))
								.earliestExpiry(TimeValidator.validateDate(csvRecord.get("earliest_expiry")))
								.vat(Double.parseDouble(csvRecord.get("vat")))
								.build();

						// Save the product
						importedProducts.add(productRepository.saveProduct(newProduct));
						// Continue
						continue;
					}

					// Get found product
					Product foundProduct = foundOptionalProduct.get();
					// Map imported product from CSV
					Product importedProduct = Product.builder()
							.pk("Products")
							.entityId(csvRecord.get("entity_id"))
							.vendorId(csvRecord.get("vendor_id"))
							.name(csvRecord.get("name"))
							.categoryName(csvRecord.get("category_name"))
							.importPrice(Double.parseDouble(csvRecord.get("import_price")))
							.salePrice(Double.parseDouble(csvRecord.get("sale_price")))
							.amount(Integer.parseInt(csvRecord.get("amount")))
							.earliestExpiry(TimeValidator.validateDate(csvRecord.get("earliest_expiry")))
							.vat(Double.parseDouble(csvRecord.get("vat")))
							.build();

					// Product information mismatch exception
					if (!foundProduct.equals(importedProduct))
						throw new AppException(ErrorCode.PRODUCT_MISMATCH);

					// Calculate up product amount
					Integer newAmount = foundProduct.getAmount() + importedProduct.getAmount();
					foundProduct.setAmount(newAmount);
					// Update earliest expiry time
					if (importedProduct.getEarliestExpiry().compareTo(foundProduct.getEarliestExpiry()) < 0)
						foundProduct.setEarliestExpiry(importedProduct.getEarliestExpiry());

					// Save the product
					productRepository.saveProduct(foundProduct);
					// Add the imported product to response
					importedProducts.add(importedProduct);
				} catch (IllegalArgumentException | DateTimeParseException e) {
					// Invalid record exception
					log.error(e.getMessage(), e);
					log.error("Invalid record: {}", csvRecord.toString());
					throw new AppException(ErrorCode.INVALID_RECORD);
				}
			}
		} catch (IOException e) {
			throw new AppException(ErrorCode.FILE_PARSE_FAILED);
		}

		return importedProducts.stream()
				.map(productMapper::toResponse)
				.toList();
	}

	@PreAuthorize("hasAuthority('CREATE_PRODUCT')")
	public ProductResponse create(ProductCreateRequest request) {
		// Duplicate exception
		Optional<Product> optionalProduct = productRepository.findProductById(request.getEntityId());
		if (optionalProduct.isPresent())
			throw new AppException(ErrorCode.PRODUCT_DUPLICATE);
		// Vendor not found exception
		if (vendorRepository.findVendorById(request.getVendorId()).isEmpty())
			throw new AppException(ErrorCode.VENDOR_NOT_FOUND);
		// Mappping
		Product entity = productMapper.toEntity(request);
		// Save
		return productMapper.toResponse(productRepository.saveProduct(entity));
	}

	@PreAuthorize("hasAuthority('VIEW_PRODUCT')")
	public DDBPageResponse<ProductResponse> getAll(Integer limit, String nextPageToken) {
		// Default limit if not provided - This remains in the service as business logic
		if (limit == null || limit <= 0) {
			limit = 10;
		}

		// Delegate the full pagination logic to the repository
		DDBPageResponse<Product> productPage = productRepository.findAllProducts(limit, nextPageToken);

		// Mapping to response DTOs - This remains in the service as presentation logic
		List<ProductResponse> productResponses = productPage.getItems().stream()
				.map(productMapper::toResponse)
				.toList();

		// Return the paginated response with DTOs
		return DDBPageResponse.<ProductResponse>builder()
				.items(productResponses)
				.nextPageToken(productPage.getNextPageToken())
				.hasMore(productPage.isHasMore())
				.build();
	}

	@PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
	public ProductResponse update(String productId, ProductUpdateRequest request) {
		// Check exists
		Product entity = productRepository.findProductById(productId)
				.orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
		// Vendor not found exception
		if (vendorRepository.findVendorById(request.getVendorId()).isEmpty())
			throw new AppException(ErrorCode.VENDOR_NOT_FOUND);
		// Updating
		productMapper.updateEntity(entity, request);
		// Save
		return productMapper.toResponse(productRepository.saveProduct(entity));
	}

	@PreAuthorize("hasAuthority('DELETE_PRODUCT')")
	public String delete(String productId) {
		// Check exists
		if (productRepository.findProductById(productId).isEmpty())
			throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
		// Delete
		productRepository.deleteProductById(productId);
		// Return ID
		return productId;
	}
}
