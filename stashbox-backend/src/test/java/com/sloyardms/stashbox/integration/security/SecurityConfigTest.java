package com.sloyardms.stashbox.integration.security;

import com.sloyardms.stashbox.integration.common.BaseIntegrationTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityConfigTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("GET /api/v1/public/test")
    class GetApiV1PublicTests {

        @Test
        @DisplayName("Should return 200 with 'Hello World' String")
        void getApiV1Public() {
            given()
                    .when()
                    .get("/api/v1/public/test")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo("Hello World"));
        }

    }

    @Nested
    @DisplayName("GET /api/v1/user/test")
    class GetApiV1UserTests {

        @Test
        @DisplayName("Should return 200 with the normal username String")
        void shouldReturn200_WhenRegularUserIsAuthenticated() {
            normalUserRequest()
                    .when()
                    .get("/api/v1/user/test")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(NORMAL_USERNAME));
        }

        @Test
        @DisplayName("Should return 200 with the admin username String")
        void shouldReturn200_WhenAdminUserIsAuthenticated() {
            adminUserRequest()
                    .when()
                    .get("/api/v1/user/test")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(ADMIN_USERNAME));
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .get("/api/v1/user/test")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Nested
    @DisplayName("GET /api/v1/admin/test")
    class GetApiV1AdminTests {

        @Test
        @DisplayName("Should return 200 with the admin username String")
        void getApiV1Admin() {
            adminUserRequest()
                    .when()
                    .get("/api/v1/admin/test")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(ADMIN_USERNAME));
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        void shouldReturn403_WhenUserIsNotAdmin() {
            normalUserRequest()
                    .when()
                    .get("/api/v1/admin/test")
                    .then()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401_WhenUserIsNotAuthenticated() {
            given()
                    .when()
                    .get("/api/v1/admin/test")
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
