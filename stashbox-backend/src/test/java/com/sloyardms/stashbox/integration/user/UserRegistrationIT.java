package com.sloyardms.stashbox.integration.user;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.dto.UserResponse;
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
public class UserRegistrationIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() {
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

    }

    @Nested
    @DisplayName("Business Logic Errors")
    class ValidationErrors {

        @Test
        @DisplayName("Should return 409 when user externalId already exists")
        void shouldReturn409WhenUserExternalIdExists() {
            UserResponse createdUser = createNormalUser();

            normalUserRequest()
                    .when()
                    .post(ApiEndpoints.USER_PROFILE)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        @DisplayName("Should return 409 when username already exists")
        void shouldReturn409WhenUsernameExists() {
            UserResponse createdUser = createNormalUser();

            User user = User.builder()
                    .externalId(UUID.randomUUID())
                    .username(createdUser.getUsername())
                    .email("another@email.com")
                    .build();

            userRepository.deleteAllInBatch();
            userRepository.save(user);

            normalUserRequest()
                    .when()
                    .post(ApiEndpoints.USER_PROFILE)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        @DisplayName("Should return 409 when email already exists")
        void shouldReturn409WhenEmailExists() {
            UserResponse createdUser = createNormalUser();

            User user = User.builder()
                    .externalId(UUID.randomUUID())
                    .username("another_username")
                    .email(createdUser.getEmail())
                    .build();

            userRepository.deleteAllInBatch();
            userRepository.save(user);

            normalUserRequest()
                    .when()
                    .post(ApiEndpoints.USER_PROFILE)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.CONFLICT.value());
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
                    .post(ApiEndpoints.USER_PROFILE)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
