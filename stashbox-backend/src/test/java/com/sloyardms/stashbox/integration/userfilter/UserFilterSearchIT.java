package com.sloyardms.stashbox.integration.userfilter;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.dto.PageResponse;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.entity.User;
import com.sloyardms.stashbox.user.repository.UserRepository;
import com.sloyardms.stashbox.userfilter.dto.UserFilterResponse;
import com.sloyardms.stashbox.userfilter.repository.UserFilterRepository;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
public class UserFilterSearchIT extends BaseIntegrationTest {

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
        @DisplayName("Should filter by domain")
        void shouldFilterByDomain() {
            fixture.createActiveFilters(currentUser.getId(), 5);
            String targetDomain = "www.asite3.com";

            PageResponse<UserFilterResponse> response = normalUserRequest()
                    .queryParam("domain", targetDomain)
                    .when()
                    .get(ApiEndpoints.USER_FILTERS)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(new TypeRef<>() {
                    });

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getDomain()).isEqualTo(targetDomain);
        }

        @Test
        @DisplayName("Should filter by active status")
        void shouldFilterByActive() {
            fixture.createActiveFilters(currentUser.getId(), 3);
            fixture.createInactiveFilters(currentUser.getId(), 2);

            PageResponse<UserFilterResponse> response = normalUserRequest()
                    .queryParam("active", true)
                    .when()
                    .get(ApiEndpoints.USER_FILTERS)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(new TypeRef<>() {
                    });

            assertThat(response.getPage().getTotalElements()).isEqualTo(3);
            assertThat(response.getContent()).allMatch(UserFilterResponse::getActive);
        }

        @Test
        @DisplayName("Should combine multiple filters")
        void shouldCombineFilters() {
            fixture.createActiveFilters(currentUser.getId(), 10);
            fixture.createInactiveFilters(currentUser.getId(), 5);

            PageResponse<UserFilterResponse> response = normalUserRequest()
                    .queryParam("search", "site")
                    .queryParam("domain", "www.asite5.com")
                    .queryParam("active", true)
                    .when()
                    .get(ApiEndpoints.USER_FILTERS)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(new TypeRef<>() {
                    });

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getDomain()).isEqualTo("www.asite5.com");
            assertThat(response.getContent().get(0).getActive()).isTrue();
        }

        @Test
        @DisplayName("Should handle custom page size")
        void shouldHandleCustomPageSize() {
            int customSize = 7;
            fixture.createActiveFilters(currentUser.getId(), 20);

            PageResponse<UserFilterResponse> response = normalUserRequest()
                    .queryParam("page", 0)
                    .queryParam("size", customSize)
                    .when()
                    .get(ApiEndpoints.USER_FILTERS)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(new TypeRef<>() {
                    });

            assertThat(response.getContent()).hasSize(customSize);
        }

        @Test
        @DisplayName("Should sort by filterName ascending")
        void shouldSortByFilterNameAsc() {
            fixture.createActiveFilters(currentUser.getId(), 5);

            PageResponse<UserFilterResponse> response = normalUserRequest()
                    .queryParam("sort", "filterName,asc")
                    .when()
                    .get(ApiEndpoints.USER_FILTERS)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(new TypeRef<>() {
                    });

            List<String> names = response.getContent().stream()
                    .map(UserFilterResponse::getFilterName)
                    .toList();

            assertThat(names).isSorted();
        }

        @Test
        @DisplayName("Should sort by priority descending")
        void shouldSortByPriorityDesc() {
            userFilterRepository.saveAll(List.of(
                    fixture.createFilterWithPriority(currentUser.getId(), "filter1", 1),
                    fixture.createFilterWithPriority(currentUser.getId(), "filter2", 5),
                    fixture.createFilterWithPriority(currentUser.getId(), "filter3", 3)
            ));

            PageResponse<UserFilterResponse> response = normalUserRequest()
                    .queryParam("sort", "priority,desc")
                    .when()
                    .get(ApiEndpoints.USER_FILTERS)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(new TypeRef<>() {
                    });

            List<Integer> priorities = response.getContent().stream()
                    .map(UserFilterResponse::getPriority)
                    .toList();

            assertThat(priorities).containsExactly(5, 3, 1);
        }
    }

    @Nested
    @DisplayName("Validation Errors")
    class ValidationErrors {

        @Test
        @DisplayName("Should return 400 when sorting by invalid field")
        void shouldReturn400WhenSortingByInvalidField() {
            normalUserRequest()
                    .queryParam("sort", "invalidField,asc")
                    .when()
                    .get(ApiEndpoints.USER_FILTERS)
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
                    .get(ApiEndpoints.USER_FILTERS)
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }
}
