package com.sloyardms.backend.integration.group;

import com.sloyardms.backend.group.ItemGroupRepository;
import com.sloyardms.backend.group.dto.ItemGroupCreateDto;
import com.sloyardms.backend.group.dto.ItemGroupUpdateDto;
import com.sloyardms.backend.group.entity.ItemGroup;
import com.sloyardms.backend.integration.common.BaseIntegrationTest;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ActiveProfiles("prod")
public class ItemGroupControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemGroupRepository itemGroupRepository;

    private final String normalUserName = "normal_user";
    private final String normalUserPassword = "password";
    private UserDetailDto existingUser;

    @BeforeEach
    public void setupDatabase() {
        itemGroupRepository.deleteAll();
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
    @DisplayName("GET /api/v1/groups/{id}")
    class GetGroupById {

        @Test
        @DisplayName("Should return 200 with group when group exists")
        void shouldReturn200_WhenGroupExists() {
            // Create test group
            ItemGroup savedGroup =
                    ItemGroupEntityDataBuilder.aValidGroup(existingUser.getId()).buildAndSave(itemGroupRepository);

            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", savedGroup.getId())
                    .when()
                    .get("/api/v1/groups/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(savedGroup.getId().toString()))
                    .body("name", equalTo(savedGroup.getName()))
                    .body("description", equalTo(savedGroup.getDescription()))
                    .body("defaultGroup", equalTo(savedGroup.isDefaultGroup()))
                    .body("createdAt", notNullValue())
                    .body("updatedAt", notNullValue());
        }

        @Test
        @DisplayName("Should return 404 when group does not exists")
        void shouldReturn404_WhenGroupDoesNotExist() {
            UUID nonExistentGroupId = UUID.randomUUID();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", nonExistentGroupId)
                    .when()
                    .get("/api/v1/groups/{id}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            UUID nonExistentGroupId = UUID.randomUUID();
            given()
                    .pathParams("id", nonExistentGroupId)
                    .when()
                    .get("/api/v1/groups/{id}")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("GET /api/v1/groups")
    class GetAllGroups {

        @Test
        @DisplayName("Should return 200 with paginated groups when groups exists")
        void shouldReturn200_WhenGroupsExist() {
            authenticatedRequest(normalUserName, normalUserPassword)
                    .when()
                    .get("/api/v1/groups")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(1))
                    .body("content[0].id", notNullValue())
                    .body("content[0].name", equalTo("Ungrouped"));
        }

        @Test
        @DisplayName("Should return 200 with empty page when groups does not exists")
        void shouldReturn200WithEmptyPage_WhenNoGroupsExist() {
            itemGroupRepository.deleteAll();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .when()
                    .get("/api/v1/groups")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(0));
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .get("/api/v1/groups")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("POST /api/v1/groups")
    class CreateGroup {

        @Test
        @DisplayName("Should return 201 with created group when group is created")
        void shouldReturn201_WhenGroupIsValid() {
            ItemGroupCreateDto createDto = ItemGroupCreateDtoDataBuilder.aValidCreateDto().build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .body(createDto)
                    .when()
                    .post("/api/v1/groups")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", notNullValue())
                    .body("name", equalTo(createDto.getName()))
                    .body("description", equalTo(createDto.getDescription()))
                    .body("defaultGroup", equalTo(false))
                    .body("createdAt", notNullValue())
                    .body("updatedAt", notNullValue());
        }

        @Test
        @DisplayName("Should return 400 when group is invalid")
        void shouldReturn400_WhenGroupIsInvalid() {
            ItemGroupCreateDto createDto = ItemGroupCreateDtoDataBuilder.anInvalidCreateDto().build();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .body(createDto)
                    .when()
                    .post("/api/v1/me/filters")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            ItemGroupCreateDto createDto = ItemGroupCreateDtoDataBuilder.aValidCreateDto().build();
            given()
                    .body(createDto)
                    .when()
                    .post("/api/v1/me/filters")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("PATCH /api/v1/groups/{id}")
    class UpdateGroup {

        @Test
        @DisplayName("Should return 204 and updated group when group exists")
        void shouldReturn204_WhenGroupExists() {
            ItemGroup savedGroup =
                    ItemGroupEntityDataBuilder.aValidGroup(existingUser.getId()).buildAndSave(itemGroupRepository);
            UUID groupId = savedGroup.getId();

            ItemGroupUpdateDto updateDto = ItemGroupUpdateDtoDataBuilder.aValidUpdateDto().build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", groupId)
                    .contentType(ContentType.JSON)
                    .body(updateDto)
                    .when()
                    .patch("/api/v1/groups/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", notNullValue())
                    .body("name", equalTo(updateDto.getName()))
                    .body("description", equalTo(updateDto.getDescription()))
                    .body("defaultGroup", equalTo(updateDto.getDefaultGroup()))
                    .body("createdAt", notNullValue())
                    .body("updatedAt", notNullValue());
        }

        @Test
        @DisplayName("Should return 404 when group does not exists")
        void shouldReturn404_WhenGroupDoesNotExist() {
            UUID nonExistentGroupId = UUID.randomUUID();
            ItemGroupUpdateDto updateDto = ItemGroupUpdateDtoDataBuilder.aValidUpdateDto().build();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", nonExistentGroupId)
                    .contentType(ContentType.JSON)
                    .body(updateDto)
                    .when()
                    .patch("/api/v1/groups/{id}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            UUID nonExistentGroupId = UUID.randomUUID();
            ItemGroupUpdateDto updateDto = ItemGroupUpdateDtoDataBuilder.aValidUpdateDto().build();

            given()
                    .pathParams("id", nonExistentGroupId)
                    .contentType(ContentType.JSON)
                    .body(updateDto)
                    .when()
                    .patch("/api/v1/groups/{id}")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("DELETE /api/v1/groups/{id}")
    class DeleteGroupById {

        @Test
        @DisplayName("Should return 204 and delete group when group exists")
        void shouldReturn204_WhenGroupExists() {
            int groupsSize = itemGroupRepository.findAll().size();

            ItemGroup savedGroup =
                    ItemGroupEntityDataBuilder.aValidGroup(existingUser.getId()).buildAndSave(itemGroupRepository);
            UUID groupId = savedGroup.getId();

            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", groupId)
                    .when()
                    .delete("/api/v1/groups/{id}")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            Optional<ItemGroup> deletedGroup = itemGroupRepository.findById(groupId);
            assertThat(deletedGroup).isNotPresent();

            int groupsSizeAfterDelete = itemGroupRepository.findAll().size();
            assertThat(groupsSizeAfterDelete).isEqualTo(groupsSize);
        }

        @Test
        @DisplayName("Should return 404 when group does not exists")
        void shouldReturn404_WhenGroupDoesNotExist() {
            UUID nonExistentGroupId = UUID.randomUUID();
            authenticatedRequest(normalUserName, normalUserPassword)
                    .pathParams("id", nonExistentGroupId)
                    .when()
                    .delete("/api/v1/groups/{id}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            UUID nonExistentGroupId = UUID.randomUUID();
            given()
                    .pathParams("id", nonExistentGroupId)
                    .when()
                    .delete("/api/v1/groups/{id}")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
