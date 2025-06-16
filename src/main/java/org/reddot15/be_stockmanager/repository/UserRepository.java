package org.reddot15.be_stockmanager.repository;

import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.model.User;
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

    public User saveUser(User user) {
        // Assign Partition Key as "Users"
        user.setPk("Users");
        return save(user);
    }

    public Optional<User> findUserById(String userId) {
        // Find Role by Partition Key "Users" and Sort Key is userId
        return findByPkAndEntityId("Users", userId);
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

    public void deleteUserById(String userId) {
        // Delete Role by Partition Key "Users" and Sort Key is userId
        deleteByPkAndEntityId("Users", userId);
    }

    public List<User> findAllUsers() {
        // Get all User by Partition Key "Users"
        return findByPk("Users");
    }
}