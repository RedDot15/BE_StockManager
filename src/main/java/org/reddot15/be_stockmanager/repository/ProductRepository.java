package org.reddot15.be_stockmanager.repository;

import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductRepository extends BaseMasterDataRepository<Product> {

    public ProductRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, Product.class);
    }

    public Product saveProduct(Product product) {
        // Assign Partition Key as "Products"
        product.setPk("Products");
        return save(product);
    }

    public PaginatedResult<Product> findAllProducts(Integer limit, Map<String, AttributeValue> exclusiveStartKey) {
        // Get all Product by Partition Key "Products"
        return findByPk("Products", limit, exclusiveStartKey);
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