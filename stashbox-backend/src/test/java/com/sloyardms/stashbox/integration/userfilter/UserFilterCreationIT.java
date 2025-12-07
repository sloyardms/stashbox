package com.sloyardms.stashbox.integration.userfilter;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.user.repository.UserRepository;
import com.sloyardms.stashbox.userfilter.dto.CreateUserFilterRequest;
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

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
public class UserFilterCreationIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFilterRepository userFilterRepository;

    @Autowired
    private UserFilterFixture fixture;

    private UserResponse currentUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAllInBatch();
        userFilterRepository.deleteAllInBatch();

        currentUser = createNormalUser();
    }

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return 201 when filter is created successfully")
        void shouldReturn201WhenCreated() {
            CreateUserFilterRequest request = CreateUserFilterRequest.builder()
                    .filterName("test filter")
                    .description("test description")
                    .urlPattern("https://www.testpage.com/tag/some-title")
                    .domain("www.testpage.com")
                    .extractionRegex("/tag/([^/?#]+)")
                    .captureGroupIndex(1)
                    .priority(1)
                    .build();

            UserFilterResponse response = normalUserRequest()
                    .body(request)
                    .when()
                    .post(ApiEndpoints.USER_FILTERS)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract()
                    .as(UserFilterResponse.class);

            assertThat(response.getId()).isNotNull();
            assertThat(response.getFilterName()).isEqualTo(request.getFilterName());
            assertThat(response.getUrlPattern()).isEqualTo(request.getUrlPattern());
            assertThat(response.getDomain()).isEqualTo(request.getDomain());
            assertThat(response.getExtractionRegex()).isEqualTo(request.getExtractionRegex());
            assertThat(response.getCaptureGroupIndex()).isEqualTo(request.getCaptureGroupIndex());
            assertThat(response.getPriority()).isEqualTo(request.getPriority());
            assertThat(response.getActive()).isTrue();
            assertThat(response.getMatchCount()).isZero();
            assertThat(response.getLastMatchedAt()).isNull();
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();
        }

    }

    @Nested
    @DisplayName("Validation Errors")
    class ValidationErrors {

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        void shouldReturn400WhenMissingRequiredFields() {
            CreateUserFilterRequest request = CreateUserFilterRequest.builder()
                    .description("incomplete request")
                    .build();

            normalUserRequest()
                    .body(request)
                    .when()
                    .post(ApiEndpoints.USER_FILTERS)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should return 400 when fields exceed character limit")
        void shouldReturn400WhenFieldsExceedCharacterLimit() {
            UserFilter savedFilter = fixture.createTestUserFilter(currentUser.getId());

            CreateUserFilterRequest request = CreateUserFilterRequest.builder()
                    .filterName("a".repeat(101))
                    .description("b".repeat(501))
                    .urlPattern("c".repeat(2049))
                    .domain("d".repeat(256))
                    .extractionRegex("e".repeat(1001))
                    .captureGroupIndex(2)
                    .priority(5)
                    .build();

            normalUserRequest()
                    .body(request)
                    .when()
                    .post(ApiEndpoints.USER_FILTERS)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

    }

    @Nested
    @DisplayName("Authentication and Authorization")
    class AuthenticationAndAuthorization {

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .post(ApiEndpoints.USER_FILTERS)
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
