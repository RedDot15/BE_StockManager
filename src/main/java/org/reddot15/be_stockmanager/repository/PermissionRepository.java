package org.reddot15.be_stockmanager.repository;

import org.reddot15.be_stockmanager.entity.Permission;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.util.List;
import java.util.Optional;

@Repository
public class PermissionRepository extends BaseMasterDataRepository<Permission> {

    public PermissionRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, Permission.class);
    }

    public Optional<Permission> findPermissionById(String permissionId) {
        // Find Permission by Partition Key "Permissions" and Sort Key is permissionId
        return findByPkAndEntityId("Permissions", permissionId);
    }
}