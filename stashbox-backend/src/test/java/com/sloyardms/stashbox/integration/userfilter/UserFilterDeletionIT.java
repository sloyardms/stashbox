package com.sloyardms.stashbox.integration.userfilter;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.entity.User;
import com.sloyardms.stashbox.user.repository.UserRepository;
import com.sloyardms.stashbox.userfilter.entity.UserFilter;
import com.sloyardms.stashbox.userfilter.repository.UserFilterRepository;
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
public class UserFilterDeletionIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFilterRepository userFilterRepository;

    @Autowired
    private UserFilterFixture fixture;

    private User currentUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAllInBatch();
        userFilterRepository.deleteAllInBatch();

        createNormalUser();
        this.currentUser = userRepository.findAll().getFirst();
    }

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 204 and delete filter")
        void shouldReturn204AndDelete() {
            UserFilter filter = fixture.createSingleFilter(currentUser.getId(), "test");
            long countBefore = userFilterRepository.count();

            normalUserRequest()
                    .pathParam("id", filter.getId())
                    .when()
                    .delete(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(userFilterRepository.count()).isEqualTo(countBefore - 1);
        }

    }

    @Nested
    @DisplayName("Resource Not Found")
    class ResourceNotFound {

        @Test
        @DisplayName("Should return 404 when filter does not exist")
        void shouldReturn404WhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();

            normalUserRequest()
                    .pathParams("id", nonExistentId)
                    .when()
                    .delete(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

    }

    @Nested
    @DisplayName("Authentication and Authorization")
    class AuthenticationAndAuthorization {

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            UUID id = UUID.randomUUID();

            given()
                    .pathParams("id", id)
                    .contentType(ContentType.JSON)
                    .when()
                    .delete(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
