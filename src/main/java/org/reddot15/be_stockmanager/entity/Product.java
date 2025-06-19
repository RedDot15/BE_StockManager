package org.reddot15.be_stockmanager.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@DynamoDbBean
public class Product extends BaseMasterDataItem {
    String vendorId;
    String name;
    String categoryName;
    Double importPrice;
    Double salePrice;
    Integer amount;
    String earliestExpiry;
    Double vat;

    @DynamoDbAttribute("vendor_id")
    public String getVendorId() {
        return vendorId;
    }

    @DynamoDbAttribute("category_name")
    public String getCategoryName() {
        return categoryName;
    }

    @DynamoDbAttribute("import_price")
    public Double getImportPrice() {
        return importPrice;
    }

    @DynamoDbAttribute("sale_price")
    public Double getSalePrice() {
        return salePrice;
    }

    @DynamoDbAttribute("earliest_expiry")
    public String getEarliestExpiry() {
        return earliestExpiry;
    }
}