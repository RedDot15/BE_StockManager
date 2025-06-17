package org.reddot15.be_stockmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.reddot15.be_stockmanager.dto.request.ProductCreateRequest;
import org.reddot15.be_stockmanager.dto.request.ProductUpdateRequest;
import org.reddot15.be_stockmanager.dto.response.pagination.PageResponse;
import org.reddot15.be_stockmanager.dto.response.ProductResponse;
import org.reddot15.be_stockmanager.dto.response.ProductResponse;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.mapper.ProductMapper;
import org.reddot15.be_stockmanager.repository.ProductRepository;
import org.reddot15.be_stockmanager.repository.ProductRepository;
import org.reddot15.be_stockmanager.repository.VendorRepository;
import org.reddot15.be_stockmanager.util.PaginationTokenUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class ProductService {
	ProductRepository productRepository;
	ProductMapper productMapper;
	ObjectMapper objectMapper;
	VendorRepository vendorRepository;

	@PreAuthorize("hasAuthority('MANAGE_DATA')")
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

	@PreAuthorize("hasAuthority('MANAGE_DATA')")
	public PageResponse<ProductResponse> getAll(Integer limit, String nextPageToken) {
		// Default limit if not provided
		if (limit == null || limit <= 0) {
			limit = 10;
		}

		// Initialize variables for aggregation
		List<Product> aggregatedProducts = new ArrayList<>();
		Map<String, AttributeValue> currentExclusiveStartKey = null;
		boolean hasMore = true;

		// Decode & Assign ExclusiveStartKey if exists using the utility
		currentExclusiveStartKey = PaginationTokenUtil.decodeNextPageToken(nextPageToken, objectMapper);

		// Loop to aggregate items until limit is met or no more data
		while (aggregatedProducts.size() < limit && hasMore) {
			// Determine the limit for the *current* internal DynamoDB query
			int ddbQueryLimit = limit - aggregatedProducts.size();
			// Cap ddbQueryLimit to DynamoDB's max internal limit
			if (ddbQueryLimit > 100) {
				ddbQueryLimit = 100;
			}

			// Query
			PaginatedResult<Product> pageResult = productRepository.findAllProducts(ddbQueryLimit, currentExclusiveStartKey);

			// Add all
			aggregatedProducts.addAll(pageResult.getItems());
			// Update for next iteration
			currentExclusiveStartKey = pageResult.getLastEvaluatedKey();
			hasMore = currentExclusiveStartKey != null && !currentExclusiveStartKey.isEmpty();
		}

		// Mapping to response
		List<ProductResponse> productResponses = aggregatedProducts.stream()
				.map(productMapper::toResponse)
				.toList();

		// Encode LastEvaluatedKey if exists using the utility
		String newNextPageToken = PaginationTokenUtil.encodeLastEvaluatedKey(currentExclusiveStartKey, objectMapper);

		// Return
		return PageResponse.<ProductResponse>builder()
				.items(productResponses)
				.nextPageToken(newNextPageToken)
				.hasMore(hasMore)
				.build();
	}

	@PreAuthorize("hasAuthority('MANAGE_DATA')")
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

	@PreAuthorize("hasAuthority('MANAGE_DATA')")
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
