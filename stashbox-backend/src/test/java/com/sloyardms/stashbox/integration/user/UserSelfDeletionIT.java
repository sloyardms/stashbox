package com.sloyardms.stashbox.integration.user;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.repository.UserRepository;
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

@ActiveProfiles("dev")
public class UserSelfDeletionIT extends BaseIntegrationTest {

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
        @DisplayName("Should delete user successfully")
        void deleteUserSuccessfully() {
            createNormalUser();

            normalUserRequest()
                    .when()
                    .delete(ApiEndpoints.USER_PROFILE)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(userRepository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Resource Not Found")
    class ResourceNotFound {

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

    }

}
