package com.sloyardms.stashbox.containers;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainersConfig {

    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
    private static final KeycloakContainer KEYCLOAK_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:18.0")
                .withDatabaseName("stashboxdb")
                .withUsername("stashboxdbuser")
                .withPassword("stashboxdbpassword")
                .withReuse(true);  // Enable reuse

        KEYCLOAK_CONTAINER = new KeycloakContainer("quay.io/keycloak/keycloak:26.4")
                .withRealmImportFile("/keycloak/stashbox-realm.json")
                .withReuse(true);  // Enable reuse

        POSTGRES_CONTAINER.start();
        KEYCLOAK_CONTAINER.start();
    }

    public static PostgreSQLContainer<?> getPostgresContainer() {
        return POSTGRES_CONTAINER;
    }

    public static KeycloakContainer getKeycloakContainer() {
        return KEYCLOAK_CONTAINER;
    }

}
