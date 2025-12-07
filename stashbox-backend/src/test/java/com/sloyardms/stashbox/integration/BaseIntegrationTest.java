package com.sloyardms.stashbox.integration;

import com.sloyardms.stashbox.constants.ApiEndpoints;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.dto.PageResponse;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class BaseIntegrationTest {

    @Value("${spring.data.web.pageable.default-page-size}")
    public Integer defaultPageSize;
    public Integer smallPageSize;
    public Integer largePageSize;

    @LocalServerPort
    private int port;

    public static final String NORMAL_USERNAME = "normal_user";
    public static final String NORMAL_PASSWORD = "password";
    public static final String ADMIN_USERNAME = "admin_user";
    public static final String ADMIN_PASSWORD = "password";

    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:18.0")
            .withDatabaseName("stashboxdb")
            .withUsername("stashboxdbuser")
            .withPassword("stashboxdbpassword");

    @Container
    static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:26.4")
            .withRealmImportFile("/keycloak/stashbox-realm.json");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Override postgresql properties
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);

        // Override Keycloak properties
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "/realms/stashbox");
    }

    @BeforeEach
    void setUpTestBase() {
        smallPageSize = defaultPageSize - 5;
        largePageSize = defaultPageSize + 5;
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterEach
    void tearDownTestBase() {
        RestAssured.reset();
    }

    public RequestSpecification authenticatedRequest(String username, String password) {
        String token = generateAccessToken(username, password);
        return given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON);
    }

    public RequestSpecification normalUserRequest() {
        String token = generateAccessToken(NORMAL_USERNAME, NORMAL_PASSWORD);
        return given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON);
    }

    public RequestSpecification adminUserRequest() {
        String token = generateAccessToken(ADMIN_USERNAME, ADMIN_PASSWORD);
        return given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON);
    }

    private String generateAccessToken(String username, String password) {
        String tokenUrl = keycloakContainer.getAuthServerUrl() +
                "/realms/stashbox/protocol/openid-connect/token";
        return given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "stashbox-nextjs-client")
                .formParam("username", username)
                .formParam("password", password)
                .when()
                .post(tokenUrl)
                .then()
                .statusCode(200)
                .extract()
                .path("access_token");
    }

    public UserResponse createNormalUser() {
        return createUser(normalUserRequest());
    }

    public UserResponse createAdminUser() {
        return createUser(adminUserRequest());
    }

    private UserResponse createUser(RequestSpecification requestSpec) {
        return requestSpec
                .when()
                .post(ApiEndpoints.USER_PROFILE)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(UserResponse.class);
    }

    /**
     * Asserts pagination metadata matches expected values
     */
    public void assertPaginationMetadata(PageResponse<?> response,
                                          int expectedSize,
                                          int expectedTotalElements) {
        int expectedTotalPages = calculateExpectedPages(expectedTotalElements, expectedSize);

        assertThat(response.getPage().getSize()).isEqualTo(expectedSize);
        assertThat(response.getPage().getTotalElements()).isEqualTo(expectedTotalElements);
        assertThat(response.getPage().getTotalPages()).isEqualTo(expectedTotalPages);
    }

    /**
     * Calculates expected number of pages for pagination
     */
    private int calculateExpectedPages(int totalElements, int pageSize) {
        if (totalElements == 0) return 0;
        return (int) Math.ceil((double) totalElements / pageSize);
    }

}