package org.reddot15.be_stockmanager.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder; // Dùng SuperBuilder để kế thừa builder từ lớp cha
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey; // Cho LSI

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@DynamoDbBean
public class Invoice extends BaseMasterDataItem {
    String createdAt; // LSI Sort Key
    String updatedAt;
    Double total;
    Double tax;
    List<SaleItem> sales;

    @DynamoDbSecondarySortKey(indexNames = "pk-created_at-lsi")
    @DynamoDbAttribute("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }
}