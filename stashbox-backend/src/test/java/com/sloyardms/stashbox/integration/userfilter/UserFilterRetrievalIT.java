package com.sloyardms.stashbox.integration.userfilter;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.entity.User;
import com.sloyardms.stashbox.user.repository.UserRepository;
import com.sloyardms.stashbox.userfilter.dto.UserFilterResponse;
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

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
public class UserFilterRetrievalIT extends BaseIntegrationTest {

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
        @DisplayName("Should return 200 and filter data when UserFilter exists")
        void shouldReturn200AndFilterData() {
            UserFilter savedFilter = fixture.createSingleFilter(currentUser.getId(), "test");

            UserFilterResponse response = normalUserRequest()
                    .pathParams("id", savedFilter.getId())
                    .when()
                    .get(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(UserFilterResponse.class);

            assertThat(response.getId()).isEqualTo(savedFilter.getId());
            assertThat(response.getFilterName()).isEqualTo(savedFilter.getFilterName());
            assertThat(response.getUrlPattern()).isEqualTo(savedFilter.getUrlPattern());
            assertThat(response.getDomain()).isEqualTo(savedFilter.getDomain());
            assertThat(response.getExtractionRegex()).isEqualTo(savedFilter.getExtractionRegex());
            assertThat(response.getCaptureGroupIndex()).isEqualTo(savedFilter.getCaptureGroupIndex());
            assertThat(response.getPriority()).isEqualTo(savedFilter.getPriority());
            assertThat(response.getActive()).isEqualTo(savedFilter.getActive());
            assertThat(response.getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
                    .isEqualTo(savedFilter.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
            assertThat(response.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS))
                    .isEqualTo(savedFilter.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS));
        }

    }

    @Nested
    @DisplayName("Resource Not Found")
    class ResourceNotFound {

        @Test
        @DisplayName("Should return 404 when UserFilter does not exist")
        void shouldReturn404WhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();

            normalUserRequest()
                    .pathParams("id", nonExistentId)
                    .when()
                    .get(ApiEndpoints.USER_FILTER_BY_ID)
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
                    .get(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
