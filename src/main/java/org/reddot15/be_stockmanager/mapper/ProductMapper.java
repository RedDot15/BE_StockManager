package org.reddot15.be_stockmanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.reddot15.be_stockmanager.dto.request.ProductCreateRequest;
import org.reddot15.be_stockmanager.dto.request.ProductUpdateRequest;
import org.reddot15.be_stockmanager.dto.request.VendorCreateRequest;
import org.reddot15.be_stockmanager.dto.request.VendorUpdateRequest;
import org.reddot15.be_stockmanager.dto.response.ProductResponse;
import org.reddot15.be_stockmanager.dto.response.VendorResponse;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.Vendor;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    // Create
    Product toEntity(ProductCreateRequest request);

    // Response
    ProductResponse toResponse(Product entity);

    // Update
    void updateEntity(@MappingTarget Product entity, ProductUpdateRequest request);
}
