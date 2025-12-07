package com.sloyardms.stashbox.integration.user;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.user.repository.UserRepository;
import io.restassured.http.ContentType;
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
public class UserSelfRetrievalIT extends BaseIntegrationTest {

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
        @DisplayName("Should return user data successfully")
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
    }

    @Nested
    @DisplayName("Resource Not Found")
    class ResourceNotFound {

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
