package org.reddot15.be_stockmanager.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
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

    @DynamoDbSecondarySortKey(indexNames = "pk-email-lsi")
    public String getEmail() {
        return email;
    }

    @DynamoDbAttribute("role_ids")
    public List<String> getRoleIds() {
        return roleIds;
    }
}