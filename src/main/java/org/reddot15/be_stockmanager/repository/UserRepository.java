package org.reddot15.be_stockmanager.repository;

import org.reddot15.be_stockmanager.entity.User;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserRepository extends BaseMasterDataRepository<User> {

    public UserRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, User.class);
    }

    public Optional<User> findUserByEmail(String email) {
        // Query LSI
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue("Users")
                        .sortValue(email)
                        .build()
        );

        List<User> users = table.index("pk-email-lsi") // Tên của LSI
                .query(queryConditional)
                .stream()
                .flatMap(page -> page.items().stream())
                .toList();

        if (users.isEmpty()) {
            return Optional.empty();
        } else if (users.size() == 1) {
            return Optional.of(users.getFirst());
        } else {
            throw new AppException(ErrorCode.INVALID_USER_QUERY);
        }
    }
}