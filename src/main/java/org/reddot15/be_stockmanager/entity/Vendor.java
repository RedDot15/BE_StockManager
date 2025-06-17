package org.reddot15.be_stockmanager.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder; // Cần dùng SuperBuilder

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@DynamoDbBean
public class Vendor extends BaseMasterDataItem {
    String name;
}