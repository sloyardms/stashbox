package com.sloyardms.stashbox.integration.user;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.dto.UpdateUserSettingsRequest;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.user.repository.UserRepository;
import io.restassured.http.ContentType;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
public class UserSettingsUpdateIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 200 and update darkMode setting to true")
        void shouldReturn200AndUpdateDarkModeToTrue() {
            UserResponse user = createNormalUser();
            assertThat(user.getSettings().getDarkMode()).isFalse();

            UpdateUserSettingsRequest request = UpdateUserSettingsRequest.builder()
                    .darkMode(true)
                    .build();

            UserResponse response = normalUserRequest()
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_SETTINGS)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(UserResponse.class);

            assertThat(response.getSettings().getDarkMode()).isTrue();
            assertThat(response.getSettings().getUseFilters()).isFalse();
        }

        @Test
        @DisplayName("Should return 200 and update useFilters setting to true")
        void shouldReturn200AndUpdateUseFiltersToTrue() {
            UserResponse user = createNormalUser();
            assertThat(user.getSettings().getUseFilters()).isFalse();

            UpdateUserSettingsRequest request = UpdateUserSettingsRequest.builder()
                    .useFilters(true)
                    .build();

            UserResponse response = normalUserRequest()
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_SETTINGS)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(UserResponse.class);

            assertThat(response.getSettings().getUseFilters()).isTrue();
            assertThat(response.getSettings().getDarkMode()).isFalse();
        }

        @Test
        @DisplayName("Should return 200 and update both settings")
        void shouldReturn200AndUpdateBothSettings() {
            UserResponse user = createNormalUser();

            UpdateUserSettingsRequest request = UpdateUserSettingsRequest.builder()
                    .darkMode(true)
                    .useFilters(true)
                    .build();

            UserResponse response = normalUserRequest()
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_SETTINGS)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(UserResponse.class);

            assertThat(response.getSettings().getDarkMode()).isTrue();
            assertThat(response.getSettings().getUseFilters()).isTrue();
        }

        @Test
        @DisplayName("Should return 200 and only update specified setting (partial update)")
        void shouldReturn200AndOnlyUpdateSpecifiedSetting() {
            UserResponse user = createNormalUser();

            // First, set darkMode to true
            UpdateUserSettingsRequest firstRequest = UpdateUserSettingsRequest.builder()
                    .darkMode(true)
                    .build();

            normalUserRequest()
                    .body(firstRequest)
                    .when()
                    .patch(ApiEndpoints.USER_SETTINGS)
                    .then()
                    .statusCode(HttpStatus.OK.value());

            // Then, only update useFilters, leaving darkMode unchanged
            UpdateUserSettingsRequest secondRequest = UpdateUserSettingsRequest.builder()
                    .useFilters(true)
                    .build();

            UserResponse response = normalUserRequest()
                    .body(secondRequest)
                    .when()
                    .patch(ApiEndpoints.USER_SETTINGS)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(UserResponse.class);

            assertThat(response.getSettings().getDarkMode()).isTrue();
            assertThat(response.getSettings().getUseFilters()).isTrue();
        }
    }

    @Nested
    @DisplayName("Validation Errors")
    class ValidationErrors {

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void shouldReturn400WhenRequestBodyIsEmpty() {
            createNormalUser();

            UpdateUserSettingsRequest request = UpdateUserSettingsRequest.builder().build();

            normalUserRequest()
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_PROFILE + "/settings")
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should return 400 when invalid field types are provided")
        void shouldReturn400WhenInvalidFieldTypesAreProvided() throws Exception {
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
                    .get(ApiEndpoints.USER_PROFILE)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

}
