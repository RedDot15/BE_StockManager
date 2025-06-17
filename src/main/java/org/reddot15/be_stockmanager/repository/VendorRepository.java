package org.reddot15.be_stockmanager.repository;

import org.reddot15.be_stockmanager.entity.PaginatedResult;
import org.reddot15.be_stockmanager.entity.Vendor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class VendorRepository extends BaseMasterDataRepository<Vendor> {

    public VendorRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, Vendor.class);
    }

    public Vendor saveVendor(Vendor vendor) {
        // Assign Partition Key as "Vendors"
        vendor.setPk("Vendors");
        return save(vendor);
    }

    public PaginatedResult<Vendor> findAllVendors(Integer limit, Map<String, AttributeValue> exclusiveStartKey) {
        // Pass the pagination parameters to the base class method
        return findByPk("Vendors", limit, exclusiveStartKey);
    }

    public Optional<Vendor> findVendorById(String vendorId) {
        // Find Vendor by Partition Key "Vendors" and Sort Key is vendorId
        return findByPkAndEntityId("Vendors", vendorId);
    }

    public void deleteVendorById(String vendorId) {
        // Delete Vendor by Partition Key "Vendors" and Sort Key is vendorId
        deleteByPkAndEntityId("Vendors", vendorId);
    }

}