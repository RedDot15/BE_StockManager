package org.reddot15.be_stockmanager.repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.entity.Vendor;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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

    public PaginatedResult<Vendor> findVendors(
            Map<String, AttributeValue> nextPageToken,
            Integer limit) {
        // Delegate to the generic pagination utility
        return findByPk(
                null,
                QueryConditional.keyEqualTo(Key.builder().partitionValue("Vendors").build()),
                null,
                nextPageToken,
                limit);
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