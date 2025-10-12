package com.sloyardms.backend.integration.user;

import com.sloyardms.backend.integration.common.BaseIntegrationTest;
import com.sloyardms.backend.user.UserRepository;
import com.sloyardms.backend.user.dto.UserDetailDto;
import com.sloyardms.backend.user.dto.UserSettingsUpdateDto;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("prod")
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private final String normalUserName = "normal_user";
    private final String normalUserPassword = "password";
    private UserDetailDto existingUser;

    @BeforeEach
    public void setupDatabase() {
        userRepository.deleteAll();

        // Create normal user
        existingUser = authenticatedRequest(normalUserName, normalUserPassword)
                .when()
                .post("/api/v1/me")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(ContentType.JSON)
                .extract()
                .as(UserDetailDto.class);
    }

    @Nested
    @DisplayName("GET /api/v1/me")
    class GetUserByExternalId {

        @Test
        @DisplayName("Should return 200 with user details when user exists")
        void shouldReturn200_WhenUserExists() {
            authenticatedRequest(normalUserName, normalUserPassword)
                    .when()
                    .get("/api/v1/me")
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
            userRepository.deleteAll();
            assertThat(userRepository.findAll()).isEmpty();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .when()
                    .get("/api/v1/me")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .get("/api/v1/me")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("POST /api/v1/me")
    class CreateUser {

        @Test
        @DisplayName("Should return 201 with user details when user is created")
        void shouldReturn201_WhenUserIsCreated() {
            userRepository.deleteAll();
            assertThat(userRepository.findAll()).isEmpty();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .when()
                    .post("/api/v1/me")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .contentType(ContentType.JSON)
                    .extract()
                    .as(UserDetailDto.class);

            assertThat(userRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("Should return 409 when user already exists")
        void shouldReturn409_WhenUserAlreadyExists() {
            authenticatedRequest(normalUserName, normalUserPassword)
                    .when()
                    .post("/api/v1/me")
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .post("/api/v1/me")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("PATCH /api/v1/me/settings")
    class UpdateUser {

        @Test
        @DisplayName("Should return 204 and update user settings when body is valid")
        void shouldReturn204_WhenUserIsUpdated() {
            UserSettingsUpdateDto updateDto = UserSettingsUpdateDto.builder().darkMode(true).build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .body(updateDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .patch("/api/v1/me/settings")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(existingUser.getId().toString()))
                    .body("externalId", equalTo(existingUser.getExternalId().toString()))
                    .body("userName", equalTo(existingUser.getUserName()))
                    .body("settings.darkMode", equalTo(true));
        }

        @Test
        @DisplayName("Should return 400 when body is invalid")
        void shouldReturn400_WhenBodyIsInvalid() {
            // Empty body
            UserSettingsUpdateDto updateDto = UserSettingsUpdateDto.builder().build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .body(updateDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .patch("/api/v1/me/settings")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should return 404 when user does not exist")
        void shouldReturn404_WhenUserDoesNotExist() {
            userRepository.deleteAll();
            assertThat(userRepository.findAll()).isEmpty();

            UserSettingsUpdateDto updateDto = UserSettingsUpdateDto.builder().darkMode(true).build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .body(updateDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .patch("/api/v1/me/settings")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            UserSettingsUpdateDto updateDto = UserSettingsUpdateDto.builder().build();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .body(updateDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .patch("/api/v1/me/settings")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

    }

    @Nested
    @DisplayName("DELETE /api/v1/me")
    class DeleteUser {

        @Test
        @DisplayName("Should return 204 when user is deleted")
        void shouldReturn204_WhenUserIsDeleted() {
            authenticatedRequest(normalUserName, normalUserPassword)
                    .when()
                    .delete("/api/v1/me")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(userRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should return 404 when user does not exists")
        void shouldReturn404_WhenUserDoesNotExist() {
            userRepository.deleteAll();
            assertThat(userRepository.findAll()).isEmpty();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .when()
                    .delete("/api/v1/me")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .delete("/api/v1/me")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
