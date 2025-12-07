package com.sloyardms.stashbox.integration.userfilter;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.integration.BaseIntegrationTest;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.user.repository.UserRepository;
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
public class UserFilterDomainsRetrievalIT extends BaseIntegrationTest {

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
        @DisplayName("Should return 200 with empty page when the user has no UserFilters")
        void shouldReturn200WithEmptyPageWhenUserHasNoUserFilters() {
            List<String> response = normalUserRequest()
                    .when()
                    .get(ApiEndpoints.USER_FILTERS_DOMAIN_LIST)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(new TypeRef<>() {
                    });
            assertThat(response.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 200 with found domains list when user has UserFilters")
        void shouldReturn200WithFoundDomainsListWhenUserHasUserFilters() {
            int numberOfFilters = 5;
            fixture.createActiveFilters(currentUser.getId(), numberOfFilters);

            List<String> response = normalUserRequest()
                    .when()
                    .get(ApiEndpoints.USER_FILTERS_DOMAIN_LIST)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(new TypeRef<>() {
                    });
            assertThat(response.size()).isEqualTo(numberOfFilters);
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
                    .post(ApiEndpoints.USER_FILTERS_DOMAIN_LIST)
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
