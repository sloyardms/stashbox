package com.sloyardms.stashbox.integration.user;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.dto.PageResponse;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.user.entity.User;
import com.sloyardms.stashbox.user.repository.UserRepository;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
public class AdminUserSearchIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        createAdminUser();
    }

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 200 and paginated users excluding current admin")
        void shouldReturn200AndPaginatedUsersExcludingCurrentAdmin() {
            int numberOfUsers = largePageSize;
            createUserList(numberOfUsers);

            PageResponse<UserResponse> response = fetchUsers();

            assertPaginationMetadata(response, defaultPageSize, numberOfUsers);
            assertThat(response.getContent()).hasSize(Math.min(numberOfUsers, defaultPageSize));
            assertCurrentAdminExcluded(response);
            assertThat(userRepository.count()).isEqualTo(numberOfUsers + 1);
        }

        @Test
        @DisplayName("Should return 200 and empty page when only current admin exists")
        void shouldReturn200AndEmptyPageWhenOnlyCurrentAdminExists() {
            PageResponse<UserResponse> response = fetchUsers();

            assertPaginationMetadata(response, defaultPageSize, 0);
            assertThat(response.getPage().getNumber()).isEqualTo(0);
            assertThat(response.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should sort by username ascending by default")
        void shouldSortByUsernameAscendingByDefault() {
            int numberOfUsers = largePageSize;
            createUserList(numberOfUsers);

            PageResponse<UserResponse> response = fetchUsers();

            List<String> usernames = extractUsernames(response);

            assertThat(usernames).isSorted();
            assertPaginationMetadata(response, defaultPageSize, numberOfUsers);
            assertThat(response.getContent()).hasSize(Math.min(numberOfUsers, defaultPageSize));
            assertCurrentAdminExcluded(response);
            assertThat(userRepository.count()).isEqualTo(numberOfUsers + 1);
        }

        @Test
        @DisplayName("Should sort by username descending when specified")
        void shouldSortByUsernameDescending() {
            int numberOfUsers = largePageSize;
            createUserList(numberOfUsers);

            PageResponse<UserResponse> response = fetchUsers(spec ->
                    spec.queryParam("sort", "username,desc")
            );

            List<String> usernames = extractUsernames(response);

            assertThat(usernames).isSortedAccordingTo(Comparator.reverseOrder());
            assertPaginationMetadata(response, defaultPageSize, numberOfUsers);
            assertThat(response.getContent()).hasSize(Math.min(numberOfUsers, defaultPageSize));
            assertCurrentAdminExcluded(response);
            assertThat(userRepository.count()).isEqualTo(numberOfUsers + 1);
        }

        @Test
        @DisplayName("Should filter by username search query")
        void shouldFilterByUsernameSearchQuery() {
            int numberOfUsers = smallPageSize;
            createUserList(numberOfUsers);

            PageResponse<UserResponse> response = fetchUsers(spec ->
                    spec.queryParam("search", "2")
            );

            // Matches: user2, user12
            int expectedMatches = 2;
            assertPaginationMetadata(response, defaultPageSize, expectedMatches);
            assertThat(response.getContent()).hasSize(expectedMatches);
            assertCurrentAdminExcluded(response);
        }

        @Test
        @DisplayName("Should filter by email search query")
        void shouldFilterByEmailSearchQuery() {
            int numberOfUsers = smallPageSize;
            createUserList(numberOfUsers);

            PageResponse<UserResponse> response = fetchUsers(spec ->
                    spec.queryParam("search", "3@gmail")
            );

            // Matches: user3@gmail.com, user13@gmail.com
            int expectedMatches = 2;
            assertPaginationMetadata(response, defaultPageSize, expectedMatches);
            assertThat(response.getContent()).hasSize(expectedMatches);
            assertCurrentAdminExcluded(response);
        }

    }

    @Nested
    @DisplayName("Validation Errors")
    class ValidationErrors {

        @Test
        @DisplayName("Should return 400 when sorting by invalid field")
        void shouldReturn400WhenSortingByInvalidField() {
            adminUserRequest()
                    .queryParam("sort", "invalidField,desc")
                    .when()
                    .get(ApiEndpoints.ADMIN_USERS_LIST)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

    }

    @Nested
    @DisplayName("Authentication and Authorization")
    class AuthenticationAndAuthorization {

        @Test
        @DisplayName("Should return 401 when token is not provided")
        void shouldReturn401WhenTokenNotProvided() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get(ApiEndpoints.ADMIN_USERS_LIST)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("Should return 403 when authenticated user is not ADMIN")
        void shouldReturn403WhenAuthenticatedUserIsNotAdmin() {
            normalUserRequest()
                    .when()
                    .get(ApiEndpoints.ADMIN_USERS_LIST)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

    }

    /**
     * HELPER METHODS
     */
    private PageResponse<UserResponse> fetchUsers() {
        return fetchUsers(spec -> {
        });
    }

    private PageResponse<UserResponse> fetchUsers(Consumer<RequestSpecification> customizer) {
        RequestSpecification request = adminUserRequest();
        customizer.accept(request);

        return request
                .when()
                .get(ApiEndpoints.ADMIN_USERS_LIST)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });
    }

    private void assertCurrentAdminExcluded(PageResponse<UserResponse> response) {
        assertThat(response.getContent())
                .extracting(UserResponse::getUsername)
                .doesNotContain(ADMIN_USERNAME);
    }

    private List<String> extractUsernames(PageResponse<UserResponse> response) {
        return response.getContent()
                .stream()
                .map(UserResponse::getUsername)
                .toList();
    }

    private void createUserList(int size) {
        List<User> users = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            String username = "user" + i;
            users.add(User.builder()
                    .externalId(UUID.randomUUID())
                    .username(username)
                    .email(username + "@gmail.com")
                    .build());
        }

        userRepository.saveAll(users);
    }
}
