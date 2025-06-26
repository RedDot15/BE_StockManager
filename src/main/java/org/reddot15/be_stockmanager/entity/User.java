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

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@DynamoDbBean
public class User extends BaseMasterDataItem {
    String email;
    String password;
    List<String> roleIds;

    @DynamoDbSecondaryPartitionKey(indexNames = "email-gsi")
    public String getEmail() {
        return email;
    }

    @DynamoDbAttribute("role_ids")
    public List<String> getRoleIds() {
        return roleIds;
    }
}