package com.sloyardms.stashbox.integration;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.user.dto.UpdateUserSettingsRequest;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.user.repository.UserRepository;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.time.temporal.ChronoUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
public class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET " + ApiEndpoints.USER_PROFILE)
    class GetCurrentUser {

        @Test
        @DisplayName("Should return 200 with user data when user exists")
        void shouldReturn200WithUserDataWhenUserExists() {
            //Create user first
            UserResponse createdUser = createNormalUser();

            UserResponse foundUser = normalUserRequest()
                    .when()
                    .get(ApiEndpoints.USER_PROFILE)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(UserResponse.class);

            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getId()).isEqualByComparingTo(createdUser.getId());
            assertThat(foundUser.getUsername()).isEqualTo(createdUser.getUsername());
            assertThat(foundUser.getEmail()).isEqualTo(createdUser.getEmail());
            assertThat(foundUser.getSettings()).isNotNull();
            assertThat(foundUser.getSettings().getDarkMode()).isEqualTo(createdUser.getSettings().getDarkMode());
            assertThat(foundUser.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)).isEqualTo(createdUser.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
            assertThat(foundUser.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS)).isEqualTo(createdUser.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS));
        }

        @Test
        @DisplayName("Should return 404 when user does not exists")
        void shouldReturn404WhenUserDoesNotExist() {
            normalUserRequest()
                    .when()
                    .get(ApiEndpoints.USER_PROFILE)
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
                    .get(ApiEndpoints.USER_PROFILE)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("POST " + ApiEndpoints.USER_PROFILE)
    class CreateUser {

        @Test
        @DisplayName("Should return 201 with user data when user is created")
        void shouldReturn201WithUserDataWhenUserIsCreated() {
            UserResponse createdUser = createNormalUser();

            assertThat(createdUser).isNotNull();
            assertThat(createdUser.getId()).isNotNull();
            assertThat(createdUser.getUsername()).isNotNull();
            assertThat(createdUser.getEmail()).isNotNull();
            assertThat(createdUser.getSettings()).isNotNull();
            assertThat(createdUser.getSettings().getDarkMode()).isFalse();
            assertThat(createdUser.getSettings().getUseFilters()).isFalse();
            assertThat(createdUser.getCreatedAt()).isNotNull();
            assertThat(createdUser.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should return 409 when user already exists (externalId, username or email)")
        void shouldReturn409WhenUserAlreadyExists() {
            //Create user first
            createNormalUser();

            //Try to create it again
            normalUserRequest()
                    .when()
                    .post(ApiEndpoints.USER_PROFILE)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        @DisplayName("Should return 401 when token is not provided")
        void shouldReturn401WhenTokenNotProvided() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .post(ApiEndpoints.USER_PROFILE)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }


    @Nested
    @DisplayName("PATCH " + ApiEndpoints.USER_SETTINGS)
    class UpdateUserSettings {

        @Test
        @DisplayName("Should return 200 and update all user settings")
        void shouldReturn200AndUpdateAllUserSettings() {
            UserResponse createdUser = createNormalUser();

            UpdateUserSettingsRequest updateRequest = new UpdateUserSettingsRequest();
            updateRequest.setDarkMode(true);
            updateRequest.setUseFilters(true);

            UserResponse updatedUser = updateUserSettings(updateRequest);

            assertThat(updatedUser).isNotNull();
            assertThat(createdUser.getId()).isEqualByComparingTo(updatedUser.getId());
            assertThat(createdUser.getUsername()).isEqualTo(updatedUser.getUsername());
            assertThat(createdUser.getEmail()).isEqualTo(updatedUser.getEmail());
            assertThat(createdUser.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)).isEqualTo(createdUser.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
            assertThat(updatedUser.getSettings()).isNotNull();
            assertThat(updatedUser.getSettings().getDarkMode()).isTrue();
            assertThat(updatedUser.getSettings().getUseFilters()).isTrue();
            assertThat(updatedUser.getUpdatedAt()).isNotNull();
            assertThat(updatedUser.getUpdatedAt()).isAfter(createdUser.getUpdatedAt());
        }

        @Test
        @DisplayName("Should return 200 and update darkMode to true")
        void shouldReturn200AndUpdateDarkModeToTrue() {
            UserResponse createdUser = createNormalUser();

            UpdateUserSettingsRequest updateRequest = new UpdateUserSettingsRequest();
            updateRequest.setDarkMode(true);

            UserResponse updatedUser = updateUserSettings(updateRequest);

            assertThat(updatedUser).isNotNull();
            assertThat(createdUser.getId()).isEqualByComparingTo(updatedUser.getId());
            assertThat(createdUser.getUsername()).isEqualTo(updatedUser.getUsername());
            assertThat(createdUser.getEmail()).isEqualTo(updatedUser.getEmail());
            assertThat(createdUser.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)).isEqualTo(createdUser.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
            assertThat(updatedUser.getSettings()).isNotNull();
            assertThat(updatedUser.getSettings().getDarkMode()).isTrue();
            assertThat(updatedUser.getSettings().getUseFilters()).isFalse();
            assertThat(updatedUser.getUpdatedAt()).isNotNull();
            assertThat(updatedUser.getUpdatedAt()).isAfter(createdUser.getUpdatedAt());
        }

        @Test
        @DisplayName("Should return 400 when no fields are provided")
        void shouldReturn400WhenNoFieldsAreProvided() {
            createNormalUser();

            UpdateUserSettingsRequest updateRequest = new UpdateUserSettingsRequest();

            normalUserRequest()
                    .body(updateRequest)
                    .when()
                    .patch(ApiEndpoints.USER_SETTINGS)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should return 400 when invalid fields are provided")
        void shouldReturn400WhenInvalidFieldsAreProvided() throws JSONException {
            createNormalUser();

            JSONObject invalidJson = new JSONObject();
            invalidJson.put("darkMode", "not-a-boolean");
            invalidJson.put("useFilters", "not-a-boolean");

            normalUserRequest()
                    .body(invalidJson.toString())
                    .when()
                    .patch(ApiEndpoints.USER_SETTINGS)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should return 401 when token is not provided")
        void shouldReturn401WhenTokenNotProvided() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .patch(ApiEndpoints.USER_SETTINGS)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }


    @Nested
    @DisplayName("DELETE " + ApiEndpoints.USER_PROFILE)
    class DeleteUser {

        @Test
        @DisplayName("Should return 204 and delete user")
        void shouldReturn204AndDeleteUser() {
            createNormalUser();

            normalUserRequest()
                    .when()
                    .delete(ApiEndpoints.USER_PROFILE)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(userRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should return 404 when user does not exists")
        void shouldReturn404WhenUserDoesNotExist() {
            normalUserRequest()
                    .when()
                    .delete(ApiEndpoints.USER_PROFILE)
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

    }

    // ==================== HELPER METHODS ====================

    private UserResponse updateUserSettings(UpdateUserSettingsRequest updateRequest) {
        return normalUserRequest()
                .body(updateRequest)
                .when()
                .patch(ApiEndpoints.USER_SETTINGS)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(UserResponse.class);
    }

}
