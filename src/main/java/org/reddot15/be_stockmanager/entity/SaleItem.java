package org.reddot15.be_stockmanager.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@DynamoDbBean
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SaleItem {
    String productId;
    String vendorId;
    String categoryName;
    Double amount;
    Double price;
    Double vat;

    @DynamoDbAttribute(value = "product_id")
    public String getProductId() {
        return productId;
    }

    @DynamoDbAttribute(value = "vendor_id")
    public String getVendorId() {
        return vendorId;
    }
}