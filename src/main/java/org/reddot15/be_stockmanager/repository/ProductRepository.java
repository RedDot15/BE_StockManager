package org.reddot15.be_stockmanager.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.response.pagination.DDBPageResponse;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import org.reddot15.be_stockmanager.util.DynamoDbPaginationUtil;
import org.reddot15.be_stockmanager.util.QueryConditionalBuilder;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
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

    public DDBPageResponse<Product> findAllPaginatedProducts(
            String keyword,
            String categoryName,
            Double minPrice,
            Double maxPrice,
            Integer limit,
            String encodedNextPageToken) {
        final boolean useGsiQuery = categoryName != null && !categoryName.isBlank();

        // Build the filter expression if a keyword is provided.
        Expression filterExpression;
        if (keyword != null && !keyword.isBlank()) {
            filterExpression = Expression.builder()
                    .expression("(contains(#name, :keyword) OR contains(#vendorId, :keyword))")
                    .putExpressionName("#name", "name")
                    .putExpressionName("#vendorId", "vendor_id")
                    .putExpressionValue(":keyword", AttributeValue.builder().s(keyword).build())
                    .build();
        } else {
            filterExpression = null;
        }

        // Choose the correct data-fetching function based on whether a category is present.
        BiFunction<Integer, Map<String, AttributeValue>, PaginatedResult<Product>> queryFunction;

        if (useGsiQuery) {
            queryFunction = (ddbQueryLimit, startKey) ->
                    findByPk(
                            "category_name-sale_price-gsi",
                            QueryConditionalBuilder.build(categoryName, minPrice, maxPrice),
                            ddbQueryLimit,
                            startKey,
                            filterExpression,
                            false);
        } else {
            // This path is taken when no category is specified.
            queryFunction = (ddbQueryLimit, startKey) ->
                    findByPk(
                            "pk-sale_price-lsi",
                            QueryConditionalBuilder.build("Products", minPrice, maxPrice),
                            ddbQueryLimit,
                            startKey,
                            filterExpression,
                            false);
        }

        // Delegate to the generic pagination utility with the chosen function.
        return DynamoDbPaginationUtil.paginate(
                objectMapper,
                limit,
                encodedNextPageToken,
                queryFunction
        );
    }

    public List<Product> findAllProducts(
            String keyword,
            String categoryName,
            Double minPrice,
            Double maxPrice) {
        final boolean useGsiQuery = categoryName != null && !categoryName.isBlank();

        // Build the filter expression if a keyword is provided.
        Expression filterExpression;
        if (keyword != null && !keyword.isBlank()) {
            filterExpression = Expression.builder()
                    .expression("(contains(#name, :keyword) OR contains(#vendorId, :keyword))")
                    .putExpressionName("#name", "name")
                    .putExpressionName("#vendorId", "vendor_id")
                    .putExpressionValue(":keyword", AttributeValue.builder().s(keyword).build())
                    .build();
        } else {
            filterExpression = null;
        }

        List<Product> products;
        if (useGsiQuery) {
            products = findByPk(
                    "category_name-sale_price-gsi",
                    QueryConditionalBuilder.build(categoryName, minPrice, maxPrice),
                    null,
                    null,
                    filterExpression,
                    true)
                    .getItems();
        } else {
            // This path is taken when no category is specified.
            products = findByPk(
                    "pk-sale_price-lsi",
                    QueryConditionalBuilder.build("Products", minPrice, maxPrice),
                    null,
                    null,
                    filterExpression,
                    true)
                    .getItems();
        }

        // Return all products
        return products;
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