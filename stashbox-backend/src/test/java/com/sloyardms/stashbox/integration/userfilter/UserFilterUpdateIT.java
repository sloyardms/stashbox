package com.sloyardms.stashbox.integration.userfilter;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.user.entity.User;
import com.sloyardms.stashbox.user.repository.UserRepository;
import com.sloyardms.stashbox.userfilter.dto.UpdateUserFilterRequest;
import com.sloyardms.stashbox.userfilter.dto.UserFilterResponse;
import com.sloyardms.stashbox.userfilter.entity.UserFilter;
import com.sloyardms.stashbox.userfilter.repository.UserFilterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
public class UserFilterUpdateIT extends BaseIntegrationTest {

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
        @DisplayName("Should update all fields successfully")
        void shouldUpdateAllFields() {
            UserFilter savedFilter = fixture.createTestUserFilter(currentUser.getId());

            UpdateUserFilterRequest request = UpdateUserFilterRequest.builder()
                    .filterName("updated filter name")
                    .description("updated description")
                    .urlPattern("https://www.updated-site.com/path/to/resource")
                    .domain("www.updated-site.com")
                    .extractionRegex("/path/([^/?#]+)")
                    .captureGroupIndex(2)
                    .priority(5)
                    .active(false)
                    .build();

            UserFilterResponse response = normalUserRequest()
                    .pathParam("id", savedFilter.getId())
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(UserFilterResponse.class);

            assertThat(response.getId()).isEqualTo(savedFilter.getId());
            assertThat(response.getFilterName()).isEqualTo(request.getFilterName());
            assertThat(response.getDescription()).isEqualTo(request.getDescription());
            assertThat(response.getUrlPattern()).isEqualTo(request.getUrlPattern());
            assertThat(response.getDomain()).isEqualTo(request.getDomain());
            assertThat(response.getExtractionRegex()).isEqualTo(request.getExtractionRegex());
            assertThat(response.getCaptureGroupIndex()).isEqualTo(request.getCaptureGroupIndex());
            assertThat(response.getPriority()).isEqualTo(request.getPriority());
            assertThat(response.getActive()).isEqualTo(request.getActive());
        }

        @Test
        @DisplayName("Should update only provided fields (partial update)")
        void shouldUpdatePartialFields() {
            UserFilter savedFilter = fixture.createTestUserFilter(currentUser.getId());
            String originalUrlPattern = savedFilter.getUrlPattern();
            String originalDomain = savedFilter.getDomain();

            UpdateUserFilterRequest request = UpdateUserFilterRequest.builder()
                    .filterName("updated name")
                    .priority(7)
                    .active(false)
                    .build();

            UserFilterResponse response = normalUserRequest()
                    .pathParam("id", savedFilter.getId())
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(UserFilterResponse.class);

            // Updated fields
            assertThat(response.getFilterName()).isEqualTo(request.getFilterName());
            assertThat(response.getPriority()).isEqualTo(request.getPriority());
            assertThat(response.getActive()).isEqualTo(request.getActive());

            // Unchanged fields should remain the same
            assertThat(response.getUrlPattern()).isEqualTo(originalUrlPattern);
            assertThat(response.getDomain()).isEqualTo(originalDomain);
        }

        @Test
        @DisplayName("Should preserve matchCount and lastMatchedAt during update")
        void shouldPreserveMatchData() {
            UserFilter savedFilter = fixture.createTestUserFilter(currentUser.getId());
            savedFilter.setMatchCount(5L);
            savedFilter.setLastMatchedAt(Instant.now().minus(1, ChronoUnit.DAYS));
            userFilterRepository.save(savedFilter);

            UpdateUserFilterRequest request = UpdateUserFilterRequest.builder()
                    .filterName("updated name")
                    .build();

            UserFilterResponse response = normalUserRequest()
                    .pathParam("id", savedFilter.getId())
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(UserFilterResponse.class);

            assertThat(response.getMatchCount()).isEqualTo(5);
            assertThat(response.getLastMatchedAt()).isNotNull();
        }

    }

    @Nested
    @DisplayName("Validation Errors")
    class ValidationErrors {

        @Test
        @DisplayName("Should return 400 when all fields are null")
        void shouldReturn400WhenAllFieldsNull() {
            UserFilter savedFilter = fixture.createTestUserFilter(currentUser.getId());

            UpdateUserFilterRequest request = UpdateUserFilterRequest.builder()
                    .build();

            normalUserRequest()
                    .pathParam("id", savedFilter.getId())
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should return 400 when fields exceed character limit")
        void shouldReturn400WhenFieldsExceedCharacterLimit() {
            UserFilter savedFilter = fixture.createTestUserFilter(currentUser.getId());

            UpdateUserFilterRequest request = UpdateUserFilterRequest.builder()
                    .filterName("a".repeat(101))
                    .description("b".repeat(501))
                    .urlPattern("c".repeat(2049))
                    .domain("d".repeat(256))
                    .extractionRegex("e".repeat(1001))
                    .captureGroupIndex(2)
                    .priority(5)
                    .active(false)
                    .build();

            normalUserRequest()
                    .pathParam("id", savedFilter.getId())
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

    }

    @Nested
    @DisplayName("Resource Not Found")
    class ResourceNotFound {

        @Test
        @DisplayName("Should return 404 when filter does not exist")
        void shouldReturn404WhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();

            UpdateUserFilterRequest request = UpdateUserFilterRequest.builder()
                    .filterName("updated name")
                    .build();

            normalUserRequest()
                    .pathParam("id", nonExistentId)
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 404 when updating another user's filter")
        void shouldReturn404WhenUpdatingAnotherUsersFilter() {
            UserResponse adminUser = createAdminUser();
            UserFilter adminFilter = fixture.createTestUserFilter(adminUser.getId());

            UpdateUserFilterRequest request = UpdateUserFilterRequest.builder()
                    .filterName("trying to update")
                    .build();

            normalUserRequest()
                    .pathParam("id", adminFilter.getId())
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());

            // Verify filter wasn't updated
            UserFilter unchangedFilter = userFilterRepository.findById(adminFilter.getId()).orElseThrow();
            assertThat(unchangedFilter.getFilterName()).isEqualTo(adminFilter.getFilterName());
        }

    }

    @Nested
    @DisplayName("Business Logic Errors")
    class BusinessLogicErrors {

        @Test
        @DisplayName("Should return 409 when filterName conflicts with existing filter")
        void shouldReturn409OnFilterNameConflict() {
            UserFilter filter1 = fixture.createTestUserFilter(currentUser.getId());
            UserFilter filter2 = UserFilter.builder()
                    .user(User.builder().id(currentUser.getId()).build())
                    .filterName("unique filter name")
                    .urlPattern("https://www.unique-site.com/path")
                    .domain("www.unique-site.com")
                    .extractionRegex("/tag/([^/?#]+)")
                    .captureGroupIndex(1)
                    .priority(1)
                    .build();
            userFilterRepository.save(filter2);

            UpdateUserFilterRequest request = UpdateUserFilterRequest.builder()
                    .filterName("unique filter name")
                    .build();

            normalUserRequest()
                    .pathParam("id", filter1.getId())
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        @DisplayName("Should return 409 when urlPattern conflicts with existing filter")
        void shouldReturn409OnUrlPatternConflict() {
            UserFilter filter1 = fixture.createTestUserFilter(currentUser.getId());
            UserFilter filter2 = UserFilter.builder()
                    .user(User.builder().id(currentUser.getId()).build())
                    .filterName("another unique filter")
                    .urlPattern("https://www.unique-url.com/path")
                    .domain("www.unique-url.com")
                    .extractionRegex("/tag/([^/?#]+)")
                    .captureGroupIndex(1)
                    .priority(1)
                    .build();
            userFilterRepository.save(filter2);

            UpdateUserFilterRequest request = UpdateUserFilterRequest.builder()
                    .urlPattern("https://www.unique-url.com/path")
                    .build();

            normalUserRequest()
                    .pathParam("id", filter1.getId())
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

    }

    @Nested
    @DisplayName("Authentication and Authorization")
    class AuthenticationAndAuthorization {

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            UUID id = UUID.randomUUID();

            UpdateUserFilterRequest request = UpdateUserFilterRequest.builder()
                    .filterName("updated name")
                    .build();

            given()
                    .pathParam("id", id)
                    .body(request)
                    .when()
                    .patch(ApiEndpoints.USER_FILTER_BY_ID)
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
