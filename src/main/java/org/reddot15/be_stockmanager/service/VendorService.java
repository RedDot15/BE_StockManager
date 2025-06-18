package org.reddot15.be_stockmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.reddot15.be_stockmanager.dto.request.VendorCreateRequest;
import org.reddot15.be_stockmanager.dto.request.VendorUpdateRequest;
import org.reddot15.be_stockmanager.dto.response.pagination.PageResponse;
import org.reddot15.be_stockmanager.dto.response.VendorResponse;
import org.reddot15.be_stockmanager.entity.Vendor;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.mapper.VendorMapper;
import org.reddot15.be_stockmanager.repository.VendorRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class VendorService {
	VendorRepository vendorRepository;
	VendorMapper vendorMapper;
	ObjectMapper objectMapper;


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
	public PageResponse<VendorResponse> getAll(Integer limit, String nextPageToken) {
		// Default limit if not provided - This remains as business logic in the service
		if (limit == null || limit <= 0) {
			limit = 10;
		}

		// Delegate the full pagination logic to the repository
		PageResponse<Vendor> vendorPage = vendorRepository.findAllVendors(limit, nextPageToken);

		// Mapping to response DTOs - This remains as presentation logic in the service
		List<VendorResponse> vendorResponses = vendorPage.getItems().stream()
				.map(vendorMapper::toResponse)
				.toList();

		// Return the paginated response with DTOs
		return PageResponse.<VendorResponse>builder()
				.items(vendorResponses)
				.nextPageToken(vendorPage.getNextPageToken()) // Propagate token from repository
				.hasMore(vendorPage.isHasMore())             // Propagate hasMore from repository
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
		if (vendorRepository.findVendorById(vendorId).isEmpty())
			throw new AppException(ErrorCode.VENDOR_NOT_FOUND);
		// Delete
		vendorRepository.deleteVendorById(vendorId);
		// Return ID
		return vendorId;
	}
}
