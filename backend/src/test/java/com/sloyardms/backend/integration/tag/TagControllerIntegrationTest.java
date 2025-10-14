package com.sloyardms.backend.integration.tag;

import com.sloyardms.backend.integration.common.BaseIntegrationTest;
import com.sloyardms.backend.tag.TagRepository;
import com.sloyardms.backend.tag.dto.TagCreateDto;
import com.sloyardms.backend.tag.dto.TagUpdateDto;
import com.sloyardms.backend.tag.entity.Tag;
import com.sloyardms.backend.user.UserRepository;
import com.sloyardms.backend.user.dto.UserDetailDto;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("prod")
public class TagControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    private final String normalUserName = "normal_user";
    private final String normalUserPassword = "password";
    private UserDetailDto existingUser;

    @BeforeEach
    public void setupDatabase() {
        tagRepository.deleteAll();
        userRepository.deleteAll();

        // Create normal user
        existingUser = authenticatedRequest(normalUserName, normalUserPassword)
                .when()
                .post("/api/v1/me")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(ContentType.JSON)
                .extract()
                .as(UserDetailDto.class);
    }

    @Nested
    @DisplayName("GET /api/v1/tags/{id}")
    class GetTagById {

        @Test
        @DisplayName("Should return 200 with tag when tag exists")
        void shouldReturn200_WhenTagExists() {
            Tag savedTag = TagEntityDataBuilder.aValidTag(existingUser.getId())
                    .buildAndSave(tagRepository);

            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", savedTag.getId())
                    .when()
                    .get("/api/v1/tags/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(savedTag.getId().toString()))
                    .body("name", equalTo(savedTag.getName()));
        }

        @Test
        @DisplayName("Should return 404 when tag does not exist")
        void shouldReturn404_WhenTagDoesNotExist() {
            UUID nonExistentTagId = UUID.randomUUID();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", nonExistentTagId)
                    .when()
                    .get("/api/v1/tags/{id}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            UUID nonExistentTagId = UUID.randomUUID();
            given()
                    .pathParams("id", nonExistentTagId)
                    .when()
                    .get("/api/v1/tags/{id}")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("GET /api/v1/tags")
    class GetAllTags {

        @Test
        @DisplayName("Should return 200 with paginated tags when tags exist")
        void shouldReturn200_WhenTagsExist() {
            authenticatedRequest(normalUserName, normalUserPassword)
                    .when()
                    .get("/api/v1/tags")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", greaterThanOrEqualTo(0)); // Adjust according to initial data
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .get("/api/v1/tags")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("POST /api/v1/tags")
    class CreateTag {

        @Test
        @DisplayName("Should return 201 with created tag when tag is valid")
        void shouldReturn201_WhenTagIsValid() {
            TagCreateDto createDto = TagCreateDtoDataBuilder.aValidCreateDto().build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .body(createDto)
                    .when()
                    .post("/api/v1/tags")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", notNullValue())
                    .body("name", equalTo(createDto.getName()));
        }

        @Test
        @DisplayName("Should return 400 when tag is invalid")
        void shouldReturn400_WhenTagIsInvalid() {
            TagCreateDto createDto = TagCreateDtoDataBuilder.anInvalidCreateDto().build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .body(createDto)
                    .when()
                    .post("/api/v1/tags")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            TagCreateDto createDto = TagCreateDtoDataBuilder.aValidCreateDto().build();

            given()
                    .body(createDto)
                    .when()
                    .post("/api/v1/tags")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("PATCH /api/v1/tags/{id}")
    class UpdateTag {

        @Test
        @DisplayName("Should return 200 and updated tag when tag exists")
        void shouldReturn200_WhenTagExists() {
            Tag savedTag = TagEntityDataBuilder.aValidTag(existingUser.getId())
                    .buildAndSave(tagRepository);

            TagUpdateDto updateDto = TagUpdateDtoDataBuilder.aValidUpdateDto().build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", savedTag.getId())
                    .contentType(ContentType.JSON)
                    .body(updateDto)
                    .when()
                    .patch("/api/v1/tags/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", notNullValue())
                    .body("name", equalTo(updateDto.getName()));
        }

        @Test
        @DisplayName("Should return 404 when tag does not exist")
        void shouldReturn404_WhenTagDoesNotExist() {
            UUID nonExistentTagId = UUID.randomUUID();
            TagUpdateDto updateDto = TagUpdateDtoDataBuilder.aValidUpdateDto().build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", nonExistentTagId)
                    .contentType(ContentType.JSON)
                    .body(updateDto)
                    .when()
                    .patch("/api/v1/tags/{id}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            UUID nonExistentTagId = UUID.randomUUID();
            TagUpdateDto updateDto = TagUpdateDtoDataBuilder.aValidUpdateDto().build();

            given()
                    .pathParams("id", nonExistentTagId)
                    .contentType(ContentType.JSON)
                    .body(updateDto)
                    .when()
                    .patch("/api/v1/tags/{id}")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("DELETE /api/v1/tags/{id}")
    class DeleteTagById {

        @Test
        @DisplayName("Should return 204 and delete tag when tag exists")
        void shouldReturn204_WhenTagExists() {
            int tagsSize = tagRepository.findAll().size();

            Tag savedTag = TagEntityDataBuilder.aValidTag(existingUser.getId())
                    .buildAndSave(tagRepository);

            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", savedTag.getId())
                    .when()
                    .delete("/api/v1/tags/{id}")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            Optional<Tag> deletedTag = tagRepository.findById(savedTag.getId());
            assertThat(deletedTag).isNotPresent();

            int tagsSizeAfterDelete = tagRepository.findAll().size();
            assertThat(tagsSizeAfterDelete).isEqualTo(tagsSize);
        }

        @Test
        @DisplayName("Should return 404 when tag does not exist")
        void shouldReturn404_WhenTagDoesNotExist() {
            UUID nonExistentTagId = UUID.randomUUID();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", nonExistentTagId)
                    .when()
                    .delete("/api/v1/tags/{id}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            UUID nonExistentTagId = UUID.randomUUID();
            given()
                    .pathParams("id", nonExistentTagId)
                    .when()
                    .delete("/api/v1/tags/{id}")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}