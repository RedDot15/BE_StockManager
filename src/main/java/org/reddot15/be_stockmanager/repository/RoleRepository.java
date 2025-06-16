package org.reddot15.be_stockmanager.repository;

import org.reddot15.be_stockmanager.model.Role;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepository extends BaseMasterDataRepository<Role> {

    public RoleRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, Role.class);
    }

    public Role saveRole(Role role) {
        // Assign Partition Key as "Roles"
        role.setPk("Roles");
        return save(role);
    }

    public Optional<Role> findRoleById(String roleId) {
        // Find Role by Partition Key "Roles" and Sort Key is roleId
        return findByPkAndEntityId("Roles", roleId);
    }

    public void deleteRoleById(String roleId) {
        // Delete Role by Partition Key "Roles" and Sort Key is roleId
        deleteByPkAndEntityId("Roles", roleId);
    }

    public List<Role> findAllRoles() {
        // Get all Role by Partition Key "Roles"
        return findByPk("Roles");
    }
}