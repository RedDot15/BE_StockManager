package org.reddot15.be_stockmanager.model;

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
public class SaleItem {
    String productId;
    String vendorId;
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