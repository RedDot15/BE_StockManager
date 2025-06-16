package org.reddot15.be_stockmanager.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.reddot15.be_stockmanager.dto.request.VendorCreateRequest;
import org.reddot15.be_stockmanager.dto.request.VendorUpdateRequest;
import org.reddot15.be_stockmanager.dto.response.VendorResponse;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.mapper.VendorMapper;
import org.reddot15.be_stockmanager.model.Vendor;
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
public class VendorService {
	VendorRepository vendorRepository;
	VendorMapper vendorMapper;

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
	public List<VendorResponse> getAll() {
		// Get all
		return vendorRepository.findAllVendors()
				.stream()
				.map(vendorMapper::toResponse)
				.toList();
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
