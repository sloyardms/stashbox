package com.sloyardms.backend.integration.common;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:18.0")
            .withDatabaseName("stashboxdb")
            .withUsername("user")
            .withPassword("password");

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

    @BeforeAll
    static void startContainers() {
        postgresContainer.start();
        keycloakContainer.start();
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        RestAssured.reset();
    }

    public RequestSpecification authenticatedRequest(String username, String password) {
        String token = generateAccessToken(username, password);
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
}
