package org.reddot15.be_stockmanager.mapper;

import org.apache.commons.csv.CSVRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.reddot15.be_stockmanager.dto.request.ProductCreateRequest;
import org.reddot15.be_stockmanager.dto.request.ProductUpdateRequest;
import org.reddot15.be_stockmanager.dto.response.ProductResponse;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.util.TimeValidator;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    // Create
    Product toEntity(ProductCreateRequest request);

    // Response
    ProductResponse toResponse(Product entity);

    // Update
    void updateEntity(@MappingTarget Product entity, ProductUpdateRequest request);

    default void updateExistingProduct(Product existingProduct, Product importedProduct) {
        // Product information mismatch exception
        if (!existingProduct.equals(importedProduct)) {
            throw new AppException(ErrorCode.PRODUCT_MISMATCH);
        }
        // Calculate up product amount
        existingProduct.setAmount(existingProduct.getAmount() + importedProduct.getAmount());
        // Update earliest expiry time
        if (importedProduct.getEarliestExpiry().compareTo(existingProduct.getEarliestExpiry()) < 0) {
            existingProduct.setEarliestExpiry(importedProduct.getEarliestExpiry());
        }
    }

    default Product toProduct(CSVRecord csvRecord) {
        return Product.builder()
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
    }
}
