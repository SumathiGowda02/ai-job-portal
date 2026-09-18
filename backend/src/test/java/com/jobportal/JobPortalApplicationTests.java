package com.jobportal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JobPortalApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context (security, JPA, Redis, controllers) wires up correctly
    }
}
