package org.reddot15.be_stockmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.reddot15.be_stockmanager.converter.dynamodb.AttributeValueConverter;
import org.reddot15.be_stockmanager.dto.request.VendorCreateRequest;
import org.reddot15.be_stockmanager.dto.request.VendorUpdateRequest;
import org.reddot15.be_stockmanager.dto.response.VendorPaginationResponse;
import org.reddot15.be_stockmanager.dto.response.VendorResponse;
import org.reddot15.be_stockmanager.entity.PaginatedResult;
import org.reddot15.be_stockmanager.entity.Vendor;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.mapper.VendorMapper;
import org.reddot15.be_stockmanager.repository.VendorRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class VendorService {
	VendorRepository vendorRepository;
	VendorMapper vendorMapper;
	private final ObjectMapper objectMapper;

	@PreAuthorize("hasAuthority('MANAGE_DATA')")
	public VendorResponse create(VendorCreateRequest request) {
		// Duplicate exception
		Optional<Vendor> optionalVendor = vendorRepository.findVendorById(request.getEntityId());
		if (optionalVendor.isPresent())
			throw new AppException(ErrorCode.VENDOR_DUPLICATE);
		// Mappping
		Vendor entity = vendorMapper.toEntity(request);
		// Save
		return vendorMapper.toResponse(vendorRepository.saveVendor(entity));
	}

	@PreAuthorize("hasAuthority('MANAGE_DATA')")
	public VendorPaginationResponse getAll(Integer limit, String nextPageToken) {
		// Default limit if not provided
		if (limit == null || limit <= 0) {
			limit = 10;
		}
		// Max limit: Prevent clients from requesting too many items
		if (limit > 50) {
			limit = 50;
		}
		// Decode & Assign ExclusiveStartKey if exists
		Map<String, AttributeValue> exclusiveStartKey = null;
		if (nextPageToken != null && !nextPageToken.trim().isEmpty()) {
			try {
				// 1. Decode base64
				String decodedString = new String(Base64.getUrlDecoder().decode(nextPageToken));
				// 2. Deserialize JSON string back to Map<String, String>
				Map<String, String> stringMap = objectMapper.readValue(decodedString, Map.class);
				// 3. Convert Map<String,String> -> Map<String,AttributeValue>
				exclusiveStartKey = AttributeValueConverter.convertMapStringToAttributeValue(stringMap);
			} catch (IOException e) {
				throw new AppException(ErrorCode.INVALID_PAGINATION_TOKEN);
			}
		}

		// Find all paginated vendors
		PaginatedResult<Vendor> paginatedResult = vendorRepository.findAllVendors(limit, exclusiveStartKey);
		// Get items
		List<VendorResponse> vendorResponses = paginatedResult.getItems().stream()
				.map(vendorMapper::toResponse)
				.toList();

		// Encode LastEvaluatedKey if exists
		String newNextPageToken = null;
		boolean hasMore = false;
		if (paginatedResult.getLastEvaluatedKey() != null && !paginatedResult.getLastEvaluatedKey().isEmpty()) {
			try {
				// 1. Convert Map<String,AttributeValue> -> Map<String,String>
				Map<String, String> stringMap = AttributeValueConverter
						.convertMapAttributeValueToString(paginatedResult.getLastEvaluatedKey());
				// 2. Write as json string
				String jsonString = objectMapper.writeValueAsString(stringMap);
				// 3. Base64 encode
				newNextPageToken = Base64.getUrlEncoder().encodeToString(jsonString.getBytes());
				hasMore = true;
			} catch (IOException e) {
				throw new AppException(ErrorCode.SERIALIZE_PAGINATION_TOKEN_FAILED);
			}
		}
		// Return
		return VendorPaginationResponse.builder()
				.vendors(vendorResponses)
				.nextPageToken(newNextPageToken)
				.hasMore(hasMore)
				.build();
	}

	@PreAuthorize("hasAuthority('MANAGE_DATA')")
	public VendorResponse update(String vendorId, VendorUpdateRequest request) {
		// Check exists
		Vendor entity = vendorRepository.findVendorById(vendorId)
				.orElseThrow(() -> new AppException(ErrorCode.VENDOR_NOT_FOUND));
		// Updating
		vendorMapper.updateEntity(entity, request);
		// Save
		return vendorMapper.toResponse(vendorRepository.saveVendor(entity));
	}

	@PreAuthorize("hasAuthority('MANAGE_DATA')")
	public String delete(String vendorId) {
		// Check exists
		Vendor entity = vendorRepository.findVendorById(vendorId)
				.orElseThrow(() -> new AppException(ErrorCode.VENDOR_NOT_FOUND));
		// Delete
		vendorRepository.deleteVendorById(vendorId);
		// Return ID
		return vendorId;
	}
}
