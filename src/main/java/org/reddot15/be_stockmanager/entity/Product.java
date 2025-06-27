package org.reddot15.be_stockmanager.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

import java.util.Objects;

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

    @DynamoDbSecondaryPartitionKey(indexNames = "category_name-sale_price-gsi")
    @DynamoDbAttribute("category_name")
    public String getCategoryName() {
        return categoryName;
    }

    @DynamoDbAttribute("import_price")
    public Double getImportPrice() {
        return importPrice;
    }

    @DynamoDbSecondarySortKey(indexNames = {"category_name-sale_price-gsi", "pk-sale_price-lsi"})
    @DynamoDbAttribute("sale_price")
    public Double getSalePrice() {
        return salePrice;
    }

    @DynamoDbAttribute("earliest_expiry")
    public String getEarliestExpiry() {
        return earliestExpiry;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(vendorId, product.vendorId) &&
                Objects.equals(name, product.name) &&
                Objects.equals(categoryName, product.categoryName) &&
                Objects.equals(importPrice, product.importPrice) &&
                Objects.equals(salePrice, product.salePrice) &&
                Objects.equals(vat, product.vat);
    }
}