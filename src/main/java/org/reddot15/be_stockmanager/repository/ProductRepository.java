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

    public ProductRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, Product.class);
    }

    public Product saveProduct(Product product) {
        // Assign Partition Key as "Products"
        product.setPk("Products");
        return save(product);
    }

    public PaginatedResult<Product> findAllPaginatedProducts(
            String keyword,
            String categoryName,
            Double minPrice,
            Double maxPrice,
            Integer limit,
            Map<String, AttributeValue> nextPageToken) {
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

        String index;
        QueryConditional queryConditional;
        if (useGsiQuery) {
            index = "category_name-sale_price-gsi";
            queryConditional = QueryConditionalBuilder.build(categoryName, minPrice, maxPrice);
        } else {
            index = "pk-sale_price-lsi";
            queryConditional = QueryConditionalBuilder.build("Products", minPrice, maxPrice);
        }

        return findByPk(
                index,
                queryConditional,
                limit,
                nextPageToken,
                filterExpression);
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