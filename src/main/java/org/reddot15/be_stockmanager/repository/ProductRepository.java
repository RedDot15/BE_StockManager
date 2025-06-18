package org.reddot15.be_stockmanager.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.response.pagination.PageResponse;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.util.DynamoDbPaginationUtil;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Repository
public class ProductRepository extends BaseMasterDataRepository<Product> {
    ObjectMapper objectMapper;

    public ProductRepository(DynamoDbEnhancedClient enhancedClient, ObjectMapper objectMapper) {
        super(enhancedClient, Product.class);
        this.objectMapper = objectMapper;
    }

    public Product saveProduct(Product product) {
        // Assign Partition Key as "Products"
        product.setPk("Products");
        return save(product);
    }

    public PageResponse<Product> findAllProducts(Integer limit, String encodedNextPageToken) {
        // Delegate to the generic pagination utility
        return DynamoDbPaginationUtil.paginate(
                objectMapper,
                limit,
                encodedNextPageToken,
                // Provide the specific query function for Products
                (ddbQueryLimit, currentExclusiveStartKey) ->
                        findByPk("Products", ddbQueryLimit, currentExclusiveStartKey)
        );
    }

    public Optional<Product> findProductById(String productId) {
        // Find Product by Partition Key "Products" and Sort Key is productId
        return findByPkAndEntityId("Products", productId);
    }

    public void deleteProductById(String productId) {
        // Delete Product by Partition Key "Products" and Sort Key is productId
        deleteByPkAndEntityId("Products", productId);
    }

}