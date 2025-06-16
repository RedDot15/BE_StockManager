package org.reddot15.be_stockmanager.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@DynamoDbBean
public class Role extends BaseMasterDataItem {
    String name;
    List<String> permissionIds;

    @DynamoDbAttribute("permission_ids")
    public List<String> getPermissionIds() {
        return permissionIds;
    }
}