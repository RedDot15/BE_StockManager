package org.reddot15.be_stockmanager.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.response.pagination.DDBPageResponse;
import org.reddot15.be_stockmanager.entity.Vendor;
import org.reddot15.be_stockmanager.util.DynamoDbPaginationUtil;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Repository
public class VendorRepository extends BaseMasterDataRepository<Vendor> {
    ObjectMapper objectMapper;

    public VendorRepository(DynamoDbEnhancedClient enhancedClient, ObjectMapper objectMapper) {
        super(enhancedClient, Vendor.class);
        this.objectMapper = objectMapper;
    }

    public Vendor saveVendor(Vendor vendor) {
        // Assign Partition Key as "Vendors"
        vendor.setPk("Vendors");
        return save(vendor);
    }

    public DDBPageResponse<Vendor> findAllVendors(Integer limit, String encodedNextPageToken) {
        // Delegate to the generic pagination utility
        return DynamoDbPaginationUtil.paginate(
                objectMapper,
                limit,
                encodedNextPageToken,
                // Provide the specific query function for Vendors
                (ddbQueryLimit, currentExclusiveStartKey) ->
                        findByPk("Vendors", ddbQueryLimit, currentExclusiveStartKey)
        );
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