package org.reddot15.be_stockmanager.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@PreAuthorize("hasAuthority('VIEW_DATA')")
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
