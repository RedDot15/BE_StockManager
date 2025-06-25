package org.reddot15.be_stockmanager.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.response.pagination.DDBPageResponse;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import org.reddot15.be_stockmanager.util.DynamoDbPaginationUtil;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

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

    public DDBPageResponse<Product> findAllProducts(String keyword, String categoryName, Integer limit, String encodedNextPageToken) {
        final boolean useGsiQuery = categoryName != null && !categoryName.isBlank();

        // Choose the correct data-fetching function based on whether a category is present.
        BiFunction<Integer, Map<String, AttributeValue>, PaginatedResult<Product>> queryFunction;

        if (useGsiQuery) {
            queryFunction = (ddbQueryLimit, startKey) ->
                    queryProductByPKAndFilterByKeyword(
                            "category_name-gsi", categoryName, keyword, ddbQueryLimit, startKey);
        } else {
            // This path is taken when no category is specified.
            queryFunction = (ddbQueryLimit, startKey) ->
                    queryProductByPKAndFilterByKeyword(
                            null,"Products", keyword, ddbQueryLimit, startKey);
        }

        // Delegate to the generic pagination utility with the chosen function.
        return DynamoDbPaginationUtil.paginate(
                objectMapper,
                limit,
                encodedNextPageToken,
                queryFunction
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