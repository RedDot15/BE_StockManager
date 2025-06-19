package org.reddot15.be_stockmanager.repository;

import org.reddot15.be_stockmanager.entity.Role;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.util.Optional;

@Repository
public class RoleRepository extends BaseMasterDataRepository<Role> {

    public RoleRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, Role.class);
    }

    public Optional<Role> findRoleById(String roleId) {
        // Find Role by Partition Key "Roles" and Sort Key is roleId
        return findByPkAndEntityId("Roles", roleId);
    }
}