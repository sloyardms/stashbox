package com.sloyardms.stashbox.integration.user;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.entity.User;
import com.sloyardms.stashbox.user.repository.UserRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
public class AdminUserDeletionIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        createAdminUser();
    }

    @Nested
    @DisplayName("SuccessfulOperations")
    class SuccessfulOperations {

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

    }

    @Nested
    @DisplayName("Resource Not Found")
    class ResourceNotFound {

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

}
