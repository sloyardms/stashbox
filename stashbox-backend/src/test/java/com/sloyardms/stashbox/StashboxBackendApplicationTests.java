package com.sloyardms.stashbox;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class StashboxBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
