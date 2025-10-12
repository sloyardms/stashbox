package com.sloyardms.backend.integration.user;

import com.sloyardms.backend.integration.common.BaseIntegrationTest;
import com.sloyardms.backend.user.UserRepository;
import com.sloyardms.backend.user.entity.User;
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
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("prod")
public class UserAdminControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private final String adminUserName = "admin_user";
    private final String adminPassword = "password";
    private User existingUser;

    @BeforeEach
    public void setupDatabase() {
        userRepository.deleteAll();

        existingUser = UserEntityDataBuilder.buildAndSave(userRepository);
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id}")
    class GetUserById {

        @Test
        @DisplayName("Should return 200 with user details when user exists")
        void shouldReturn200_WhenUserExists() {
            authenticatedRequest(adminUserName, adminPassword)
                    .pathParams("id", existingUser.getId())
                    .when()
                    .get("/api/v1/admin/users/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(existingUser.getId().toString()))
                    .body("externalId", equalTo(existingUser.getExternalId().toString()))
                    .body("userName", equalTo(existingUser.getUserName()))
                    .body("settings.darkMode", equalTo(false));
        }

        @Test
        @DisplayName("Should return 404 when user does not exist")
        void shouldReturn404_WhenUserDoesNotExist() {
            UUID nonExistentUserId = UUID.randomUUID();
            authenticatedRequest(adminUserName, adminPassword)
                    .pathParams("id", nonExistentUserId)
                    .when()
                    .get("/api/v1/admin/users/{id}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .get("/api/v1/admin/users/{id}", existingUser.getId())
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        void shouldReturn403_WhenUserIsNotAdmin() {
            authenticatedRequest("normal_user", "password")
                    .pathParams("id", existingUser.getId())
                    .when()
                    .get("/api/v1/admin/users/{id}")
                    .then()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

    }

    @Nested
    @DisplayName("GET /api/v1/users")
    class GetAllUsers {

        @Test
        @DisplayName("Should return 200 with paginated users when users exists")
        void shouldReturn200_WhenUsersExist() {
            authenticatedRequest(adminUserName, adminPassword)
                    .when()
                    .get("/api/v1/admin/users")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("content[0].id", equalTo(existingUser.getId().toString()))
                    .body("content[0].externalId", equalTo(existingUser.getExternalId().toString()))
                    .body("content[0].userName", equalTo(existingUser.getUserName()));
        }

        @Test
        @DisplayName("Should return 200 with empty page when users does not exist")
        void shouldReturn200WithEmptyPage_WhenNoUsersExist() {
            userRepository.deleteAll();
            authenticatedRequest(adminUserName, adminPassword)
                    .when()
                    .get("/api/v1/admin/users")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(0));
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .get("/api/v1/admin/users")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        void shouldReturn403_WhenUserIsNotAdmin() {
            authenticatedRequest("normal_user", "password")
                    .when()
                    .get("/api/v1/admin/users")
                    .then()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id}")
    class DeleteUserById {

        @Test
        @DisplayName("Should return 204 and delete user when user exists")
        void shouldReturn204_WhenUserExists() {
            authenticatedRequest(adminUserName, adminPassword)
                    .pathParams("id", existingUser.getId())
                    .when()
                    .delete("/api/v1/admin/users/{id}")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(userRepository.findAll().isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should return 404 when user does not exist")
        void shouldReturn404_WhenUserDoesNotExist() {
            UUID nonExistentUserId = UUID.randomUUID();
            authenticatedRequest(adminUserName, adminPassword)
                    .pathParams("id", nonExistentUserId)
                    .when()
                    .delete("/api/v1/admin/users/{id}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .delete("/api/v1/admin/users/{id}", existingUser.getId())
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        void shouldReturn403_WhenUserIsNotAdmin() {
            authenticatedRequest("normal_user", "password")
                    .pathParams("id", existingUser.getId())
                    .when()
                    .delete("/api/v1/admin/users/{id}")
                    .then()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

    }

}
