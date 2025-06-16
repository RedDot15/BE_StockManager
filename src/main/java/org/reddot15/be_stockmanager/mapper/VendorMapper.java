package org.reddot15.be_stockmanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.reddot15.be_stockmanager.dto.request.VendorCreateRequest;
import org.reddot15.be_stockmanager.dto.request.VendorUpdateRequest;
import org.reddot15.be_stockmanager.dto.response.VendorResponse;
import org.reddot15.be_stockmanager.model.Vendor;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VendorMapper {
    // Create
    Vendor toEntity(VendorCreateRequest request);

    // Response
    VendorResponse toResponse(Vendor entity);

    // Update
    void updateEntity(@MappingTarget Vendor entity, VendorUpdateRequest request);
}
