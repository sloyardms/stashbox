package com.sloyardms.backend.integration.user_filter;

import com.sloyardms.backend.integration.common.BaseIntegrationTest;
import com.sloyardms.backend.user.UserRepository;
import com.sloyardms.backend.user.dto.UserDetailDto;
import com.sloyardms.backend.user_filter.UserFilterRepository;
import com.sloyardms.backend.user_filter.dto.UserFilterCreateDto;
import com.sloyardms.backend.user_filter.dto.UserFilterUpdateDto;
import com.sloyardms.backend.user_filter.entity.UserFilter;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ActiveProfiles("prod")
public class UserFilterControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFilterRepository userFilterRepository;

    private final String normalUserName = "normal_user";
    private final String normalUserPassword = "password";
    private UserFilter existingUserFilter;

    @BeforeEach
    public void setupDatabase() {
        userFilterRepository.deleteAll();
        userRepository.deleteAll();

        // Create normal user
        UserDetailDto existingUser = authenticatedRequest(normalUserName, normalUserPassword)
                .when()
                .post("/api/v1/me")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(ContentType.JSON)
                .extract()
                .as(UserDetailDto.class);

        existingUserFilter = UserFilterEntityDataBuilder
                .aValidFilter(existingUser.getId())
                .buildAndSave(userFilterRepository);
    }

    @Nested
    @DisplayName("GET /api/v1/me/filters/{id}")
    class GetUserFilterById {

        @Test
        @DisplayName("Should return 200 with user filter when filter exists")
        void shouldReturn200_WhenFilterExists() {
            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", existingUserFilter.getId())
                    .when()
                    .get("/api/v1/me/filters/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(existingUserFilter.getId().toString()))
                    .body("filterName", equalTo(existingUserFilter.getFilterName()))
                    .body("urlPattern", equalTo(existingUserFilter.getUrlPattern()))
                    .body("extractionRegex", equalTo(existingUserFilter.getExtractionRegex()))
                    .body("active", equalTo(true))
                    .body("createdAt", notNullValue())
                    .body("updatedAt", notNullValue());
        }

        @Test
        @DisplayName("Should return 404 when user filter does not exist")
        void shouldReturn404_WhenFilterDoesNotExist() {
            UUID nonExistentFilterId = UUID.randomUUID();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", nonExistentFilterId)
                    .when()
                    .get("/api/v1/me/filters/{id}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .get("/api/v1/me/filters/{id}", existingUserFilter.getId())
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("GET /api/v1/me/filters")
    class GetAllFilters {

        @Test
        @DisplayName("Should return 200 with paginated user filters when filters exists")
        void shouldReturn200_WhenFiltersExist() {
            authenticatedRequest(normalUserName, normalUserPassword)
                    .when()
                    .get("/api/v1/me/filters")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("content[0].id", equalTo(existingUserFilter.getId().toString()))
                    .body("content[0].filterName", equalTo(existingUserFilter.getFilterName()))
                    .body("content[0].urlPattern", equalTo(existingUserFilter.getUrlPattern()))
                    .body("content[0].extractionRegex", equalTo(existingUserFilter.getExtractionRegex()))
                    .body("content[0].active", equalTo(true))
                    .body("content[0].createdAt", notNullValue())
                    .body("content[0].updatedAt", notNullValue());
        }

        @Test
        @DisplayName("Should return 200 with empty page when user filters does not exists")
        void shouldReturn200WithEmptyPage_WhenNoFiltersExist() {
            userFilterRepository.deleteAll();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .when()
                    .get("/api/v1/me/filters")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(0));
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .get("/api/v1/me/filters")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("POST /api/v1/me/filters")
    class CreateFilter {

        @Test
        @DisplayName("Should return 201 with created user filter when filter is created")
        void shouldReturn201_WhenFilterIsValid() {
            userFilterRepository.deleteAll();
            UserFilterCreateDto createDto = UserFilterCreateDtoDataBuilder.aValidCreateDto().build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .body(createDto)
                    .when()
                    .post("/api/v1/me/filters")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", notNullValue())
                    .body("filterName", equalTo(createDto.getFilterName()))
                    .body("urlPattern", equalTo(createDto.getUrlPattern()))
                    .body("extractionRegex", equalTo(createDto.getExtractionRegex()))
                    .body("active", equalTo(true))
                    .body("createdAt", notNullValue())
                    .body("updatedAt", notNullValue());
        }

        @Test
        @DisplayName("Should return 400 when user filter is invalid")
        void shouldReturn400_WhenFilterIsInvalid() {
            UserFilterCreateDto createDto = UserFilterCreateDtoDataBuilder.anInvalidCreateDto().build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .contentType(ContentType.JSON)
                    .body(createDto)
                    .when()
                    .post("/api/v1/me/filters")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/v1/me/filters")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("PATCH /api/v1/me/filters/{id}")
    class UpdateFilter {

        @Test
        @DisplayName("Should return 204 and update user filter when filter exists")
        void shouldReturn204_WhenFilterExists() {
            UserFilterUpdateDto updateDto = UserFilterUpdateDtoDataBuilder
                    .aValidUpdateDto()
                    .asInactive()
                    .build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", existingUserFilter.getId())
                    .contentType(ContentType.JSON)
                    .body(updateDto)
                    .when()
                    .patch("/api/v1/me/filters/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(existingUserFilter.getId().toString()))
                    .body("filterName", equalTo(updateDto.getFilterName()))
                    .body("urlPattern", equalTo(updateDto.getUrlPattern()))
                    .body("extractionRegex", equalTo(updateDto.getExtractionRegex()))
                    .body("active", equalTo(updateDto.getActive()))
                    .body("createdAt", notNullValue())
                    .body("updatedAt", notNullValue());
        }

        @Test
        @DisplayName("Should return 400 when user filter body is invalid")
        void shouldReturn400_WhenFilterIsInvalid() {
            UserFilterUpdateDto updateDto = UserFilterUpdateDtoDataBuilder.anEmptyUpdateDto().build();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", existingUserFilter.getId())
                    .contentType(ContentType.JSON)
                    .body(updateDto)
                    .when()
                    .patch("/api/v1/me/filters/{id}")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should return 404 when user filter does not exist")
        void shouldReturn404_WhenFilterDoesNotExist() {
            UUID nonExistentFilterId = UUID.randomUUID();
            UserFilterUpdateDto updateDto = UserFilterUpdateDtoDataBuilder.aValidUpdateDto().build();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", nonExistentFilterId)
                    .contentType(ContentType.JSON)
                    .body(updateDto)
                    .when()
                    .patch("/api/v1/me/filters/{id}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .pathParams("id", existingUserFilter.getId())
                    .contentType(ContentType.JSON)
                    .when()
                    .patch("/api/v1/me/filters/{id}")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("DELETE /api/v1/me/filters/{id}")
    class DeleteFilter {

        @Test
        @DisplayName("Should return 204 and delete user filter when filter exists")
        void shouldReturn204_WhenFilterExists() {
            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", existingUserFilter.getId())
                    .when()
                    .delete("/api/v1/me/filters/{id}")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        @DisplayName("Should return 404 when user filter does not exist")
        void shouldReturn404_WhenFilterDoesNotExist() {
            UUID nonExistentFilterId = UUID.randomUUID();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", nonExistentFilterId)
                    .when()
                    .delete("/api/v1/me/filters/{id}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .pathParams("id", existingUserFilter.getId())
                    .when()
                    .delete("/api/v1/me/filters/{id}")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

}
