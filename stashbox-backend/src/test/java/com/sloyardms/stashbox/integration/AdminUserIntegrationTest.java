package com.sloyardms.stashbox.integration;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.user.entity.User;
import com.sloyardms.stashbox.user.repository.UserRepository;
import com.sloyardms.stashbox.utils.PageResponse;
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
public class AdminUserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        createAdminUser();
    }

    @Nested
    @DisplayName("GET " + ApiEndpoints.ADMIN_USERS_LIST)
    class GetUsers {

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
        @DisplayName("Should respect page size parameter")
        void shouldRespectPageSizeParameter() {
            int pageSize = 5;
            int numberOfUsers = smallPageSize;
            createUserList(numberOfUsers);

            PageResponse<UserResponse> response = fetchUsers(spec ->
                    spec.queryParam("size", pageSize)
            );

            assertPaginationMetadata(response, pageSize, numberOfUsers);
            assertThat(response.getContent()).hasSize(pageSize);
            assertCurrentAdminExcluded(response);
            assertThat(userRepository.count()).isEqualTo(numberOfUsers + 1);
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

        @Test
        @DisplayName("Should return correct page when page parameter is provided")
        void shouldReturnCorrectPageWhenPageParameterIsProvided() {
            int numberOfUsers = largePageSize;
            createUserList(numberOfUsers);

            PageResponse<UserResponse> response = fetchUsers(spec ->
                    spec.queryParam("page", 1)
            );

            int expectedSize = numberOfUsers - defaultPageSize;
            assertPaginationMetadata(response, defaultPageSize, numberOfUsers);
            assertThat(response.getPage().getNumber()).isEqualTo(1);
            assertThat(response.getContent()).hasSize(expectedSize);
            assertCurrentAdminExcluded(response);
        }

        @Test
        @DisplayName("Should return empty result when search query matches no users")
        void shouldReturnEmptyResultWhenSearchingWithNoUsers() {
            PageResponse<UserResponse> response = fetchUsers(spec ->
                    spec.queryParam("search", "nonexistent")
            );

            assertPaginationMetadata(response, defaultPageSize, 0);
            assertThat(response.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should apply sorting to search results")
        void shouldApplySortingToSearchResults() {
            createUserList(15);

            PageResponse<UserResponse> response = fetchUsers(spec -> {
                spec.queryParam("search", "1");
                spec.queryParam("sort", "username,desc");
            });

            List<String> usernames = extractUsernames(response);
            assertThat(usernames).isSortedAccordingTo(Comparator.reverseOrder());
        }

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

    @Nested
    @DisplayName("DELETE " + ApiEndpoints.ADMIN_USERS_BY_ID)
    class DeleteById {

        @Test
        @DisplayName("Should return 204 and delete user")
        void shouldReturn204AndDeleteUser() {
            User newUser = User.builder()
                    .externalId(UUID.randomUUID())
                    .username("user1")
                    .email("user1@gmail.com")
                    .build();
            newUser = userRepository.save(newUser);

            int numberOfUsers = (int) userRepository.count();

            adminUserRequest()
                    .pathParams("id", newUser.getId())
                    .when()
                    .delete(ApiEndpoints.ADMIN_USERS_BY_ID)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            int numberOfUsersAfterDelete = (int) userRepository.count();
            assertThat(numberOfUsersAfterDelete).isEqualTo(numberOfUsers - 1);
        }

        @Test
        @DisplayName("Should return 404 when user does not exists")
        void shouldReturn404WhenUserDoesNotExist() {
            UUID nonexistentId = UUID.randomUUID();

            adminUserRequest()
                    .pathParams("id", nonexistentId)
                    .when()
                    .delete(ApiEndpoints.ADMIN_USERS_BY_ID)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when token is not provided")
        void shouldReturn401WhenTokenNotProvided() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .delete(ApiEndpoints.USER_PROFILE)
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

    // ==================== HELPER METHODS ====================

    /**
     * Fetches users with default parameters
     */
    private PageResponse<UserResponse> fetchUsers() {
        return fetchUsers(spec -> {
        });
    }

    /**
     * Fetches users with custom request parameters
     *
     * @param customizer function to customize the request specification
     */
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

    /**
     * Asserts pagination metadata matches expected values
     */
    private void assertPaginationMetadata(PageResponse<?> response,
                                          int expectedSize,
                                          int expectedTotalElements) {
        int expectedTotalPages = calculateExpectedPages(expectedTotalElements, expectedSize);

        assertThat(response.getPage().getSize()).isEqualTo(expectedSize);
        assertThat(response.getPage().getTotalElements()).isEqualTo(expectedTotalElements);
        assertThat(response.getPage().getTotalPages()).isEqualTo(expectedTotalPages);
    }

    /**
     * Extracts usernames from response content
     */
    private List<String> extractUsernames(PageResponse<UserResponse> response) {
        return response.getContent()
                .stream()
                .map(UserResponse::getUsername)
                .toList();
    }

    /**
     * Asserts that the current admin user is excluded from results
     */
    private void assertCurrentAdminExcluded(PageResponse<UserResponse> response) {
        assertThat(response.getContent())
                .extracting(UserResponse::getUsername)
                .doesNotContain(ADMIN_USERNAME);
    }

    /**
     * Calculates expected number of pages for pagination
     */
    private int calculateExpectedPages(int totalElements, int pageSize) {
        if (totalElements == 0) return 0;
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    /**
     * Creates and saves a list of test users with sequential naming
     *
     * @param size number of users to create
     */
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